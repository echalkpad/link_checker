package main

import (
	"bytes"
	"fmt"
	"io"
	"io/ioutil"
	"net/http"
	"time"
)

// WebRetriever is an interface that retrieves the contents of a given url. It will read at most
// maxLength bytes.
type WebRetriever interface {
	GetURL(url string, maxLength int64) (r io.Reader, statusCode int, err error)
}

// NewWebRetriever returns the default impl of a WebRetriever.
func NewWebRetriever() WebRetriever {
	return &webRetrieverDefault{}
}

type webRetrieverDefault struct{}

func (w *webRetrieverDefault) GetURL(url string, maxLength int64) (r io.Reader, statusCode int, err error) {
	client := &http.Client{Timeout: 30 * time.Second}
	httpResp, err := client.Get(url)

	defer httpResp.Body.Close()

	// We read the entire body into a buffer now to avoid potential problems with timeouts later
	body, err := readBody(httpResp, maxLength)

	return bytes.NewReader(body), httpResp.StatusCode, err
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