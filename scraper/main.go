/*The scraper command is a worker process that can check various URLs to make sure they still return content,
and is also capable of discovering links on a page so they can be scraped in the future.

The scraper leverages goroutines to scrape multiple domains concurrently.
*/
package main

import (
	"log"
)

func webProcessFactory() RequestProcessor {
	return &WebProcessor{retriever: NewWebClient(), extractor: NewLinkExtractor()}
}

func workerFactory() Dispatcher {
	return NewBucketDispatcher(3, RequestProcessorFactory(webProcessFactory))
}

// General architecture:
//
// The main thread creates a Dispatcher process, which has a channel that accepts ScrapeRequests. In order to
// allow concurrent requests, the Dispatcher will fanout requests to various workers - there is 1 worker per
// domain, and each fanout worker is responsible for making sure only 10(ish) requests are made a second to
// a given host.
//
// fill in response handling
// --(url, follow_links) ---> Dispatcher [--> Worker -->] ResProcessor --(loop)
func main() {
	p := NewFanOutProcessor(WorkerFactory(workerFactory))
	fanout := NewDispatcher(p)

	respChan := make(chan ScrapeResponse)
	apiClient := NewAPIClient("http://localhost:8080", nil)
	respSender := NewQueueProcessorWithWorkers(NewResponseSender(apiClient), 10)

	kafkaConsumer, err := NewKafkaConsumer([]string{"localhost:9092"}, "scrapeReports")
	if err != nil {
		log.Fatalf("Failed setting up consumer: %v", err)
	}

	go kafkaConsumer.start()

	respSender.Start()

	go func() {
		for {
			select {
			case resp := <-respChan:
				log.Printf("Dispatch ScrapeResponse for " + resp.URL.String())
				respSender.Process(resp)
			case req := <-kafkaConsumer.OutputChan:
				log.Printf("Recieved ScrapeRequest from Kafka for " + req.url.String())
				req.respChan = respChan
				fanout.ReqChan() <- req
			}
		}
	}()

	fanout.Start()
}
