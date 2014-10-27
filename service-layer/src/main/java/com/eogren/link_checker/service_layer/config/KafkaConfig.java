package com.eogren.link_checker.service_layer.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;

import javax.ws.rs.DefaultValue;

public class KafkaConfig {
    @NotEmpty
    @DefaultValue("http://localhost:9092")
    protected String zkAddress;

    @JsonProperty("broker")
    public String getZkAddress() { return zkAddress; }
}
