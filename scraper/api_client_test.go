package main

import (
	"io"
	"io/ioutil"
	"net/url"
	"strings"
	"testing"
	"time"
)

type mockWebClient struct {
	resp     string
	sc       int
	lastURL  string
	lastPOST string
}

func (r *mockWebClient) GetURL(url string, maxLength int64) (io.Reader, int, error) {
	r.lastURL = url
	return strings.NewReader(r.resp), r.sc, nil
}

func (r *mockWebClient) PostURL(url string, body io.Reader) (int, error) {
	r.lastURL = url
	bytes, err := ioutil.ReadAll(body)
	r.lastPOST = string(bytes)

	if err != nil {
		panic("Unexpected failure while reading body")
	}
	return 200, nil
}

func (r *mockWebClient) SetTimeout(t time.Duration) {

}

func TestApiClientCanParseRootPages(t *testing.T) {
	r := &mockWebClient{resp: `[{"url": "http://www.cnn.com"}, {"url": "http://www.nytimes.com"}]`, sc: 200}
	c := NewAPIClient("http://unittest", r)

	expectedURL := "http://unittest/api/v1/root_page"
	expectedLinks := make(map[string]bool)
	expectedLinks["http://www.cnn.com"] = true
	expectedLinks["http://www.nytimes.com"] = true

	urls, err := c.GetRootPages()
	if err != nil {
		t.Fatalf("Expected GetRootPage to have no error, instead %v", err)
	}

	if r.lastURL != expectedURL {
		t.Errorf("Expected last retrieved URL to be %s, was %s", expectedURL, r.lastURL)
	}

	if len(urls) != len(expectedLinks) {
		t.Errorf("Expected len(root page links) to be %d, was %d", len(urls), len(expectedLinks))
	}

	for _, url := range urls {
		_, ok := expectedLinks[url.String()]
		if !ok {
			t.Errorf("Expected %s to be found in expected URL list, was not", url.String())
		}
	}
}

func TestApiClientCanPostCrawlReports(t *testing.T) {
	r := &mockWebClient{sc: 200}
	c := NewAPIClient("http://unittest", r)

	expectedURL := "http://unittest/api/v1/crawl_report"
	expectedBody := `{"url":"http://www.eogren.com","statusCode":200,"links":[{"url":"http://www.link1.com","anchorText":"Link One"},{"url":"http://www.link2.com","anchorText":"Link Two"}],"date":"2014-01-05T00:00:00Z","error":""}`

	url, _ := url.Parse("http://www.eogren.com")

	resp := &ScrapeResponse{URL: url, Status: 200, Links: make([]*Link, 0, 2), Date: time.Date(2014, 01, 05, 00, 00, 00, 00, time.UTC)}
	resp.Links = append(resp.Links, &Link{URL: "http://www.link1.com", AnchorText: "Link One"})
	resp.Links = append(resp.Links, &Link{URL: "http://www.link2.com", AnchorText: "Link Two"})

	err := c.SubmitScrapeResponse(resp)
	if err != nil {
		t.Errorf("Expected SubmitScrapeResponse to have no error, instead %v", err)
	}

	if r.lastURL != expectedURL {
		t.Errorf("Expected last URL to be %s, was %s", expectedURL, r.lastURL)
	}

	if r.lastPOST != expectedBody {
		t.Errorf("Expected last POST to be %s, was %s", expectedBody, r.lastPOST)
	}

}
