package com.eogren.link_checker.messaging.serde;

import com.eogren.link_checker.protobuf.ScraperMessages;
import com.eogren.link_checker.service_layer.api.CrawlReport;

import java.util.Optional;

public class ProtobufSerializer {
    /**
     * Serialize a CrawlReport into its proto buf representation.
     * @param crawlReport Crawl Report to serialize
     * @return Protobuf object
     */
    public static ScraperMessages.ScrapeResponse crawlReportToProtobuf(CrawlReport crawlReport) {
        return ScraperMessages.ScrapeResponse.newBuilder()
                .setUrl(crawlReport.getUrl())
                .setStatus(crawlReport.getStatusCode() == 200)
                .setStatusMessage("Test")
                .addLinks("http://www.cnn.com")
                .build();
    }


    /**
     * Create and serialize a scrape update message from the old and new crawl reports
     * @param oldCrawlReport old crawl report
     * @param newCrawlReport new crawl report
     * @return byte array of message
     */
    public static byte[] createScrapeUpdate(Optional<CrawlReport> oldCrawlReport, CrawlReport newCrawlReport) {
        ScraperMessages.ScrapeUpdate.Builder b = ScraperMessages.ScrapeUpdate.newBuilder();
        b.setNewStatus(ProtobufSerializer.crawlReportToProtobuf(newCrawlReport));
        if (oldCrawlReport.isPresent()) {
            b.setOldStatus(ProtobufSerializer.crawlReportToProtobuf(oldCrawlReport.get()));
        }

        return ScraperMessages.ScraperMessage.newBuilder()
                .setType(ScraperMessages.ScraperMessage.Type.SCRAPE_UPDATE)
                .setUpdate(b.build())
                .build()
                .toByteArray();
    }
}