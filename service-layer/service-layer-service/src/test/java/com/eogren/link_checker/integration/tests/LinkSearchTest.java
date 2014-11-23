package com.eogren.link_checker.integration.tests;

import com.eogren.link_checker.service_layer.LinkCheckerApplication;
import com.eogren.link_checker.service_layer.LinkCheckerConfiguration;
import com.eogren.link_checker.service_layer.api.CrawlReport;
import com.eogren.link_checker.service_layer.api.Link;
import com.eogren.link_checker.service_layer.api.MonitoredPage;
import com.eogren.link_checker.service_layer.exceptions.DatabaseException;
import com.eogren.link_checker.service_layer.schema.SchemaManager;
import com.eogren.link_checker.tests.categories.IntegrationTest;
import com.eogren.link_checker.tests.utils.TestUtils;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.sun.jersey.api.client.*;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class LinkSearchTest {


    @ClassRule
    public static DropwizardAppRule<LinkCheckerConfiguration> RULE =
            new DropwizardAppRule<>(LinkCheckerApplication.class, TestUtils.resourceFilePath("test.yml"));

    @Before
    public void createSchema() {
        try {
            SchemaManager mgr = new SchemaManager(RULE.getConfiguration().getCassandraFactory().getSession());
            mgr.createSchema();
        } catch (DatabaseException e) {
            throw new RuntimeException(e);
        }
    }

    @After
    public void dropSchema() {
        try {
            SchemaManager mgr = new SchemaManager(RULE.getConfiguration().getCassandraFactory().getSession());
            mgr.dropSchema();
        } catch (DatabaseException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testEmptyResponse() {
        final String url = "http://www.cnn.com";

        try {
            List<MonitoredPage> response = TestUtils.searchMonitoredPagesLinksTo(url, RULE);


            assertEquals("Expected links to to be 0", 0, response.size());
        } catch (UniformInterfaceException uie) {
            TestUtils.uieHandler(uie);
        }
    }



    @Test
    public void testLinkSearchImplicitLink() {
        try {
            Client client = TestUtils.getClient();
            final String url = "http://www.page1.com";

            TestUtils.addMonitoredPage(client, url, RULE);
            List<MonitoredPage> response =
                    TestUtils.getResource(client, TestUtils.mpPrefix + "search?links_to=" + url, RULE)
                    .get(new GenericType<List<MonitoredPage>>(){});

            assertEquals("Expect monitored pages to implicitly link to themselves [empty size]", 1, response.size());
            assertEquals("Expect URLs to match", url, response.get(0).getUrl());
        } catch (UniformInterfaceException uie) {
            TestUtils.uieHandler(uie);
        }
    }

    @Test
    public void testLinkSearch() {
        final String target_url = "http://www.brokenlink.com";

        try {
            Client client = TestUtils.getClient();

            TestUtils.addMonitoredPage(client, "http://www.page1.com", RULE);
            TestUtils.addMonitoredPage(client, "http://www.page2.com", RULE);

            List<Link> page1List = new ArrayList<>();
            page1List.add(new Link(target_url, "Page 1"));
            page1List.add(new Link("http://www.cnn.com", "CNN"));

            postCrawlReport(client, "http://www.page1.com", page1List);
            postCrawlReport(client, "http://anonmonitoredpage.com", page1List);

            List<MonitoredPage> response = TestUtils.searchMonitoredPagesLinksTo(target_url, RULE);

            assertEquals("Expected found monitored pages to to be 1", 1, response.size());
            assertEquals("Expected URLs to match", "http://www.page1.com", response.get(0).getUrl());
        } catch (UniformInterfaceException uie) {
            TestUtils.uieHandler(uie);
        }
    }

    @Test
    public void testCrawlReportSearch() {
        final String target_url = "http://www.target.com";

        List<Link> target_links = new ArrayList<>();
        target_links.add(new Link("http://www.link1.com", "Link1 1"));
        target_links.add(new Link("http://www.cnn.com", "CNN"));

        List<Link> empty_link = new ArrayList<>();

        try {
            Client client = TestUtils.getClient();

            postCrawlReport(client, target_url, target_links);
            for (Link l : target_links) {
                postCrawlReport(client, l.getUrl(), empty_link);
            }

            List<CrawlReport> response =
                    TestUtils.getResource(client, TestUtils.crPrefix + "search?links_from=" + target_url, RULE)
                            .get(new GenericType<List<CrawlReport>>() {
                            });


            assertEquals("Expected found monitored pages to match", target_links.size(), response.size() - 1);

            Set<String> foundUrls = response.stream().map(CrawlReport::getUrl).collect(Collectors.toSet());
            assertTrue("Expected URL to link to itself", foundUrls.contains(target_url));
            for (Link l : target_links) {
                assertTrue("Expected URLs to be in set", foundUrls.contains(l.getUrl()));
            }

        } catch (UniformInterfaceException uie) {
            TestUtils.uieHandler(uie);
        }
    }

    private void postCrawlReport(Client client, String crawled_url, List<Link> links) {
        CrawlReport crp = new CrawlReport(
                crawled_url,
                new DateTime(),
                null,
                200,
                links
        );

        TestUtils.getResource(client, TestUtils.crPrefix, RULE).entity(crp, MediaType.APPLICATION_JSON).post();
    }
}
