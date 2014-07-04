package main

import (
	"net/url"
)

// The Dispatcher is basically a task distributor; it receives requests to scrape various URLs,
// and is responsible for dispatching them (and possibly creating) to the correct workers.
//
// Currently there is 1 worker per domain so that the crawl request can be easily throttled.
// Note: I assume that the cost of goroutines/workers is so low that there is no need to clean up workers
// if they haven't been needed for a while.
//
// XXX Is making this an interface overkill?
type Dispatcher interface {
	Start()
	ReqChan() chan ScrapeRequest
	QuitChan() chan bool
}

// RequestProcessor must be implemented by callers of NewDispatcher
type RequestProcessor interface {
	ProcessRequest(*ScrapeRequest)
	ProcessQuit()
}

type dispatcherImpl struct {
	reqc  chan ScrapeRequest
	stopc chan bool

	processor RequestProcessor
}

// NewDispatcher creates a new dispatcher object ready for use
func NewDispatcher(rp RequestProcessor) Dispatcher {
	return &dispatcherImpl{reqc: make(chan ScrapeRequest, 50), stopc: make(chan bool), processor: rp}
}

// Start starts the dispatcher loop. It will end if it receives any request on stopc.
func (d *dispatcherImpl) Start() {
	for {
		select {
		case req := <-d.reqc:
			d.processor.ProcessRequest(&req)
		case <-d.stopc:
			d.processor.ProcessQuit()
			return
		}
	}
}

func (d *dispatcherImpl) ReqChan() chan ScrapeRequest {
	return d.reqc
}

func (d *dispatcherImpl) QuitChan() chan bool {
	return d.stopc
}

// ScrapeRequest is used to describe a request to the Dispatcher
type ScrapeRequest struct {
	url         *url.URL
	followLinks bool
	respChan    chan ScrapeResponse
}

// ScrapeResponse is a response to a ScrapeRequest
type ScrapeResponse struct {
	url    *url.URL
	status int
}
