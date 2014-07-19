package main

import (
	"testing"
	"time"
)

func TestWebRetrieverError(t *testing.T) {
	t.Parallel()

	r := NewWebRetriever()
	r.SetTimeout(1 * time.Millisecond)
	doneChan := make(chan error)
	timerChan := time.After(10 * time.Millisecond)

	go func() {
		_, _, err := r.GetURL("http://localhost:9999", 500000)
		doneChan <- err
	}()

	select {
	case err := <-doneChan:
		if err == nil {
			t.Errorf("Expected to receieve error for bad url, did not")
		}
	case <-timerChan:
		t.Errorf("Expected to receive a response within 1 millisecond but did not")
	}
}
