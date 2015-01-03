package com.eogren.link_checker.status_updater.tests;

import com.eogren.link_checker.messaging.common.Utils;
import com.eogren.link_checker.protobuf.ScraperMessages;
import com.eogren.link_checker.status_updater.ScraperMessageProcessor;
import com.eogren.link_checker.service_layer.api.CrawlReport;
import com.eogren.link_checker.service_layer.api.MonitoredPage;
import com.eogren.link_checker.service_layer.client.ApiClient;
import com.eogren.link_checker.status_updater.StatusUpdaterApplication;
import com.eogren.link_checker.status_updater.config.StatusUpdaterConfig;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class StatusUpdaterTest {
    protected ScraperMessageProcessor processor;

    public class MonitoredPageUpdate {
        protected String url;
        protected MonitoredPage.Status newStatus;

        public MonitoredPageUpdate(String url, MonitoredPage.Status newStatus) {
            this.url = url;
            this.newStatus = newStatus;
        }

        public String getUrl() {
            return url;
        }

        public MonitoredPage.Status getNewStatus() {
            return newStatus;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            MonitoredPageUpdate that = (MonitoredPageUpdate) o;

            if (newStatus != that.newStatus) return false;
            if (!url.equals(that.url)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = url.hashCode();
            result = 31 * result + newStatus.hashCode();
            return result;
        }

        @Override
        public String toString() {
            return "MonitoredPageUpdate{" +
                    "url='" + url + '\'' +
                    ", newStatus=" + newStatus +
                    '}';
        }
    }

    public class TestApiClient extends ApiClient {
        private final Map<String, List<MonitoredPage>> pagesThatLinkTo;
        private final Map<String, List<CrawlReport>> crawlReportsFollowingLinks;
        private final List<MonitoredPageUpdate> statusUpdates;

        public TestApiClient(Map<String, List<MonitoredPage>> pagesThatLinkTo,
                             Map<String, List<CrawlReport>> crawlReportsFollowingLinks) {
            super("");

            this.pagesThatLinkTo = pagesThatLinkTo;
            this.crawlReportsFollowingLinks = crawlReportsFollowingLinks;
            this.statusUpdates = new ArrayList<>();
        }

        @Override
        public List<MonitoredPage> getMonitoredPagesThatLinkTo(String url) throws IOException {
            return pagesThatLinkTo.getOrDefault(url, new ArrayList<>());
        }

        @Override
        public List<CrawlReport> getLatestCrawlReportsFollowingLinksFor(String url) throws IOException {
            return crawlReportsFollowingLinks.getOrDefault(url, new ArrayList<>());
        }

        @Override
        public void updateMonitoredPageStatus(MonitoredPage page, MonitoredPage.Status pageStatus) {
            statusUpdates.add(new MonitoredPageUpdate(page.getUrl(), pageStatus));
        }

        public List<MonitoredPageUpdate> getStatusUpdates() {
            return statusUpdates;
        }
    }

    public class TestStatusUpdaterApplication extends StatusUpdaterApplication {
        private final Map<String, List<CrawlReport>> crawlReportsFollowingLinks;
        private final Map<String, List<MonitoredPage>> pagesThatLinkTo;

        public TestStatusUpdaterApplication(String configFile,
                                            Map<String, List<MonitoredPage>> pagesThatLinkTo,
                                            Map<String, List<CrawlReport>> crawlReportsFollowingLinks) {
            super(configFile);

            this.pagesThatLinkTo = pagesThatLinkTo;
            this.crawlReportsFollowingLinks = crawlReportsFollowingLinks;
        }

        @Override
        protected ApiClient createApiClient(StatusUpdaterConfig config) {
            return new TestApiClient(pagesThatLinkTo, crawlReportsFollowingLinks);
        }
    }

    @After
    public void setUpProcessor() {
        if (processor != null) {
            processor.stop();
        }
    }

    @Test
    public void testGoodStatus() {
        TestApiClient testClient = getDefaultTestEnvironment();
        processor = new ScraperMessageProcessor(testClient, 2);

        processor.consumeScrapeUpdate(
                ScraperMessages.ScrapeUpdate.newBuilder()
                        .setNewStatus(ScraperMessages.ScrapeResponse.newBuilder()
                                        .setUrl("http://www.brokenpage.com")
                                        .setStatus(false)
                                        .setStatusCode(500)
                                        .setStatusMessage("Broken by unit test")
                                        .build()
                        )
                        .build());

        try {
            Thread.sleep(100);
        } catch (InterruptedException ex) {}

        assertEquals("Expected message processor to have consumed 2 messages", 2, testClient.getStatusUpdates().size());

        System.out.println(testClient.getStatusUpdates().get(0));
        System.out.println(testClient.getStatusUpdates().get(1));

        assertTrue("Expected to find www.page1.com in error state",
                testClient.getStatusUpdates().contains(new MonitoredPageUpdate("http://www.page1.com", MonitoredPage.Status.ERROR)));

        assertTrue("Expected to find www.page2.com in error state",
                testClient.getStatusUpdates().contains(new MonitoredPageUpdate("http://www.page2.com", MonitoredPage.Status.ERROR)));
    }

    protected TestApiClient getDefaultTestEnvironment() {
        //
        // www.page1.com ---> www.brokenpage.com
        // www.page2.com ---> www.brokenpage.com
        // www.page3.com ---> www.goodpage.com
        MonitoredPage page1 = createMonitoredPage("http://www.page1.com");
        MonitoredPage page2 = createMonitoredPage("http://www.page2.com");
        MonitoredPage page3 = createMonitoredPage("http://www.page3.com");

        Map<String, List<MonitoredPage>> pagesThatLinkTo = new HashMap<>();

        List<MonitoredPage> brokenPageLinks = new ArrayList<>();
        brokenPageLinks.add(page1);
        brokenPageLinks.add(page2);
        pagesThatLinkTo.put("http://www.brokenpage.com", brokenPageLinks);

        List<MonitoredPage> goodPageLinks = new ArrayList<>();
        goodPageLinks.add(page3);
        pagesThatLinkTo.put("http://www.goodpage.com", goodPageLinks);

        CrawlReport p1CrawlReport = createCrawlReport("http://www.page1.com", MonitoredPage.Status.GOOD);
        CrawlReport p2CrawlReport = createCrawlReport("http://www.page2.com", MonitoredPage.Status.GOOD);
        CrawlReport p3CrawlReport = createCrawlReport("http://www.page3.com", MonitoredPage.Status.GOOD);
        CrawlReport brokenCrawlReport = createCrawlReport("http://www.brokenpage.com", MonitoredPage.Status.ERROR);
        CrawlReport goodCrawlReport = createCrawlReport("http://www.goodpage.com", MonitoredPage.Status.GOOD);

        Map<String, List<CrawlReport>> crawlReports = new HashMap<>();
        crawlReports.put("http://www.page1.com", getListOfReports(p1CrawlReport, brokenCrawlReport));
        crawlReports.put("http://www.page2.com", getListOfReports(p2CrawlReport, brokenCrawlReport));
        crawlReports.put("http://www.page3.com", getListOfReports(p3CrawlReport, goodCrawlReport));


        return new TestApiClient(pagesThatLinkTo, crawlReports);
    }

    protected List<CrawlReport> getListOfReports(CrawlReport... reports) {
        return Arrays.asList(reports);
    }

    protected MonitoredPage createMonitoredPage(String url) {
        return new MonitoredPage(url);
    }

    protected CrawlReport createCrawlReport(String url, MonitoredPage.Status status) {
        return new CrawlReport(url, DateTime.now(), "", status == MonitoredPage.Status.ERROR ? 500 : 200, new ArrayList<>());
    }

}
