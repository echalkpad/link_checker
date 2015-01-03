package com.eogren.link_checker.scheduler;

import com.eogren.link_checker.scheduler.commands.*;
import com.eogren.link_checker.service_layer.api.MonitoredPage;
import com.eogren.link_checker.service_layer.client.ApiClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The CommandExecutor sets up a single thread that processes several different types of
 * messages.
 */
public class CommandExecutor {
    protected final Logger logger = LoggerFactory.getLogger(CommandExecutor.class);

    protected final ApiClient apiClient;

    protected final BlockingQueue<Command> inputQueue;
    protected final BlockingQueue<Command> outputQueue;

    protected final long monitoredPageInterval;
    protected final long scrapeInterval = 1000;

    protected final AtomicBoolean stop;

    private final PriorityQueue<ScheduledScrape> scheduledRequests;
    private final Set<String> scrapedRecently;

    protected Thread mainWorker;
    protected Thread mpTimerWorker;
    protected Thread srTimerWorker;

    public CommandExecutor(ApiClient apiClient, long monitoredPageInterval, BlockingQueue<Command> outputQueue) {
        this.apiClient = apiClient;
        this.monitoredPageInterval = monitoredPageInterval;

        this.scheduledRequests = new PriorityQueue<>();
        this.scrapedRecently = new HashSet<>();

        this.inputQueue = new ArrayBlockingQueue<>(100);
        this.outputQueue = outputQueue;
        this.stop = new AtomicBoolean(false);

    }

    public BlockingQueue<Command> getInputQueue() {
        return inputQueue;
    }

    public void start() {
        if (mainWorker != null) {
            throw new IllegalStateException("Can't start the CommandExecutor while it's already running");
        }

        mainWorker = new Thread(this::mainWorker, "CommandExecutor-mainWorker");
        mainWorker.start();

        if (mpTimerWorker != null) {
            throw new IllegalStateException("Can't start the mpTimerWorker while it's running");
        }

        mpTimerWorker = new Thread(this::mpTimerWorker, "CommandExecutor-mpTimerWorker");
        mpTimerWorker.start();

        if (srTimerWorker != null) {
            throw new IllegalStateException("Can't start the srTimerWorker while it's running");
        }

        srTimerWorker = new Thread(this::srTimerWorker, "CommandExecutor-srTimerWorker");
        srTimerWorker.start();
    }

    public void stop() {
        if (mainWorker == null) {
            return;
        }

        stop.set(true);

        mainWorker.interrupt();
        mpTimerWorker.interrupt();
        try {
            mainWorker.join(2000);
            mpTimerWorker.join(2000);
        } catch (InterruptedException e) {
            logger.debug("Interrupted waiting for workers to die");
        } finally {
            mainWorker = null;
            mpTimerWorker = null;
        }
    }

    public void mainWorker() {
        logger.info("mainWorker: start");
        while (!stop.get()) {
            try {
                Command c = inputQueue.take();

                if (c instanceof ProduceScrapeRequestsCommand) {
                    produceScrapeRequests();
                } else if (c instanceof ScheduleScrapeIfNecessaryCommand) {
                    scheduleScrapesIfNecessary((ScheduleScrapeIfNecessaryCommand) c);
                } else {
                    logger.warn(String.format("Unknown message type %s received, skipping", c.getClass().getName()));
                }
            } catch (InterruptedException e) {
                continue;
            }
        }
        logger.info("mainWorker: stop");
    }

    public void mpTimerWorker() {
        logger.info("mpTimerWorker: start");
        while (!stop.get()) {
            try {
                checkMonitoredPages();
                Thread.sleep(monitoredPageInterval);
            } catch (InterruptedException e) {
                // continue
            }
        }
        logger.info("mpTimerWorker: stop");
    }

    public void srTimerWorker() {
        logger.info("srTimerWorker: start");
        while (!stop.get()) {
            try {
                inputQueue.put(new ProduceScrapeRequestsCommand());
                Thread.sleep(scrapeInterval);
            } catch (InterruptedException e) {
                // ignore
            }
        }
    }

    public class ScheduledScrape implements Comparable<ScheduledScrape> {
        private final long when;
        private final String url;

        public ScheduledScrape(long when, String url) {
            this.when = when;
            this.url = url;
        }

        public long getWhen() {
            return when;
        }

        public String getUrl() {
            return url;
        }

        @Override
        public int compareTo(ScheduledScrape o) {
            if (when < o.when) return -1;
            if (when > o.when) return 1;
            return 0;
        }

        @Override
        public String toString() {
            return "ScheduledScrape{" +
                    "when=" + when +
                    ", url='" + url + '\'' +
                    '}';
        }


    }

    private void checkMonitoredPages() {
        try {
            logger.info("Retrieving monitored pages and scheduling scrapes");
            List<MonitoredPage> mps = apiClient.retrieveAllMonitoredPages();
            scrapedRecently.clear();
            for (MonitoredPage p : mps) {
                inputQueue.put(new ScheduleScrapeIfNecessaryCommand(p.getUrl()));
            }
        } catch (IOException e) {
            logger.warn("IOException while retrieving monitored pages: " + e.getMessage());
        } catch (InterruptedException e) {
            logger.warn("Interrupted trying to place msg in blocking queue!");
        }
    }

    private void scheduleScrapesIfNecessary(ScheduleScrapeIfNecessaryCommand c) {
        logger.debug("Checking to see if scrape necessary for " + c.getUrl());

        if (!scrapeScheduled(c.getUrl())) {
            ScheduledScrape scrape = new ScheduledScrape(System.currentTimeMillis(), c.getUrl());
            scrapedRecently.add(c.getUrl());
            scheduledRequests.add(scrape);
            logger.info("Added scrape to queue: " + scrape.toString());
        } else {
            logger.debug("Scrape already scheduled, ignoring");
        }
    }

    private void produceScrapeRequests() {
        long now = System.currentTimeMillis();

        while (!scheduledRequests.isEmpty() &&
                scheduledRequests.peek().getWhen() <= now) {
            ScheduledScrape s = scheduledRequests.poll();

            try {
                outputQueue.put(new SendScrapeRequestCommand(s.getUrl()));
                logger.info("Sending scrape request for " + s.getUrl());
            } catch (InterruptedException e) {
                logger.warn("Interrupted while trying to send scrape request!");
            }
        }
    }

    private boolean scrapeScheduled(String url) {
        return scrapedRecently.contains(url);
    }
}
