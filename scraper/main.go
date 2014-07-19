/*The scraper command is a worker process that can check various URLs to make sure they still return content,
and is also capable of discovering links on a page so they can be scraped in the future.

The scraper leverages goroutines to scrape multiple domains concurrently.
*/
package main

import (
	"log"
	"time"
)

func webProcessFactory() RequestProcessor {
	return &WebProcessor{retriever: NewWebRetriever(), extractor: NewLinkExtractor()}
}

func workerFactory() Dispatcher {
	return NewBucketDispatcher(10, RequestProcessorFactory(webProcessFactory))
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
	scrapeInterval := time.Duration(5)

	respChan := make(chan ScrapeResponse)
	apiClient := NewAPIClient("http://localhost:8080", nil)

	var timer = time.After(1 * time.Millisecond)

	go func() {
		for {
			select {
			case resp := <-respChan:
				// TODO: Report results
				// TODO: Trigger future scrapes based on depth
				resp.Dump()
			case <-timer:
				urls, err := apiClient.GetRootPages()

				if err != nil {
					log.Printf("Unable to retrieve root pages from API: %v", err)
					timer = time.After(scrapeInterval * 2 * time.Minute)
					continue
				}

				for _, url := range urls {
					req := ScrapeRequest{url, 0, respChan}
					log.Printf("Dispatch ScrapeRequest for %s\n", url)
					fanout.ReqChan() <- req
				}

				timer = time.After(scrapeInterval * time.Minute)
			}
		}
	}()

	fanout.Start()
}
