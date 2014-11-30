package com.eogren.link_checker.scheduler.config;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class SchedulerApplicationConfig {
    @NotNull
    @Valid
    protected KafkaConfiguration kafkaConfig;

    @NotNull
    @Valid
    protected DataApiConfig dataApiConfig;

    @JsonProperty("kafka")
    public KafkaConfiguration getKafkaConfig() {
        return kafkaConfig;
    }

    @JsonProperty("data_api")
    public DataApiConfig getDataApiConfig() {
        return dataApiConfig;
    }
}
