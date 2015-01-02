package com.eogren.link_checker.scheduler;

import com.eogren.link_checker.messaging.consumer.ScraperMessageProcessor;
import com.eogren.link_checker.protobuf.ScraperMessages;
import com.eogren.link_checker.scheduler.commands.Command;
import com.eogren.link_checker.scheduler.commands.ScheduleScrapeIfNecessaryCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;

public class ScrapeUpdateProcessor extends ScraperMessageProcessor {
    final static protected Logger logger = LoggerFactory.getLogger(ScraperMessageProcessor.class);

    private final BlockingQueue<Command> commandQueue;

    public ScrapeUpdateProcessor(BlockingQueue<Command> commandQueue) {
        this.commandQueue = commandQueue;
    }

    public void consumeScrapeUpdate(ScraperMessages.ScrapeUpdate msg) {
        logger.debug("Processing ScrapeUpdate message for " + msg.getNewStatus().getUrl());

        for (ScraperMessages.Link l : msg.getNewStatus().getLinksList()) {
            logger.debug("Queuing ScrapeIfNecessary " + l.getUrl());
            try {
                commandQueue.put(new ScheduleScrapeIfNecessaryCommand(l.getUrl()));
            } catch (InterruptedException e) {
                logger.error("Interrupted while queuing message, continuing");
            }
        }
    }
}
