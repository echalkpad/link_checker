package com.eogren.link_checker.service_layer.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.URL;

import javax.validation.constraints.NotNull;

/**
 * Root Page is a representation of a page to be crawled every 5 minutes.
 */
public class Page {
    @NotNull
    @URL
    private String url;

    private boolean isMonitored;

    private CrawlReport lastCrawled;


    @JsonProperty
    public boolean isMonitored() {
        return isMonitored;
    }

    @JsonProperty
    public CrawlReport getLastCrawled() {
        return lastCrawled;
    }

    /**
     * Creates a new Page object initialized to defaults.
     */
    public Page() {
    }

    public Page(String url,
                boolean isMonitored,
                CrawlReport lastCrawled
    ) {
        this.url = url;
        this.isMonitored = isMonitored;
        this.lastCrawled = lastCrawled;
    }

    /**
     * Retrieves the URL of the root object.
     */
    @JsonProperty
    public String getUrl() {
        return url;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Page page = (Page) o;

        if (url != null ? !url.equals(page.url) : page.url != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return url != null ? url.hashCode() : 0;
    }
}
