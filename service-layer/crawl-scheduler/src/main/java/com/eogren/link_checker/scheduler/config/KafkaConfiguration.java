package com.eogren.link_checker.scheduler.config;

import com.eogren.link_checker.service_layer.config.KafkaConfig;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.ws.rs.DefaultValue;

public class KafkaConfiguration extends KafkaConfig {
    @NotNull
    @Min(1)
    @Max(16)
    protected int numThreads;

    @NotNull
    @DefaultValue("crawlscheduler")
    protected String consumerGroup;

    public String getConsumerGroup() {
        return consumerGroup;
    }

    public int getNumThreads() {
        return numThreads;
    }
}
