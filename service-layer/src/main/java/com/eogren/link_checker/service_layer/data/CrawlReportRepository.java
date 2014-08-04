package com.eogren.link_checker.service_layer.data;

import com.eogren.link_checker.service_layer.api.CrawlReport;

/**
 * CrawlReportRepository is an interface for storage of CrawlReports.
 */
public interface CrawlReportRepository {
    /**
     * Add a new crawl report
     * @param report CrawlReport to add
     */
    public void addCrawlReport(CrawlReport report);

    /**
     * Retrieve the latest crawl report for a given URL.
     * @param url URL to retrieve
     * @return CrawlReport if it exists; null if the URL has not yet been crawled
     */
    public CrawlReport getLatestStatus(String url);
}
