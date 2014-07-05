/* The scraper command is a worker process that can check various URLs to make sure they still return content,
and is also capable of discovering links on a page so they can be scraped in the future.

The scraper leverages goroutines to scrape multiple domains concurrently.
 */
package main

import (
	//"fmt"
	"net/url"
	//"time"
)

// getRootPages() retrieves the root pages to scrape from the service api
func getRootPages() *url.URL {
	ret, _ := url.Parse("http://www.cnn.com")
	return ret
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
	/*disp := NewDispatcher()
	resp := make(chan ScrapeResponse)
	go func() {
		for {
			url := getRootPages()
			req := ScrapeRequest{url, true, resp}

			disp.ReqChan() <- req
			fmt.Printf("rootPageGoRoutine sleeping for 1\n")
			time.Sleep(1 * time.Minute)
		}
	}()

	disp.Start() */
}
