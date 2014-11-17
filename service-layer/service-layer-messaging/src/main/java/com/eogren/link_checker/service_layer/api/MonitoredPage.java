package com.eogren.link_checker.service_layer.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.wordnik.swagger.annotations.ApiModel;
import com.wordnik.swagger.annotations.ApiModelProperty;
import org.hibernate.validator.constraints.URL;

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

    @JsonProperty
    @ApiModelProperty(value="URL of the monitored page", required=true)
    public String getUrl() {
        return url;
    }

    @JsonProperty
    @ApiModelProperty(value="Crawl status for the page", required=true)
    public Status getStatus() { return status; }

    public MonitoredPage() {

    }

    public MonitoredPage(String url) {
        this.url = url;
    }
}
