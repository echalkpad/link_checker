package com.eogren.link_checker.service_layer.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;

public class KafkaConfig {
    @NotEmpty
    protected String zkAddress;

    protected String prefix;
    protected String type;

    @NotEmpty
    protected String brokerList;

    @JsonProperty("zookeeper")
    public String getZkAddress() { return zkAddress; }

    @JsonProperty("brokerlist")
    public String getBrokerList() { return brokerList; }

    @JsonProperty("prefix")
    public String getPrefix() { return prefix; }

    @JsonProperty("type")
    public String getType() {
        return (type == null) ? "default" : type;
    }

    public KafkaConfig(String brokerList, String zkAddress, String prefix, String type) {
        this.brokerList = brokerList;
        this.zkAddress = zkAddress;
        this.prefix = prefix;
        this.type = type;
    }
}
