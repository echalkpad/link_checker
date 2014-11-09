package com.eogren.link_checker.status_updater.config;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class StatusUpdaterConfig {
    @NotNull
    @Valid
    protected KafkaConfiguration kafkaConfig;

    @JsonProperty("kafka")
    public KafkaConfiguration getKafkaConfig() {
        return kafkaConfig;
    }
}
