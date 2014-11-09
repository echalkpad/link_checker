package com.eogren.link_checker.service_layer.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import javax.ws.rs.DefaultValue;

public class KafkaConfig {
    @NotEmpty
    protected String zkAddress;

    protected String prefix;

    @JsonProperty("broker")
    public String getZkAddress() { return zkAddress; }

    @JsonProperty("prefix")
    public String getPrefix() { return prefix; }
}
