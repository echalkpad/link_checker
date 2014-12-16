package com.eogren.link_checker.messaging.tests;

import com.eogren.link_checker.messaging.serde.ProtobufSerializer;
import com.eogren.link_checker.protobuf.ScraperMessages;
import com.eogren.link_checker.service_layer.api.CrawlReport;
import com.eogren.link_checker.service_layer.api.Link;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class ProtobufSerializerTest {
    @Test
    public void testCrawlReportSerializer() {
        final String crawl_url = "http://www.nytimes.com/";
        List<Link> expected_links = Arrays.asList(
            // new link, etc
        );

        CrawlReport rep = createCrawlReport(crawl_url, "", 200, expected_links);

        ScraperMessages.ScrapeResponse resp = ProtobufSerializer.crawlReportToProtobuf(rep);

        assertEquals("Expected URLs to match", resp.getUrl(), crawl_url);
        assertEquals("Expected statusCode to match", resp.getStatusCode(), rep.getStatusCode());
        assertTrue("Expected status to be true for 200", resp.getStatus());
        assertEquals("Expected status codes to match", resp.getStatusMessage(), rep.getError()); // ?
        assertEquals("Expected links to match", resp.getLinksCount(), expected_links.size());
        for (Link l : expected_links) {
            if (!resp.getLinksList().contains(l)) {
                fail(String.format("Expected to find link %s in scrapeResponse", l.toString()));
            }
        }
    }

    protected CrawlReport createCrawlReport(String url, String status_msg, int status_code, List<Link> expected_links) {
        return new CrawlReport(url, DateTime.now(), status_msg, status_code, expected_links);
    }
}
