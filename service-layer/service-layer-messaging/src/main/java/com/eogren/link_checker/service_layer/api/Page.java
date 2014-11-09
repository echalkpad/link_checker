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
    private LinkStatus linkStatus;

    /**
     * Overall status for a monitored page.
     *
     * BROKEN_LINKS: Some broken links have been detected on the page.
     * NOT_FULLY_CRAWLED: No broken links have been detected, but not all of the links have
     * been crawled yet.
     * NO_BROKEN_LINKS: All is well with the page.
     */
    public enum LinkStatus {
        BROKEN_LINKS,
        NOT_FULLY_CRAWLED,
        NO_BROKEN_LINKS
    };

    @JsonProperty
    public boolean getIsMonitored() {
        return isMonitored;
    }

    @JsonProperty
    public CrawlReport getLastCrawled() {
        return lastCrawled;
    }

    @JsonProperty
    public String getLinkStatus() {
        return linkStatus.toString();
    }

    @JsonProperty
    public void setLinkStatus(String linkStatus) {
        this.linkStatus = LinkStatus.valueOf(linkStatus);
    }

    /**
     * Creates a new Page object initialized to defaults.
     */
    public Page() {
    }

    /**
     * Create a new Page object from scratch. This version takes
     * the variables that are not set by the system.
     * @param url URL of Page
     * @param isMonitored Is this page monitored?
     */
    public Page(String url,
                boolean isMonitored
    ) {
        this.url = url;
        this.isMonitored = isMonitored;

        this.linkStatus = LinkStatus.NOT_FULLY_CRAWLED;
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
