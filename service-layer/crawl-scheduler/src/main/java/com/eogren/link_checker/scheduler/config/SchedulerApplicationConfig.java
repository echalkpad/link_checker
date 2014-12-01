package com.eogren.link_checker.scheduler.config;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.ws.rs.DefaultValue;

public class SchedulerApplicationConfig {
    @NotNull
    @Valid
    protected KafkaConfiguration kafkaConfig;

    @NotNull
    @Valid
    protected DataApiConfig dataApiConfig;

    @Min(1000)
    @Max(120000)
    @DefaultValue("30000")
    protected long monitoredPageInterval;

    @JsonProperty("kafka")
    public KafkaConfiguration getKafkaConfig() {
        return kafkaConfig;
    }

    @JsonProperty("data_api")
    public DataApiConfig getDataApiConfig() {
        return dataApiConfig;
    }

    @JsonProperty("mp_interval")
    public long getMonitoredPageInterval() {
        return monitoredPageInterval;
    }
}
