package main

import (
	"net/url"
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
	t.Parallel()

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

func TestFanOutDispatcherCreatesWorkerProcesses(t *testing.T) {
	numCreations := 0
	factory := func() Dispatcher {
		numCreations++
		return NewDispatcher(newMockProcessor())
	}

	url, _ := url.Parse("http://www.cnn.com")

	d := NewFanOutProcessor(WorkerFactory(factory))
	d.ProcessRequest(&ScrapeRequest{url: url})
	if numCreations != 1 {
		t.Errorf("Expected numCreations to be 1, is %d", numCreations)
	}
}

func TestFanOutDispatcherLimitsOneWorkerProcessPerHost(t *testing.T) {
	t.Parallel()

	numCreations := 0
	processors := make([]*mockProcessor, 0, 5)

	factory := func() Dispatcher {
		numCreations++
		processor := newMockProcessor()

		processors = append(processors, processor)
		return NewDispatcher(processor)
	}

	url1, _ := url.Parse("http://www.cnn.com")
	url2, _ := url.Parse("http://www.cnn.com/some_article")
	url3, _ := url.Parse("http://www.nytimes.com")

	d := NewFanOutProcessor(WorkerFactory(factory))
	d.ProcessRequest(&ScrapeRequest{url: url1})
	d.ProcessRequest(&ScrapeRequest{url: url2})
	d.ProcessRequest(&ScrapeRequest{url: url3})
	time.Sleep(time.Second)
	d.ProcessQuit()

	if numCreations != 2 {
		t.Errorf("Expected numCreations to be 2, is %d", numCreations)
	}

	urls := make([]*url.URL, 0, 5)

	for _, processor := range processors {
		for _, req := range processor.requests {
			urls = append(urls, req.url)
		}
	}

	expectedURLs := []*url.URL{url1, url2, url3}
	for _, expectedURL := range expectedURLs {
		if !inList(expectedURL, urls) {
			t.Errorf("Expected to find URL %v in list but did not", expectedURL)
		}
	}
}

func inList(expectedURL *url.URL, urlList []*url.URL) bool {
	for _, url := range urlList {
		if expectedURL == url {
			return true
		}
	}

	return false
}
