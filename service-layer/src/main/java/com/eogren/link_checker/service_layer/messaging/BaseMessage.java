package com.eogren.link_checker.service_layer.messaging;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Basic message with no other properties. Intended to be subclassed
 * for actual serialization/deserialization.
 */
public class BaseMessage {
    @JsonProperty
    protected String type;

    public String getType() {
        return type;
    }

    /**
     * Create a new BaseMessage
     */
    public BaseMessage() {
        type = this.getClass().getName();

    }
}
