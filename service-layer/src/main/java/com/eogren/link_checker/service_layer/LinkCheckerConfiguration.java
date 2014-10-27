package com.eogren.link_checker.service_layer;

import com.eogren.link_checker.service_layer.config.CassandraClusterFactory;
import com.eogren.link_checker.service_layer.config.KafkaConfig;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

public class LinkCheckerConfiguration extends Configuration {
    @Valid
    @NotNull
    private CassandraClusterFactory cassandraFactory = new CassandraClusterFactory();

    @Valid
    @NotNull
    @Pattern(regexp="cassandra|inmemory")
    private String repoType = "cassandra";

    @Valid
    @NotNull
    private KafkaConfig kafkaConfig;

    @JsonProperty("cassandra")
    public CassandraClusterFactory getCassandraFactory() {
        return cassandraFactory;
    }

    @JsonProperty("cassandra")
    public void setCassandraFactory(CassandraClusterFactory factory) {
        cassandraFactory = factory;
    }

    @JsonProperty("kafka")
    public KafkaConfig getKafkaConfig() { return kafkaConfig; }

    @JsonProperty("kafka")
    public void setKafkaConfig(KafkaConfig config) { kafkaConfig = config; }
    @JsonProperty
    public String getRepoType() {
        return repoType;
    }

    @JsonProperty
    public void setRepoType(String repoType) {
        this.repoType = repoType;
    }
}
