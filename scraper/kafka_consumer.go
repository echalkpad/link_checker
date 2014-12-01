package main

import (
	"./gen"
	"code.google.com/p/goprotobuf/proto"
	"fmt"
	"github.com/Shopify/sarama"
	"log"
	"net/url"
)

// KafkaConsumer is responsible for listening to messages on a Kafka queue; consuming the protobufs
// and emitting the Go representations to the system
type KafkaConsumer struct {
	KafkaClient *sarama.Client
	OutputChan  chan ScrapeRequest
	Topic       string

	Partitions []int32
	Consumers  []*sarama.Consumer
}

// NewKafkaConsumer creates a new kafka consumer pointing at the given addresses and topic
func NewKafkaConsumer(addrs []string, topic string) (*KafkaConsumer, error) {
	c, err := sarama.NewClient("link_checker_scraper", addrs, sarama.NewClientConfig())
	if err != nil {
		return nil, err
	}

	partitions, err := c.Partitions(topic)
	if err != nil {
		return nil, fmt.Errorf("Unable to determine # of partitions in topic %s (%v)", topic, err)
	}

	return &KafkaConsumer{
		Topic:       topic,
		KafkaClient: c,
		OutputChan:  make(chan ScrapeRequest, 4),
		Consumers:   make([]*sarama.Consumer, len(partitions)),
		Partitions:  partitions,
	}, nil
}

func (kc *KafkaConsumer) start() {
	for i, partition := range kc.Partitions {
		consumer, err := sarama.NewConsumer(kc.KafkaClient, kc.Topic, partition, "link_checker_scraper", getConsumerConfig())
		if err != nil {
			log.Fatalf("Failed to create consumer: %v", err)
		}

		kc.Consumers[i] = consumer
		log.Printf("Starting KafkaConsumer for topic %s,  partition %d, waiting for requests", kc.Topic, partition)

		go func() {
			for e := range consumer.Events() {
				if e.Err != nil {
					log.Printf("Error retrieving event: %v", e.Err)
					continue
				}

				sr, err := decodeScrapeRequest(e.Value)
				if err != nil {
					log.Printf("Error decoding value: %v", err)
					continue
				}

				kc.OutputChan <- *sr
			}
		}()
	}
}

func (kc *KafkaConsumer) stop() {
	for _, consumer := range kc.Consumers {
		consumer.Close()
	}

	kc.KafkaClient.Close()
}

func decodeScrapeRequest(buf []byte) (*ScrapeRequest, error) {
	scraperMessage := &link_checker.ScraperMessage{}
	err := proto.Unmarshal(buf, scraperMessage)
	if err != nil {
		return nil, err
	}

	if scraperMessage.GetType() != link_checker.ScraperMessage_SCRAPE_REQUEST {
		return nil, fmt.Errorf("Unknown type %v", scraperMessage.GetType())
	}

	parsedURL, err := url.Parse(scraperMessage.Request.GetUrl())
	if err != nil {
		return nil, err
	}

	return &ScrapeRequest{url: parsedURL, depth: 1}, nil
}

func getConsumerConfig() *sarama.ConsumerConfig {
	c := sarama.NewConsumerConfig()
	c.OffsetMethod = sarama.OffsetMethodOldest

	return c
}
