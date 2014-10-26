package main

import (
	"log"
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

	if len(b.queue) > 0 {
		log.Printf("After drainQueue, %d items waiting for a timer to fire", len(b.queue))
	}

	// GC queue since the slice could grow without bound
	if cap(b.queue) > 50 {
		tmpQ := make([]*ScrapeRequest, len(b.queue))
		copy(tmpQ, b.queue)
		b.queue = tmpQ
	}
}

// Move this into its own file?

// WebProcessor is the worker that actually processes ScrapeRequests. It relies
// on the WebClient and LinkExtractors that it has been initialized with.
type WebProcessor struct {
	retriever WebClient
	extractor LinkExtractor
}

// NewWebProcessor creates a new WebProcessor.
func NewWebProcessor(r WebClient, e LinkExtractor) WebProcessor {
	return WebProcessor{retriever: r, extractor: e}
}

func (w *WebProcessor) ProcessRequest(r *ScrapeRequest) {
	resp := ScrapeResponse{URL: r.url, Depth: r.depth}

	body, statusCode, err := w.retriever.GetURL(r.url.String(), 2000000)
	resp.Status = statusCode
	resp.Date = time.Now()

	if statusCode < 200 || statusCode > 299 || err != nil {
		resp.Err = err
		r.respChan <- resp
		return
	}

	links, warnings, err := w.extractor.ExtractLinksFromPage(r.url.String(), body)
	resp.Links = links
	resp.Warnings = warnings
	resp.Err = err

	r.respChan <- resp
}

func (w *WebProcessor) ProcessQuit() {
	// do nothing
}
