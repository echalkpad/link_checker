package com.eogren.link_checker.service_layer.services;

import com.eogren.link_checker.service_layer.api.Page;
import com.eogren.link_checker.service_layer.data.CrawlReportRepository;

public class PageStatusCalculator {
    private final CrawlReportRepository crawl_repo;

    public PageStatusCalculator(CrawlReportRepository crawl_repo) {
        this.crawl_repo = crawl_repo;
    }

    public Page.LinkStatus calculateLinkStatus(String url) {
        return Page.LinkStatus.NOT_FULLY_CRAWLED;

        //Optional<CrawlReport> crawl_report = crawl_repo.getLatestStatus(url);
        //Page.Status.
    }
}
