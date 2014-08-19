package com.eogren.link_checker.service_layer.messaging;

import com.eogren.link_checker.service_layer.api.CrawlReport;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CrawlStatusMessage extends BaseMessage {
    protected CrawlReport crawlReport;

    public CrawlStatusMessage(CrawlReport report) {
        super();

        this.crawlReport = report;
    }

    @JsonProperty
    public CrawlReport getCrawlReport() {
        return crawlReport;
    }
}
