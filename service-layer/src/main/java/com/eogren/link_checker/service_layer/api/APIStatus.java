package com.eogren.link_checker.service_layer.api;

import com.fasterxml.jackson.annotation.JsonProperty;

public class APIStatus {
    private boolean success;
    private String description;

    public APIStatus(boolean success, String description) {
        this.success = success;
        this.description = description;
    }

    @JsonProperty
    public boolean getSuccess() {
        return success;
    }

    @JsonProperty
    public String getDescription() {
        return description;
    }
}
