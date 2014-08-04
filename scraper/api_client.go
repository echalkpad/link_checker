package main

import (
	"bytes"
	"encoding/json"
	"fmt"
	"net/url"
	"time"
)

// APIClient is an interface that queries the REST API for information
// and can report results
type APIClient interface {
	// GetRootPages requests the list of root pages to crawl from the server
	GetRootPages() ([]*url.URL, error)

	// SubmitScrapeResponse submits a ScrapeResponse to the server
	SubmitScrapeResponse(sr *ScrapeResponse) error
}

type apiClientImpl struct {
	baseURL string
	client  WebClient
}

type rootPage struct {
	URL string `json:"url"`
}

// crawlReport translates a ScrapeResponse into the CrawlReport specified by the server API.
// (unsurprisingly they are basically the same thing)
type crawlReport struct {
	URL        string    `json:"url"`
	StatusCode int       `json:"statusCode"`
	Links      []*Link   `json:"links"`
	Date       time.Time `json:"date"`
	Error      string    `json:"error"`
}

// NewAPIClient creates a new api client using the given baseURL (root location
// of the service API) and WebClient to actually make web requests. If WebClient
// is null, a default one will be created.
func NewAPIClient(baseURL string, client WebClient) APIClient {
	r := client
	if r == nil {
		r = NewWebClient()
	}
	return &apiClientImpl{baseURL: baseURL, client: r}
}

func (c *apiClientImpl) GetRootPages() ([]*url.URL, error) {
	reqURL := c.baseURL + "/api/v1/root_page"

	r, sc, err := c.client.GetURL(reqURL, 5000000)
	if err != nil {
		return nil, err
	}

	if sc != 200 {
		return nil, fmt.Errorf("Call to API returned %d", sc)
	}

	dec := json.NewDecoder(r)

	var pages []rootPage
	err = dec.Decode(&pages)

	if err != nil {
		return nil, err
	}

	urls := make([]*url.URL, 0, 50)
	for _, rp := range pages {
		url, err := url.Parse(rp.URL)
		if err != nil {
			// XXX log
			continue
		}
		urls = append(urls, url)
	}
	return urls, nil
}

func (c *apiClientImpl) SubmitScrapeResponse(sr *ScrapeResponse) error {
	reqURL := c.baseURL + "/api/v1/crawl_report"

	report := crawlReportFromScrapeResponse(sr)

	body, err := json.Marshal(report)
	if err != nil {
		return err
	}

	sc, err := c.client.PostURL(reqURL, bytes.NewReader(body))
	if err != nil {
		return err
	}

	if sc >= 400 {
		return fmt.Errorf("POST for %s was not success: returned %d", body, sc)
	}

	return nil
}

func crawlReportFromScrapeResponse(sr *ScrapeResponse) *crawlReport {
	resp := crawlReport{
		URL:        sr.URL.String(),
		StatusCode: sr.Status,
		Links:      sr.Links,
		Date:       sr.Date,
	}

	if resp.Links == nil {
		resp.Links = make([]*Link, 0, 1)
	}

	if sr.Err != nil {
		resp.Error = fmt.Sprintf("%v", sr.Err)
	}

	return &resp
}
