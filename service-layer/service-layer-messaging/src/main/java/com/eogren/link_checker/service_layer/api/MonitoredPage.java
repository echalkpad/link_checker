package com.eogren.link_checker.service_layer.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.joda.ser.DateTimeSerializer;
import com.wordnik.swagger.annotations.ApiModel;
import com.wordnik.swagger.annotations.ApiModelProperty;
import org.hibernate.validator.constraints.URL;
import org.joda.time.DateTime;

import javax.validation.constraints.NotNull;
import javax.ws.rs.DefaultValue;

@ApiModel(value="MonitoredPages are crawled by the system every 30 minutes.")
public class MonitoredPage {


    public enum Status {
        UNKNOWN,
        ERROR,
        GOOD
    }

    @NotNull
    @URL
    private String url;

    @DefaultValue("UNKNOWN")
    private Status status;

    private DateTime lastUpdated;

    @JsonProperty
    @ApiModelProperty(value="URL of the monitored page", required=true)
    public String getUrl() {
        return url;
    }

    @JsonProperty
    @ApiModelProperty(value="Crawl status for the page", required=true)
    public Status getStatus() { return status; }


    @JsonProperty
    @JsonSerialize(using=DateTimeSerializer.class)
    @ApiModelProperty(value="Time status was last updated")
    public DateTime getLastUpdated() { return lastUpdated; }

    public MonitoredPage() {

    }


    public MonitoredPage(String url) {
        this(url, Status.UNKNOWN);
    }

    public MonitoredPage(String url, Status status) {
        this(url, status, DateTime.now());
    }

    public MonitoredPage(String url, Status status, DateTime lastUpdated) {
        this.url = url;
        this.status = status;
        this.lastUpdated = lastUpdated;
    }

    @Override
    public String toString() {
        return "MonitoredPage{" +
                "url='" + url + '\'' +
                ", status=" + status +
                ", lastUpdated=" + lastUpdated +
                '}';
    }
}
