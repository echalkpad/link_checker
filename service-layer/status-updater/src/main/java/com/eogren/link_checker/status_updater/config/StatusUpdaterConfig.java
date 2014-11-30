package com.eogren.link_checker.status_updater.config;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.ws.rs.DefaultValue;

public class StatusUpdaterConfig {
    @NotNull
    @Valid
    protected KafkaConfiguration kafkaConfig;

    @NotNull
    @Valid
    protected DataApiConfig dataApiConfig;

    @NotNull
    @Min(1)
    @Max(8)
    protected int numThreads;

    @JsonProperty("num_threads")
    public int getNumThreads() {
        return numThreads;
    }

    @JsonProperty("kafka")
    public KafkaConfiguration getKafkaConfig() {
        return kafkaConfig;
    }

    @JsonProperty("data_api")
    public DataApiConfig getDataApiConfig() {
        return dataApiConfig;
    }
}
