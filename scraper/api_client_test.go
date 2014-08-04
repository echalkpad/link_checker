package main

import (
	"io"
	"strings"
	"testing"
	"time"
)

type mockWebClient struct {
	resp    string
	sc      int
	lastURL string
}

func (r *mockWebClient) GetURL(url string, maxLength int64) (io.Reader, int, error) {
	r.lastURL = url
	return strings.NewReader(r.resp), r.sc, nil
}

func (r *mockWebClient) PostURL(url string, body io.Reader) (int, error) {
	return 400, nil
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
