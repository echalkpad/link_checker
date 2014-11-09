package com.eogren.link_checker.service_layer.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.joda.ser.DateTimeSerializer;
import org.hibernate.validator.constraints.URL;
import org.joda.time.DateTime;

import javax.validation.constraints.NotNull;
import java.util.List;

public class CrawlReport {
    @NotNull
    @URL
    private String url;

    private int statusCode;

    @NotNull
    @JsonSerialize(using=DateTimeSerializer.class)
    private DateTime date;

    @NotNull
    private List<Link> links;

    private String error;

    public CrawlReport() {

    }

    /**
     * Create a new crawl report.
     * @param url URL for report
     * @param date Date crawled
     * @param error Error (null if none)
     * @param statusCode HTTP status code (or -1 if network error)
     * @param links Set of links found on the page
     */
    public CrawlReport(
            String url,
            DateTime date,
            String error,
            int statusCode,
            List<Link> links
    ) {
        this.url = url;
        this.date = date;
        this.error = error;
        this.statusCode = statusCode;
        this.links = links;
    }

    @JsonProperty
    public String getUrl() {
        return url;
    }

    @JsonProperty
    public List<Link> getLinks() {
        return links;
    }

    @JsonProperty
    public DateTime getDate() {
        return date;
    }

    @JsonProperty
    public int getStatusCode() {
        return statusCode;
    }

    @JsonProperty
    public String getError() {
        return error;
    }
}
