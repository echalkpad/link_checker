package com.eogren.link_checker.service_layer.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.wordnik.swagger.annotations.ApiModel;
import com.wordnik.swagger.annotations.ApiModelProperty;
import org.hibernate.validator.constraints.URL;

import javax.validation.constraints.NotNull;

@ApiModel(value="MonitoredPages are crawled by the system every 30 minutes.")
public class MonitoredPage {
    @NotNull
    @URL
    private String url;

    @JsonProperty
    @ApiModelProperty(value="URL of the monitored page", required=true)
    public String getUrl() {
        return url;
    }

    public MonitoredPage() {

    }

    public MonitoredPage(String url) {
        this.url = url;
    }
}
