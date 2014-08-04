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

    @JsonProperty
    public String getUrl() {
        return url;
    }

    @JsonProperty
    public String getAnchorText() {
        return anchorText;
    }
}
