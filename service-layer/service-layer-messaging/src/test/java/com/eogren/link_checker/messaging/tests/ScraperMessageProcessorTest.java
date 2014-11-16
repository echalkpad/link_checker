package com.eogren.link_checker.messaging.tests;

import com.eogren.link_checker.messaging.consumer.ScraperMessageProcessor;
import com.eogren.link_checker.protobuf.ScraperMessages;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class ScraperMessageProcessorTest
{
    protected LoggingProcessor consumer;

    public class LoggingProcessor extends ScraperMessageProcessor {
        public boolean requestSeen;
        public boolean responseSeen;
        public boolean updateSeen;

        public LoggingProcessor() {
            this.requestSeen = false;
            this.responseSeen = false;
            this.updateSeen = false;
        }

        @Override
        public void consumeScrapeResponse(ScraperMessages.ScrapeResponse msg) {
            super.consumeScrapeResponse(msg);
            responseSeen = true;
        }

        @Override
        public void consumeScrapeRequest(ScraperMessages.ScrapeRequest msg) {
            super.consumeScrapeRequest(msg);
            requestSeen = true;
        }

        @Override
        public void consumeScrapeUpdate(ScraperMessages.ScrapeUpdate msg) {
            super.consumeScrapeUpdate(msg);
            updateSeen = true;
        }
    }

    @Before
    public void createConsumer() {
        consumer = new LoggingProcessor();
    }

    @Test
    public void testScrapeRequest() {
        ScraperMessages.ScraperMessage.Builder b = ScraperMessages.ScraperMessage.newBuilder();
        ScraperMessages.ScraperMessage msg =
                b.setType(ScraperMessages.ScraperMessage.Type.SCRAPE_REQUEST)
                 .setRequest(ScraperMessages.ScrapeRequest.newBuilder().setUrl("http://www.cnn.com").build())
                 .build();

        consumer.consumeScraperMessage(msg);
        assertTrue("Expected scraper request be processed", consumer.requestSeen);
    }

    @Test
    public void testScrapeResponse() {
        ScraperMessages.ScraperMessage.Builder b = ScraperMessages.ScraperMessage.newBuilder();
        ScraperMessages.ScraperMessage msg =
                b.setType(ScraperMessages.ScraperMessage.Type.SCRAPE_RESPONSE)
                        .setResponse(buildScrapeResponse())
                        .build();

        consumer.consumeScraperMessage(msg);
        assertTrue("Expected scraper response be processed", consumer.responseSeen);
    }

    @Test
    public void testScrapeUpdate() {
        ScraperMessages.ScraperMessage.Builder b = ScraperMessages.ScraperMessage.newBuilder();
        ScraperMessages.ScraperMessage msg =
                b.setType(ScraperMessages.ScraperMessage.Type.SCRAPE_UPDATE)
                        .setUpdate(ScraperMessages.ScrapeUpdate.newBuilder()
                                    .setNewStatus(buildScrapeResponse())
                                    .build())
                        .build();

        consumer.consumeScraperMessage(msg);
        assertTrue("Expected scraper update be processed", consumer.updateSeen);
    }

    protected ScraperMessages.ScrapeResponse buildScrapeResponse() {
        return ScraperMessages.ScrapeResponse.newBuilder()
                .setUrl("http://www.cnn.com")
                .setStatus(true)
                .setStatusMessage("OK")
                .build();
    }
}
