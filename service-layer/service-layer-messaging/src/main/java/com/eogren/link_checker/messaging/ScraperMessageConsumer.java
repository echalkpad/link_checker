package com.eogren.link_checker.messaging;

import com.eogren.link_checker.protobuf.ScraperMessages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScraperMessageConsumer {

    public final Logger logger = LoggerFactory.getLogger(ScraperMessageConsumer.class);

    public void consumeScraperMessage(ScraperMessages.ScraperMessage msg) {
        logger.debug("Consuming msg type %s", msg.getType().toString());

        switch (msg.getType()) {
        case SCRAPE_REQUEST:
            consumeScrapeRequest(msg.getRequest());
            break;
        case SCRAPE_RESPONSE:
            consumeScrapeResponse(msg.getResponse());
            break;
        case SCRAPE_UPDATE:
            consumeScrapeUpdate(msg.getUpdate());
            break;
        default:
            logger.warn("Unknown message type %s, dropping", msg.getType().toString());
        }
    }

    /** Intended to be overridden by consumers **/
    public void consumeScrapeResponse(ScraperMessages.ScrapeResponse msg) {}
    public void consumeScrapeRequest(ScraperMessages.ScrapeRequest msg) {}
    public void consumeScrapeUpdate(ScraperMessages.ScrapeUpdate msg) {}
}
