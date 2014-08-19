package com.eogren.link_checker.service_layer.messaging;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Basic message with no other properties. Intended to be subclassed
 * for actual serialization/deserialization.
 */
public class BaseMessage {
    @JsonProperty
    public String getType() {
        return this.getClass().getName();
    }

    /**
     * Create a new BaseMessage
     */
    public BaseMessage() {
    }
}
