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
      <p><a href="http://www.complicated.com">This is a <b>link</b> with <i>some</i> images inside</p></a></p>
   </body>
 </html>
`

var invalidHTML = `
  <!DOCTYPE html>
  <html>
    <head><title>Weird nested links</title></head>
    <body>
      What happens <a href="http://www.link1.com">if you <a href="http://www.link2.com">do this?</a></a>
    </body>
 </html>
`

func TestCanExtractLinksFromPage(t *testing.T) {
	links, warnings, err := ExtractLinksFromPage("http://www.foo.com", strings.NewReader(basicHTML))
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

}

func TestReturnsErrorOnRandomPage(t *testing.T) {

}

func checkLink(t *testing.T, l Link, expectedHref string, expectedAnchor string) {
	if l.URL != expectedHref || l.anchorText != expectedAnchor {
		t.Errorf("Expected link to be href %s, anchor %s, instead was %s, anchor %s",
			expectedHref, expectedAnchor, l.URL, l.anchorText)
	}
}
