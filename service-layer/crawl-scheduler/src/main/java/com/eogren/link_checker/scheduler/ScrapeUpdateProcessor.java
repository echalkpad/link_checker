package com.eogren.link_checker.scheduler;

import com.eogren.link_checker.messaging.consumer.ScraperMessageProcessor;
import com.eogren.link_checker.protobuf.ScraperMessages;
import com.eogren.link_checker.scheduler.commands.Command;
import com.eogren.link_checker.scheduler.commands.ScheduleScrapeIfNecessaryCommand;
import com.eogren.link_checker.service_layer.client.ApiClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;

public class ScrapeUpdateProcessor extends ScraperMessageProcessor {
    final static protected Logger logger = LoggerFactory.getLogger(ScraperMessageProcessor.class);

    private final BlockingQueue<Command> commandQueue;
    private final ApiClient apiClient;

    public ScrapeUpdateProcessor(BlockingQueue<Command> commandQueue, ApiClient apiClient) {
        this.commandQueue = commandQueue;
        this.apiClient = apiClient;
    }

    public void consumeScrapeUpdate(ScraperMessages.ScrapeUpdate msg) {
        logger.debug("Processing ScrapeUpdate message for " + msg.getNewStatus().getUrl());

        try {
            // TODO this could probably be cached in the message if this call gets expensive
            if (!apiClient.getMonitoredPage(msg.getNewStatus().getUrl()).isPresent()) {
                logger.debug("ScrapeUpdate is not for MonitoredPage, ignoring");
                return;
            }
        } catch (IOException e) {
            logger.debug("IOException trying to check monitored page status; assuming " + msg.getNewStatus().getUrl() + " is an MP to be safe");
        }

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
