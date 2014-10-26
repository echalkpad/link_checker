/*The scraper command is a worker process that can check various URLs to make sure they still return content,
and is also capable of discovering links on a page so they can be scraped in the future.

The scraper leverages goroutines to scrape multiple domains concurrently.
*/
package main

import (
	"log"
	"net/url"
	"time"
)

func webProcessFactory() RequestProcessor {
	return &WebProcessor{retriever: NewWebClient(), extractor: NewLinkExtractor()}
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
	respSender := NewQueueProcessorWithWorkers(NewResponseSender(apiClient), 10)

	respSender.Start()

	var timer = time.After(1 * time.Millisecond)

	go func() {
		for {
			select {
			case resp := <-respChan:
				log.Printf("Dispatch ScrapeResponse for " + resp.URL.String())
				respSender.Process(resp)

				if resp.Depth > 0 {
					// XXX: Need to clean out uniqueURLs or else new scrapes will never be scheduled
					uniqueURLs := make(map[string]bool)
					for _, l := range resp.Links {
						uniqueURLs[l.URL] = true
					}

					for strURL := range uniqueURLs {
						parsedURL, err := url.Parse(strURL)
						if err != nil {
							log.Printf("Failed to parse %s, skipping", strURL)
							continue
						}

						req := ScrapeRequest{
							url:      parsedURL,
							depth:    resp.Depth - 1,
							respChan: respChan,
						}

						log.Printf("Dispatch ScrapeRequest for %s\n", strURL)
						fanout.ReqChan() <- req
					}
				}
			case <-timer:
				urls, err := apiClient.GetRootPages()

				if err != nil {
					log.Printf("Unable to retrieve root pages from API: %v", err)
					timer = time.After(scrapeInterval * 2 * time.Minute)
					continue
				}

				for _, url := range urls {
					req := ScrapeRequest{
						url:      url,
						depth:    1,
						respChan: respChan,
					}
					log.Printf("Dispatch ScrapeRequest for %s\n", url)
					fanout.ReqChan() <- req
				}

				timer = time.After(scrapeInterval * time.Minute)
			}
		}
	}()

	fanout.Start()
}
