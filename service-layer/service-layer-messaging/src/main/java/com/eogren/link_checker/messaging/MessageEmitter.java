package com.eogren.link_checker.messaging;

import com.eogren.link_checker.protobuf.ScraperMessages;
import com.eogren.link_checker.service_layer.api.CrawlReport;

import java.util.Optional;

public interface MessageEmitter {
    public void emitMessage(String topic, String partition_key, byte[] raw_data);

    public void notifyCrawlReport(Optional<CrawlReport> oldCrawlReport, CrawlReport newCrawlReport);
}
