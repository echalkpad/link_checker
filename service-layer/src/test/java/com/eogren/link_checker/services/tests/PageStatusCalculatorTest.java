package com.eogren.link_checker.services.tests;

import com.eogren.link_checker.service_layer.api.Page;
import com.eogren.link_checker.service_layer.data.CrawlReportRepository;
import com.eogren.link_checker.service_layer.services.PageStatusCalculator;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class PageStatusCalculatorTest {

    private CrawlReportRepository crawl_repo;

    private PageStatusCalculator calculator;

    @Before
    public void setUp() {
        crawl_repo = mock(CrawlReportRepository.class);

        calculator = new PageStatusCalculator(crawl_repo);
    }

    @Test
    public void testReturnsWhenPageNotMonitored() {
        final String url = "http://www.notmonitored.com";

        Page.LinkStatus linkStatus = calculator.calculateLinkStatus(url);

        assertEquals(Page.LinkStatus.NOT_FULLY_CRAWLED, linkStatus);
    }
}
