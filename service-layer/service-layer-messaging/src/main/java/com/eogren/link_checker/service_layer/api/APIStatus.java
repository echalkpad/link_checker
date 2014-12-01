package com.eogren.link_checker.service_layer.api;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * API Status is an object that is returned by any API call that either fails
 * or would have no other usual response (eg creation of a new object in the system).
 */
public class APIStatus {
    private boolean success;
    private String description;

    /**
     * Create a new APIStatus object.
     * @param success Indicates whether the operation was successful
     * @param description A human readable description that the API client can use for further debugging.
     */
    public APIStatus(boolean success, String description) {
        this.success = success;
        this.description = description;
    }

    /**
     * Returns the status of the operation.
     */
    @JsonProperty
    public boolean getSuccess() {
        return success;
    }

    /**
     * Returns the human readable description of the operation.
     */
    @JsonProperty
    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return "APIStatus{" +
                "success=" + success +
                ", description='" + description + '\'' +
                '}';
    }
}
