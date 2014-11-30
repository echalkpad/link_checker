package com.eogren.link_checker.scheduler.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;
import org.hibernate.validator.constraints.URL;

public class DataApiConfig {
    @NotEmpty
    @URL
    protected String dataApiHost;

    @JsonProperty("host")
    public String getDataApiHost() {
        return dataApiHost;
    }
}
