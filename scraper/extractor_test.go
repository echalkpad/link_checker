package main

import (
	"strings"
	"testing"
)

var basicHTML = `
	<!DOCTYPE html>
  <html>
    <head><title>My Title</title></head>
    <body>
      <h1>This is a header</h1>
      <p>I want to go to<a href="http://www.cnn.com">DA CNN</a>
      <p><a href="http://www.complicated.com">This is a <b>link</b> with <i>some</i> images inside</a></p>
   </body>
 </html>
`

var relativeLinkHTML = `
	<!DOCTYPE html>
  <html>
    <head><title>My Title</title></head>
    <body>
      <h1>This is a header</h1>
      <p>I want to go to<a href="/leading-slash">Leading Slash</a>
      <p><a href="../relative">Relative</a></p>
   </body>
 </html>
`

var invalidHTML = `
  This is <b>not a <p>Well-formed</b> PAGE
`

func TestCanExtractLinksFromPage(t *testing.T) {
	e := NewLinkExtractor()
	links, warnings, err := e.ExtractLinksFromPage("http://www.foo.com", strings.NewReader(basicHTML))
	if err != nil {
		t.Fatalf("Expected no error from basicHTML, got %v", err)
	}

	if len(warnings) != 0 {
		t.Errorf("Expected 0 warnings from parsing basicHTML, got %d", len(warnings))
	}

	if len(links) != 2 {
		t.Fatalf("Expected 2 links in basicHTML, found %d", len(links))
	}

	checkLink(t, links[0], "http://www.cnn.com", "DA CNN")
	checkLink(t, links[1], "http://www.complicated.com", "This is a link with some images inside")
}

func TestCanExtractRelativeLinks(t *testing.T) {
	e := NewLinkExtractor()
	links, warnings, err := e.ExtractLinksFromPage("http://www.bar.com/some_dir/some_subdir/", strings.NewReader(relativeLinkHTML))
	if err != nil {
		t.Fatalf("Expected no error from relativeLinkHTML, got %v", err)
	}

	if len(warnings) != 0 {
		t.Errorf("Expected 0 warnings from parsing relativeLinkHTML, got %d", len(warnings))
	}

	if len(links) != 2 {
		t.Fatalf("Expected 2 links in relativeLinkHTML, found %d", len(links))
	}

	checkLink(t, links[0], "http://www.bar.com/leading-slash", "Leading Slash")
	checkLink(t, links[1], "http://www.bar.com/some_dir/relative", "Relative")

}

func TestReturnsErrorOnRandomBasePage(t *testing.T) {
	e := NewLinkExtractor()
	_, _, err := e.ExtractLinksFromPage("1\\/%@!qwertyuiop!", strings.NewReader(invalidHTML))

	if err == nil {
		t.Fatalf("Expected error from errorHTML, got nil")
	}
}

func checkLink(t *testing.T, l *Link, expectedHref string, expectedAnchor string) {
	if l.URL != expectedHref || l.anchorText != expectedAnchor {
		t.Errorf("Expected link to be href %s, anchor %s, instead was %s, anchor %s",
			expectedHref, expectedAnchor, l.URL, l.anchorText)
	}
}
