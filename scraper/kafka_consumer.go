package main

import (
	"./gen"
	"fmt"
	"log"
	"net/url"
	"time"

	"code.google.com/p/goprotobuf/proto"
	"github.com/Shopify/sarama"
)

// KafkaConsumer is responsible for listening to messages on a Kafka queue; consuming the protobufs
// and emitting the Go representations to the system
type KafkaConsumer struct {
	KafkaClient *sarama.Client
	OutputChan  chan ScrapeRequest
	Topic       string

	Partitions []int32
	Consumers  []*sarama.Consumer

	zookeeperAddrs []string
}

// NewKafkaConsumer creates a new kafka consumer pointing at the given addresses and topic
func NewKafkaConsumer(kafkaAddrs []string, zookeeperAddrs []string, topic string) (*KafkaConsumer, error) {
	c, err := sarama.NewClient("link_checker_scraper", kafkaAddrs, sarama.NewClientConfig())
	if err != nil {
		return nil, err
	}

	partitions, err := c.Partitions(topic)
	if err != nil {
		return nil, fmt.Errorf("Unable to determine # of partitions in topic %s (%v)", topic, err)
	}

	return &KafkaConsumer{
		Topic:          topic,
		KafkaClient:    c,
		OutputChan:     make(chan ScrapeRequest, 4),
		Consumers:      make([]*sarama.Consumer, len(partitions)),
		Partitions:     partitions,
		zookeeperAddrs: zookeeperAddrs,
	}, nil
}

func (kc *KafkaConsumer) start() {
	checkpointer, err := NewZKPartitioner(kc.zookeeperAddrs)
	if err != nil {
		panic(fmt.Sprintf("Failed to create ZKpartitioner: %v", err))
	}

	for i, partition := range kc.Partitions {
		offset, err := checkpointer.GetLastCheckpoint(kc.Topic, partition)
		if err != nil {
			panic(fmt.Sprintf("Failed to retrieve offset for %s/%d: %v!", kc.Topic, partition, err))
		}

		consumer, err := sarama.NewConsumer(kc.KafkaClient, kc.Topic, partition, "link_checker_scraper", getConsumerConfig(offset))
		if err != nil {
			log.Fatalf("Failed to create consumer: %v", err)
		}

		kc.Consumers[i] = consumer
		log.Printf("Starting KafkaConsumer for topic %s,  partition %d, waiting for requests", kc.Topic, partition)

		go func(partition int32) {
			var lastOffset int64

			clock := time.Tick(1 * time.Second)
			lastOffset = -1

			for {
				select {
				case <-clock:
					if lastOffset != -1 {
						err := checkpointer.SaveCheckpoint(kc.Topic, partition, lastOffset)
						if err != nil {
							panic(fmt.Sprintf("Failed to save checkpoint for partition %d offset %d! %v", partition, lastOffset, err))
						}
					}
				case e := <-consumer.Events():
					if e.Err != nil {
						log.Printf("Error retrieving event: %v", e.Err)
						continue
					}

					lastOffset = e.Offset

					sr, err := decodeScrapeRequest(e.Value)
					if err != nil {
						log.Printf("Error decoding value: %v", err)
						continue
					}

					kc.OutputChan <- *sr
				}
			}
		}(partition)
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

func getConsumerConfig(offset int64) *sarama.ConsumerConfig {
	c := sarama.NewConsumerConfig()

	if offset == -1 {
		c.OffsetMethod = sarama.OffsetMethodNewest
	} else {
		c.OffsetMethod = sarama.OffsetMethodManual
		c.OffsetValue = offset
	}

	return c
}
