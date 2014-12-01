package com.eogren.link_checker.messaging.producer;

import com.eogren.link_checker.service_layer.api.CrawlReport;

import java.util.Optional;

public class NullKafkaProducer implements MessageEmitter {
    @Override
    public void emitMessage(String topic, String partition_key, byte[] raw_data) {
        // intentionally empty
    }

    @Override
    public void notifyCrawlReport(Optional<CrawlReport> oldCrawlReport, CrawlReport newCrawlReport) {
        // intentionally empty
    }

    @Override
    public void emitScrapeRequest(String url) {
        // intentionally empty
    }
}

