package com.eogren.link_checker.status_updater.config;

import com.eogren.link_checker.service_layer.config.KafkaConfig;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class KafkaConfiguration extends KafkaConfig {
    @NotNull
    @Min(1)
    @Max(16)
    protected int numThreads;

    public int getNumThreads() {
        return numThreads;
    }
}
