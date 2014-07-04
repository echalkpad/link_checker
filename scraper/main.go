// scraper is a web scraper.
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

//
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
