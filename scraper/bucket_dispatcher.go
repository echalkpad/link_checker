package main

import (
	"fmt"
	"io/ioutil"
	"net/http"
	"time"
)

// RequestProcessorFactory is a function that will create a RequestProcessor on demand.
type RequestProcessorFactory func() RequestProcessor

// BucketDispatcher is an object that makes sure to make only a certain number of requests a second.
type BucketDispatcher struct {
	tokens    int
	maxTokens int

	queue []*ScrapeRequest

	rpfactory RequestProcessorFactory

	reqc  chan ScrapeRequest
	stopc chan bool
}

// NewBucketDispatcher creates a new bucket dispatcher that will ensure only max_tokens requests are in flight
// a given second. By default it will queue up to 1 minute's worth of requests.
// Params:
//  maxTokens = maximum number of requests a second to allow
//  rpFactory = request processor factory - each new request will be passed to a new instance of a request processor
func NewBucketDispatcher(maxTokens int, rpfactory RequestProcessorFactory) *BucketDispatcher {
	return &BucketDispatcher{maxTokens, maxTokens, make([]*ScrapeRequest, 0, 5), rpfactory, make(chan ScrapeRequest, 60*maxTokens), make(chan bool)}
}

// Start starts the event loop for a BucketDispatcher. This function will block waiting for and processing requests
// until it receives something on its QuitChan().
func (b *BucketDispatcher) Start() {
	timer := time.After(time.Duration(time.Second / time.Duration(b.maxTokens)))
	for {
		select {
		case req := <-b.reqc:
			b.queue = append(b.queue, &req)
			b.drainQueue()
		case <-b.stopc:
			return
		case <-timer:
			if b.tokens < b.maxTokens {
				b.tokens++
				b.drainQueue()
			}

			timer = time.After(time.Duration(time.Second / time.Duration(b.maxTokens)))
		}
	}
}

// ReqChan returns the channel that processes ScrapeRequests.
func (b *BucketDispatcher) ReqChan() chan ScrapeRequest {
	return b.reqc
}

// QuitChan returns the channel that processes quit requests.
func (b *BucketDispatcher) QuitChan() chan bool {
	return b.stopc
}

func (b *BucketDispatcher) drainQueue() {
	for len(b.queue) > 0 && b.tokens > 0 {
		req := b.queue[0]
		p := b.rpfactory()
		go p.ProcessRequest(req)
		b.queue = b.queue[1:]
		b.tokens--
	}

	// GC queue since the slice could grow without bound
	if cap(b.queue) > 50 {
		tmpQ := make([]*ScrapeRequest, len(b.queue))
		copy(tmpQ, b.queue)
		b.queue = tmpQ
	}
}

// Move this into its own file?

type WebProcessor struct {
}

func (w *WebProcessor) ProcessRequest(r *ScrapeRequest) {
	client := &http.Client{Timeout: 30 * time.Second}
	resp := &ScrapeResponse{url: r.url, depth: r.depth, links: make([]*Link, 0, 10)}

	httpResp, err := client.Get(r.url.String())
	if err != nil {
		resp.err = err
		resp.status = -1
		r.respChan <- *resp
		return
	}

	body, err := readBody(httpResp, 2000000)
	if err != nil {
		fmt.Printf("Error! %v", err)
	} else {
		fmt.Printf("Body! %v", string(body))
	}
}

func (w *WebProcessor) ProcessQuit() {
	// do nothing
}

func readBody(r *http.Response, maxLength int64) ([]byte, error) {
	defer r.Body.Close()

	if r.ContentLength > maxLength {
		return nil, fmt.Errorf("Discarding because content length is greater than 2MB (%d)", r.ContentLength)
	}

	// If content-length is -1, for now assume these processes complete fast enough
	// that consuming 2MB per request is fine
	cappedR := NewAtMostNReader(2000000, r.Body)
	return ioutil.ReadAll(cappedR)
}
