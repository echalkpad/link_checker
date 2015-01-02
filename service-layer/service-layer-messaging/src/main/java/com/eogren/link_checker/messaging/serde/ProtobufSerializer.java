package com.eogren.link_checker.messaging.serde;

import com.eogren.link_checker.protobuf.ScraperMessages;
import com.eogren.link_checker.service_layer.api.CrawlReport;
import com.eogren.link_checker.service_layer.api.Link;
import com.google.protobuf.InvalidProtocolBufferException;

import java.util.Optional;

public class ProtobufSerializer {
    /**
     * Serialize a CrawlReport into its proto buf representation.
     * @param crawlReport Crawl Report to serialize
     * @return Protobuf object
     */
    public static ScraperMessages.ScrapeResponse crawlReportToProtobuf(CrawlReport crawlReport) {
        ScraperMessages.ScrapeResponse.Builder b =
                ScraperMessages.ScrapeResponse.newBuilder()
                    .setUrl(crawlReport.getUrl())
                    .setStatus(crawlReport.getStatusCode() == 200)
                    .setStatusCode(crawlReport.getStatusCode())
                    .setStatusMessage(crawlReport.getError());

        for (Link l : crawlReport.getLinks()) {
            b.addLinks(linkToProtobuf(l));
        }

        return b.build();
    }

    private static ScraperMessages.Link linkToProtobuf(Link l) {
        return ScraperMessages.Link.newBuilder()
                .setUrl(l.getUrl())
                .setAnchorText(l.getAnchorText())
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

    /**
     * Deserialize a scraper message from a byte stream
     * @param rawData Byte stream to decode
     * @throws java.lang.IllegalArgumentException If the byte stream cannot be properly decoded
     * @return Decoded ScraperMessage
     */
    public static ScraperMessages.ScraperMessage deserializeScraperMessage(byte[] rawData) {
        try {
            return ScraperMessages.ScraperMessage.parseFrom(rawData);
        } catch (InvalidProtocolBufferException e) {
            throw new IllegalArgumentException("Bad bytestream; could not deserialize message: " + e.toString());
        }
    }

    /**
     * Create a scrape request for a given URL
     */
    public static byte[] createScrapeRequest(String url) {
        ScraperMessages.ScrapeRequest.Builder b = ScraperMessages.ScrapeRequest.newBuilder();
        b.setUrl(url);

        return ScraperMessages.ScraperMessage.newBuilder()
                .setType(ScraperMessages.ScraperMessage.Type.SCRAPE_REQUEST)
                .setRequest(b.build())
                .build()
                .toByteArray();
    }
}
