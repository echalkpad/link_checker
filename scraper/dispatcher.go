package main

import (
	"fmt"
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

// WorkerFactory is a function that creates a specific instance of a Dispatcher.
type WorkerFactory func() Dispatcher

type fanOutProcessor struct {
	workers       map[string]Dispatcher
	workerFactory WorkerFactory
}

// NewFanOutProcessor creates a RequestProcessor that just forwards the request on to a worker
// routine [creating one if necessary].
func NewFanOutProcessor(wf WorkerFactory) RequestProcessor {
	return &fanOutProcessor{workers: make(map[string]Dispatcher), workerFactory: wf}
}

func (d *fanOutProcessor) ProcessRequest(req *ScrapeRequest) {
	w, ok := d.workers[req.url.Host]
	if !ok {
		w = d.workerFactory()
		d.workers[req.url.Host] = w
		go w.Start()
	}

	w.ReqChan() <- *req
}

func (d *fanOutProcessor) ProcessQuit() {
	for _, worker := range d.workers {
		worker.QuitChan() <- true
	}
}

// ScrapeRequest is used to describe a request to the Dispatcher
type ScrapeRequest struct {
	url      *url.URL
	depth    int
	respChan chan ScrapeResponse
}

// ScrapeResponse is a response to a ScrapeRequest. URL and Depth should be copied
// from the ScrapeRequest and are used so the requester doesn't need to keep any state
// around.
type ScrapeResponse struct {
	URL   *url.URL
	Depth int

	Status   int
	Err      error
	Links    []*Link
	Warnings []string
}

func (r *ScrapeResponse) Dump() {
	fmt.Printf("Response for: %s\n", r.URL.String())
	fmt.Printf("Status code: %d\n", r.Status)
	fmt.Printf("Error: %v\n", r.Err)
	fmt.Printf("Links:\n")
	for _, l := range r.Links {
		fmt.Printf("%s (%s)\n", l.URL, l.AnchorText)
	}

	fmt.Printf("Warnings:\n")
	for _, w := range r.Warnings {
		fmt.Printf("%s\n", w)
	}
}
