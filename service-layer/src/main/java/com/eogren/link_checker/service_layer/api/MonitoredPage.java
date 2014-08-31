package com.eogren.link_checker.service_layer.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.URL;

import javax.validation.constraints.NotNull;

public class MonitoredPage {
    @NotNull
    @URL
    private String url;

    @JsonProperty
    public String getUrl() {
        return url;
    }

    public MonitoredPage() {

    }

    public MonitoredPage(String url) {
        this.url = url;
    }
}
