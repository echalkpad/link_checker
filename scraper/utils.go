package main

import (
	"fmt"
	"io"
)

type atMostNReader struct {
	maxBytes  int64
	wrappedR  io.Reader
	readBytes int64
}

// NewAtMostNReader returns a Reader that will return an error if the caller tries to Read more than maxBytes bytes
// out of the stream. It is similar to io.LimitReader but returns an error instead of EOF.
func NewAtMostNReader(maxBytes int64, r io.Reader) io.Reader {
	return &atMostNReader{maxBytes: maxBytes, wrappedR: r}
}

func (r *atMostNReader) Read(p []byte) (int, error) {
	n, err := r.wrappedR.Read(p)

	r.readBytes += int64(n)

	if r.readBytes > r.maxBytes {
		return n, fmt.Errorf("Exceeded max byte count of %d", r.maxBytes)
	}

	if err != nil {
		return n, err
	}

	return n, nil
}
