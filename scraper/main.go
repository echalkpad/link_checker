/*The scraper command is a worker process that can check various URLs to make sure they still return content,
and is also capable of discovering links on a page so they can be scraped in the future.

The scraper leverages goroutines to scrape multiple domains concurrently.
*/
package main

import (
	"fmt"
	"net/url"
	"time"
)

// getRootPages() retrieves the root pages to scrape from the service api
func getRootPages() []*url.URL {
	ret := make([]*url.URL, 2)

	url1, _ := url.Parse("http://www.cnn.com")
	ret[0] = url1

	url2, _ := url.Parse("http://www.nytimes.com")
	ret[1] = url2

	return ret
}

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
	var timer = time.After(scrapeInterval * time.Minute)

	go func() {
		for {
			select {
			case resp := <-respChan:
				// TODO: Report results
				// TODO: Trigger future scrapes based on depth
				resp.Dump()
			case <-timer:
				urls := getRootPages()
				for _, url := range urls {
					req := ScrapeRequest{url, 0, respChan}
					fmt.Printf("Dispatch for %s\n", url)
					fanout.ReqChan() <- req
				}

				timer = time.After(scrapeInterval * time.Minute)
			}
		}
	}()

	fanout.Start()
}
