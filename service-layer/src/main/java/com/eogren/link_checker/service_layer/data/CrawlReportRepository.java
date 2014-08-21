package com.eogren.link_checker.service_layer.data;

import com.eogren.link_checker.service_layer.api.CrawlReport;

import java.util.Optional;

/**
 * CrawlReportRepository is an interface for storage of CrawlReports.
 */
public interface CrawlReportRepository {
    /**
     * Add a new crawl report
     * @param report CrawlReport to add
     * @return UUID of newly added CrawlReport
     */
    public String addCrawlReport(CrawlReport report);

    /**
     * Retrieve the latest crawl report for a given URL.
     * @param url URL to retrieve
     * @return CrawlReport if it exists; null if the URL has not yet been crawled
     */
    public Optional<CrawlReport> getLatestStatus(String url);

    /**
     * Retrieve a crawl report by UUID.
     * @param url URL to retrieve
     * @param uuid UUID to retrieve
     * @return CrawlReport if it exists; null if none found
     */
    public Optional<CrawlReport> getByUuid(String url, String uuid);
}
