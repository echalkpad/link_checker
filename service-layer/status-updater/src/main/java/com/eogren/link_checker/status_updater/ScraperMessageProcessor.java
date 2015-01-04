package com.eogren.link_checker.status_updater;

import com.eogren.link_checker.protobuf.ScraperMessages;
import com.eogren.link_checker.service_layer.api.CrawlReport;
import com.eogren.link_checker.service_layer.api.MonitoredPage;
import com.eogren.link_checker.service_layer.client.ApiClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ScraperMessageProcessor extends com.eogren.link_checker.messaging.consumer.ScraperMessageProcessor {
    protected static Logger logger = LoggerFactory.getLogger(ScraperMessageProcessor.class);
    private final ApiClient apiClient;
    private final ArrayList<BlockingQueue<MonitoredPage>> workerQueues;
    private final ExecutorService executors;

    public ScraperMessageProcessor(ApiClient apiClient, int numThreads) {
        this.apiClient = apiClient;
        this.workerQueues = new ArrayList<>(numThreads);
        for (int i = 0; i < numThreads; i++) {
            workerQueues.add(new ArrayBlockingQueue<>(50));
        }

        executors = Executors.newFixedThreadPool(numThreads);

        for (BlockingQueue<MonitoredPage> q : this.workerQueues) {
            executors.submit(new MonitoredPageWorker(apiClient, q));
        }
    }

    public void stop() {
        executors.shutdown();
    }

    @Override
    public void consumeScrapeUpdate(ScraperMessages.ScrapeUpdate msg) {
        try {
            // If the scrape update is for a monitored page its list of links may have changed
            // so re-check status.
            //
            // TODO can actually check links as well
            Optional<MonitoredPage> mp = apiClient.getMonitoredPage(msg.getNewStatus().getUrl());
            if (mp.isPresent()) {
                updateMonitoredPageStatus(mp.get());
            }
        } catch (IOException e) {
            logger.warn("Error checking if " + msg.getNewStatus().getUrl() + " is a monitored page; assuming it isn't");
        }

        if (msg.hasOldStatus() &&
            msg.getOldStatus().getStatus() == msg.getNewStatus().getStatus()) {
            logger.debug("ScraperUpdate has same status as before, ignoring.");
            return;
        }

        try {
            List<MonitoredPage> affectedPages = apiClient.getMonitoredPagesThatLinkTo(msg.getNewStatus().getUrl());
            for (MonitoredPage pageToUpdate : affectedPages) {
                // We want to make sure only one thread is responsible for updating a certain MonitoredPage at a time
                // in order to deal with race issues (thread 0 sees MP as OK; thread 1 runs later and sees as bad; but for
                // some reason thread 0's post about page status happens after thread 0).
                //
                // XXX This blockingqueue approach only works if there is one status updater in the system.
                // Separate Kafka topic with partition keys is probably better solution since then even if there are distributed workers they
                // can magically coordinate among themselves.
                updateMonitoredPageStatus(pageToUpdate);
            }
        } catch (IOException e) {
            logger.error("consumeScrapeUpdate: Error processing scraper message for " + msg.getNewStatus().getUrl() + ": " + e.getMessage() + "!", e);
        }
    }

    public void updateMonitoredPageStatus(MonitoredPage p) {
        int queueNo = Math.abs(p.getUrl().hashCode()) % workerQueues.size();
        try {
            workerQueues.get(queueNo).put(p);
        } catch (InterruptedException e) {
            logger.warn("consumeScrapeUpdate: Interrupted while trying to process " + p.getUrl());
        }
    }

    private class MonitoredPageWorker implements Runnable {
        private final BlockingQueue<MonitoredPage> queue;
        private final ApiClient apiClient;

        public MonitoredPageWorker(ApiClient apiClient, BlockingQueue<MonitoredPage> queue) {
            this.apiClient = apiClient;
            this.queue = queue;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    updateMonitoredPageStatus(queue.take());
                } catch (InterruptedException e) {

                }
            }
        }

        /**
         * Update the status of a monitored page
         *
         * @param page MonitoredPage to update
         */
        protected void updateMonitoredPageStatus(MonitoredPage page) {
            // TODO: This is going to eat a lot of unnecessary bandwidth - really only need the URL and status code

            // Get latest crawl reports for URLs that link to MonitoredPage
            // Update status
            logger.debug("Updating status for page " + page.getUrl());

            try {
                List<CrawlReport> reports = apiClient.getLatestCrawlReportsFollowingLinksFor(page.getUrl());

                MonitoredPage.Status pageStatus = MonitoredPage.Status.GOOD;
                if (reports.stream().anyMatch(x -> x.getStatusCode() < 199 || x.getStatusCode() > 299)) {
                    pageStatus = MonitoredPage.Status.ERROR;
                }

                logger.info("Setting status for " + page.getUrl() + " to " + pageStatus.toString());
                apiClient.updateMonitoredPageStatus(page, pageStatus);

            } catch (IOException e) {
                logger.error("updateMonitoredPageStatus: Error updating monitored page status for " + page.getUrl() + ": " + e.getMessage(), e);
            }
        }
    }
}
