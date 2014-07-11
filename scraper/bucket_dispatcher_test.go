package main

import (
	"sync/atomic"
	"testing"
	"time"
)

type MockProcessor struct {
	counter *int32
}

func (m *MockProcessor) ProcessRequest(*ScrapeRequest) {
	atomic.AddInt32(m.counter, 1)
}
func (m *MockProcessor) ProcessQuit() {}

func newMockProcessorFactory(counter *int32) RequestProcessorFactory {
	return func() RequestProcessor {
		return &MockProcessor{counter}
	}
}

func TestRateLimitsAppropriately(t *testing.T) {
	t.Parallel()

	var counter int32
	respChan := make(chan ScrapeResponse)
	done := make(chan bool)

	var sleepTime int32 = 2
	var rps int32 = 2
	var expectedLow = sleepTime * rps
	var expectedHigh = (sleepTime + 1) * rps

	p := NewBucketDispatcher(int(rps), RequestProcessorFactory(newMockProcessorFactory(&counter)))

	go func() {
		p.Start()
		done <- true
	}()

	go func() {
		for i := 0; i < 50; i++ {
			p.ReqChan() <- ScrapeRequest{nil, 0, respChan}
		}
		time.Sleep(time.Duration(sleepTime) * time.Second)
		p.QuitChan() <- true
	}()

	<-done
	if counter < expectedLow || counter > expectedHigh {
		t.Errorf("Expected dispatcher to have processed %d-%d requests, was %d", expectedLow, expectedHigh, counter)
	}
}
