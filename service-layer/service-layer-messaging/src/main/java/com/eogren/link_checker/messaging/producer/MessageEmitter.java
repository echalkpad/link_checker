package com.eogren.link_checker.messaging.producer;

import com.eogren.link_checker.protobuf.ScraperMessages;
import com.eogren.link_checker.service_layer.api.CrawlReport;

import java.util.Optional;

public interface MessageEmitter {
    public void emitMessage(String topic, String partition_key, byte[] raw_data);

    /**
     * Send a crawl report
     * @param oldCrawlReport Last crawl report for the URL [null if this is the first time it's been seen]
     * @param newCrawlReport New crawl report for the URL
     */
    public void notifyCrawlReport(Optional<CrawlReport> oldCrawlReport, CrawlReport newCrawlReport);

    /**
     * Emit a ScrapeRequest for the given URL.
     * @param url URL to request
     */
    public void emitScrapeRequest(String url);
}
