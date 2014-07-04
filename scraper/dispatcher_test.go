package main

import (
	"testing"
	"time"
)

type mockProcessor struct {
	quitCalled bool
	requests   []ScrapeRequest
}

func (m *mockProcessor) ProcessQuit() {
	m.quitCalled = true
}

func (m *mockProcessor) ProcessRequest(r *ScrapeRequest) {
	m.requests = append(m.requests, *r)
}

func newMockProcessor() *mockProcessor {
	return &mockProcessor{false, make([]ScrapeRequest, 0, 5)}
}

func TestDispatcherCanQuit(t *testing.T) {
	done := make(chan bool, 1)
	p := newMockProcessor()
	d := NewDispatcher(p)

	go func() {
		d.Start()
		done <- true
	}()

	d.QuitChan() <- true

	select {
	case <-done:
		if !p.quitCalled {
			t.Errorf("Dispatcher did not call ProcessQuit() on exit")
		}
		return
	case <-time.After(5 * time.Second):
		t.Fatal("Dispatcher failed to quit 5 seconds after quitc sent")
	}
}

func TestDispatcherCallsProcessRequest(t *testing.T) {
	done := make(chan bool, 1)
	p := newMockProcessor()
	d := NewDispatcher(p)

	go func() {
		d.Start()

		done <- true
	}()

	req := ScrapeRequest{}
	d.ReqChan() <- req
	time.Sleep(time.Second)
	d.QuitChan() <- true

	select {
	case <-done:
		if len(p.requests) != 1 {
			t.Errorf("Expected processRequest() to have been called once but len was %d", len(p.requests))
		}
		return
	case <-time.After(5 * time.Second):
		t.Fatal("Dispatcher failed to quit 5 seconds after quitc sent")
	}
}
