package main

import (
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
// a given second.
// Params:
//  maxTokens = maximum number of requests a second to allow
//  rpFactory = request processor factory - each new request will be passed to a new instance of a request processor
func NewBucketDispatcher(maxTokens int, rpfactory RequestProcessorFactory) *BucketDispatcher {
	return &BucketDispatcher{maxTokens, maxTokens, make([]*ScrapeRequest, 0, 5), rpfactory, make(chan ScrapeRequest, 50), make(chan bool)}
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

}

func (w *WebProcessor) ProcessQuit() {
	// do nothing
}
