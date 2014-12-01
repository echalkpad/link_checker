package com.eogren.link_checker.service_layer.api;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Link {
    private String url;
    private String anchorText;

    public Link() {}

    /**
     * Creates a new Link object with all parameters.
     * @param url URL the link points to
     * @param anchorText Anchor text of the link
     */
    public Link(
        String url,
        String anchorText
    ) {
        this.url = url;
        this.anchorText = anchorText;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Link link = (Link) o;

        if (!anchorText.equals(link.anchorText)) return false;
        if (!url.equals(link.url)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = url.hashCode();
        result = 31 * result + anchorText.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Link{" +
                "url='" + url + '\'' +
                ", anchorText='" + anchorText + '\'' +
                '}';
    }

    @JsonProperty
    public String getUrl() {
        return url;
    }

    @JsonProperty
    public String getAnchorText() {
        return anchorText;
    }
}
