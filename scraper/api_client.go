package main

import (
	"encoding/json"
	"fmt"
	"net/url"
)

// APIClient is an interface that queries the REST API for information
// and can report results
type APIClient interface {
	// GetRootPages requests the list of root pages to crawl from the server
	GetRootPages() ([]*url.URL, error)
}

type apiClientImpl struct {
	baseURL   string
	retriever WebRetriever
}

type rootPage struct {
	URL string `json:"url"`
}

// NewAPIClient creates a new api client using the given baseURL (root location
// of the service API) and webRetriever to actually make web requests. If webRetriever
// is null, a default one will be created.
func NewAPIClient(baseURL string, webRetriever WebRetriever) APIClient {
	r := webRetriever
	if r == nil {
		r = NewWebRetriever()
	}
	return &apiClientImpl{baseURL: baseURL, retriever: r}
}

func (c *apiClientImpl) GetRootPages() ([]*url.URL, error) {
	reqURL := c.baseURL + "/api/v1/root_page"

	r, sc, err := c.retriever.GetURL(reqURL, 5000000)
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
