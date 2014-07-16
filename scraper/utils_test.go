package main

import (
	"bytes"
	"io/ioutil"
	"strings"
	"testing"
)

func TestCanReadAllFromNMostUnderLimit(t *testing.T) {
	longString := "This is a string under the max limit"
	wrapped := strings.NewReader(longString)

	r := NewAtMostNReader(200000, wrapped)

	buf, err := ioutil.ReadAll(r)
	if err != nil {
		t.Errorf("Expected read from a short enough buffer to have no error, instead returned %v", err)
	}

	if !bytes.Equal(buf, []byte(longString)) {
		t.Errorf("Expected buf to be %v, was %v", longString, buf)
	}
}

func TestReadingTooMuchFRomNMostUnderLimitReturnsError(t *testing.T) {
	longString := "This is a string longer than the max limit"
	wrapped := strings.NewReader(longString)
	r := NewAtMostNReader(6, wrapped)

	_, err := ioutil.ReadAll(r)
	if err == nil {
		t.Errorf("Expected read from a buffer that is too large to return error, instead returned nil")
	}

}
