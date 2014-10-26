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
	quitc      chan bool
	reqc       chan interface{}
	processor  MessageProcessor
	numWorkers int
	wg         sync.WaitGroup
}

// NewQueueProcessor creates a new QueueProcessor with 1 worker.
func NewQueueProcessor(p MessageProcessor) QueueProcessor {
	return NewQueueProcessorWithWorkers(p, 1)
}

// NewQueueProcessorWithWorkers creates a new QueueProcessor with the given number of workers.
func NewQueueProcessorWithWorkers(p MessageProcessor, numWorkers int) QueueProcessor {
	if numWorkers <= 0 {
		panic("NewQueueProcessorWithWorkers: worker count must be at least 1")
	}

	return &defaultQueueProcessor{
		quitc:      make(chan bool),
		reqc:       make(chan interface{}, 20),
		processor:  p,
		numWorkers: numWorkers,
	}
}

// Start starts the QueueProcessor. This function blocks so should probably be called from its own goroutine.
func (r *defaultQueueProcessor) Start() {
	r.wg.Add(r.numWorkers)
	for i := 0; i < r.numWorkers; i++ {
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
}

// Stop stops the QueueProcessor.
func (r *defaultQueueProcessor) Stop() {
	close(r.quitc)
	for i := 0; i < r.numWorkers; i++ {
		r.wg.Wait()
	}
}

func (r *defaultQueueProcessor) Process(msg interface{}) {
	r.reqc <- msg
}
