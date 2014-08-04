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
}
