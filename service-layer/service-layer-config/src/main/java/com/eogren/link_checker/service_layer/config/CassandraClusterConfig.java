package com.eogren.link_checker.service_layer.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;

import java.util.List;

public class CassandraClusterConfig {
    @NotEmpty
    protected List<String> nodes;

    @NotEmpty
    protected String keyspace;


    @JsonProperty
    public List<String> getNodes() {
        return nodes;
    }

    @JsonProperty
    public String getKeyspace() {
        return keyspace;
    }

    @JsonProperty
    public void setNodes(List<String> nodes) {
        this.nodes = nodes;
    }
}
