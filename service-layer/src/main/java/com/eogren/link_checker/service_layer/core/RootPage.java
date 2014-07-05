package com.eogren.link_checker.service_layer.core;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Root Page is a representation of a page to be crawled every 5 minutes.
 */
public class RootPage {
    private String url;

    public RootPage() {

    }

    public RootPage(String url) {
        this.url = url;
    }

    @JsonProperty
    public String getUrl() {
        return url;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RootPage rootPage = (RootPage) o;

        if (url != null ? !url.equals(rootPage.url) : rootPage.url != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return url != null ? url.hashCode() : 0;
    }
}