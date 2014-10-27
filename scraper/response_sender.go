package main

import (
	"log"
	"time"
)

// ResponseSender is responsible for taking a ScrapeResponse and posting it to the data API.
type ResponseSender struct {
	apiClient APIClient
}

// NewResponseSender initializes a new ResponseSender with an appropriate APIClient.
func NewResponseSender(apiClient APIClient) *ResponseSender {
	return &ResponseSender{apiClient: apiClient}
}

// Process processes incoming messages. They must be a ScrapeResponse or pointer to one.
func (rs *ResponseSender) Process(msg interface{}) {
	var scrapeResponse *ScrapeResponse

	switch t := msg.(type) {
	default:
		log.Panicf("ResponseSender.Process got unknown message type %T", t)
	case ScrapeResponse:
		scrapeResponse = &t
	case *ScrapeResponse:
		scrapeResponse = t
	}

	numErrors := 0
	backoff := 2
	var err error

	for numErrors < 3 {
		err = rs.apiClient.SubmitScrapeResponse(scrapeResponse)
		if err == nil {
			return
		}

		numErrors++
		time.Sleep(time.Duration(backoff) * time.Second)
		backoff *= backoff
	}

	log.Printf("Failed to post scrape response! Error %v", err)
}
