package main

import (
	"io"

	"code.google.com/p/go.net/html"
	"code.google.com/p/go.net/html/atom"
	"fmt"
	"net/url"
)

// Link represents a scraped link from a webpage.
type Link struct {
	URL        string
	anchorText string
}

// LinkExtractor is an interface representing an object that knows how to extract
// links from a page.
type LinkExtractor interface {
	ExtractLinksFromPage(baseURLS string, r io.Reader) ([]Link, []string, error)
}

type linkExtractorDefault struct{}

// NewLinkExtractor creates a new LinkExtractor with the default implementation.
func NewLinkExtractor() LinkExtractor {
	return &linkExtractorDefault{}
}

/*ExtractLinksFromPage will scrape a webpage searching for any clickable links.
 * Params:
 *  baseURL: Is the URL of the page from the reader. Any relative links will be resolved from
 *  this base URL.
 *  r: Reader that has the contents of the webpage.
 *
 * Returns:
 * an array of Links on success,
 * an array of string warnings for links that could not be parsed,
 * an error if the page could not be parsed or other fatal errors
 */
func (l *linkExtractorDefault) ExtractLinksFromPage(baseURLS string, r io.Reader) ([]Link, []string, error) {
	const (
		SearchForLink = iota
		InsideLink
	)

	tokenizer := html.NewTokenizer(r)
	links := make([]Link, 0, 5)
	warnings := make([]string, 0, 5)
	state := SearchForLink

	baseURL, err := url.Parse(baseURLS)
	if err != nil {
		return nil, warnings, err
	}

	var pendingLink Link

	for {
		tokenType := tokenizer.Next()
		if tokenType == html.ErrorToken {
			err := tokenizer.Err()
			if err != io.EOF {
				return nil, warnings, err
			}

			break
		}

		token := tokenizer.Token()
		switch state {
		case SearchForLink:
			if (tokenType != html.StartTagToken && tokenType != html.SelfClosingTagToken) || token.DataAtom != atom.A {
				continue
			}

			href, warning := getHref(&token, baseURL)
			if warning != "" {
				warnings = append(warnings, warning)
				continue
			}

			pendingLink = Link{href, ""}

			if tokenType == html.SelfClosingTagToken {
				links = append(links, pendingLink)
				continue
			}

			pendingLink.anchorText = ""
			state = InsideLink
		case InsideLink:
			if tokenType == html.EndTagToken && token.DataAtom == atom.A {
				links = append(links, pendingLink)
				state = SearchForLink
				pendingLink = Link{"", ""}
				continue
			}

			if tokenType == html.TextToken {
				pendingLink.anchorText = (pendingLink.anchorText + token.Data)
			}
		}

	}

	return links, warnings, nil
}

func getHref(t *html.Token, baseURL *url.URL) (ret string, warning string) {
	attr, err := getAttribute(t, "href")
	if err != nil {
		return "", fmt.Sprintf("Could not retrieve href for token %v, continuing", t)
	}

	href, err := url.Parse(attr)

	if err != nil {
		return "", fmt.Sprintf("Could not parse href for token %v, continuing", t)
	}

	return baseURL.ResolveReference(href).String(), ""
}

func getAttribute(t *html.Token, attr string) (string, error) {
	for _, a := range t.Attr {
		if a.Key == attr {
			return a.Val, nil
		}
	}

	return "", fmt.Errorf("Attr %s not found in %v", attr, t)
}
