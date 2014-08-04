package main

import (
	"bytes"
	"fmt"
	"io"
	"io/ioutil"
	"net/http"
	"time"
)

// WebClient is a client that can retrieve a URL body and POST to a given URL.
type WebClient interface {
	GetURL(url string, maxLength int64) (r io.Reader, statusCode int, err error)
	PostURL(url string, body io.Reader) (statusCode int, err error)
	SetTimeout(d time.Duration)
}

// NewWebClient returns a new WebClient.
func NewWebClient() WebClient {
	return &webClientDefault{client: &http.Client{Timeout: 30 * time.Second}}
}

type webClientDefault struct {
	client *http.Client
}

func (w *webClientDefault) SetTimeout(d time.Duration) {
	w.client.Timeout = d
}

func (w *webClientDefault) GetURL(url string, maxLength int64) (r io.Reader, statusCode int, err error) {
	httpResp, err := w.client.Get(url)
	if err != nil {
		return nil, -1, err
	}

	defer httpResp.Body.Close()

	// We read the entire body into a buffer now to avoid potential problems with timeouts later
	body, err := readBody(httpResp, maxLength)

	return bytes.NewReader(body), httpResp.StatusCode, err
}

func (w *webClientDefault) PostURL(url string, body io.Reader) (statusCode int, err error) {
	httpResp, err := w.client.Post(url, "application/json", body)
	if err != nil {
		return -1, err
	}

	defer httpResp.Body.Close()

	return httpResp.StatusCode, err
}

func readBody(r *http.Response, maxLength int64) ([]byte, error) {
	defer r.Body.Close()

	if r.ContentLength > maxLength {
		return nil, fmt.Errorf("Discarding because content length is greater than 2MB (%d)", r.ContentLength)
	}

	// If content-length is -1, for now assume these processes complete fast enough
	// that consuming 2MB per request is fine
	cappedR := NewAtMostNReader(maxLength, r.Body)
	return ioutil.ReadAll(cappedR)
}
