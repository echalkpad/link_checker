package main

import (
	"sync"
)

// QueueProcessor is an interface that sits on its own goroutine, and will pass messages to its processor
// until told to stop. Golang has no generics so assumption is that the processing function will bail out
// if it gets passed a weird message.
type QueueProcessor interface {
	Start()
	Stop()

	Process(msg interface{})
}

// MessageProcessor is an interface that a message processor implements
type MessageProcessor interface {
	Process(msg interface{})
}

type defaultQueueProcessor struct {
	quitc     chan bool
	reqc      chan interface{}
	processor MessageProcessor
	wg        sync.WaitGroup
}

func NewQueueProcessor(p MessageProcessor) QueueProcessor {
	return &defaultQueueProcessor{
		quitc:     make(chan bool),
		reqc:      make(chan interface{}, 20),
		processor: p,
	}
}

// Start starts the QueueProcessor. This function blocks so should probably be called from its own goroutine.
func (r *defaultQueueProcessor) Start() {
	r.wg.Add(1)
	go func() {
		for {
			select {
			case <-r.quitc:
				r.wg.Done()
				return
			case req := <-r.reqc:
				r.processor.Process(req)
			}
		}
	}()
}

// Stop stops the QueueProcessor.
func (r *defaultQueueProcessor) Stop() {
	r.quitc <- true
	r.wg.Wait()
}

func (r *defaultQueueProcessor) Process(msg interface{}) {
	r.reqc <- msg
}
