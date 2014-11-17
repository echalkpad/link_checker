package com.eogren.link_checker.integration.tests;

import com.eogren.link_checker.service_layer.LinkCheckerApplication;
import com.eogren.link_checker.service_layer.LinkCheckerConfiguration;
import com.eogren.link_checker.service_layer.api.CrawlReport;
import com.eogren.link_checker.service_layer.api.Link;
import com.eogren.link_checker.service_layer.api.MonitoredPage;
import com.eogren.link_checker.service_layer.commands.CreateSchemaCommand;
import com.eogren.link_checker.service_layer.exceptions.DatabaseException;
import com.eogren.link_checker.service_layer.schema.SchemaManager;
import com.eogren.link_checker.tests.categories.IntegrationTest;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.google.common.io.Resources;
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
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class LinkSearchTest {

    final String apiPrefix = "/api/v1";
    final String mpPrefix = apiPrefix + "/monitored_page/";
    final String crPrefix = apiPrefix + "/crawl_report/";

    @ClassRule
    public static DropwizardAppRule<LinkCheckerConfiguration> RULE =
            new DropwizardAppRule<>(LinkCheckerApplication.class, resourceFilePath("test.yml"));

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
        try {
            Client client = getClient();

            List<MonitoredPage> response =
                getResource(client, mpPrefix + "search?links_to=http://www.cnn.com")
                .get(new GenericType<List<MonitoredPage>>() {
                });


            assertEquals("Expected links to to be 0", 0, response.size());
        } catch (UniformInterfaceException uie) {
            uieHandler(uie);
        }
    }

    @Test
    public void testLinkSearchImplicitLink() {
        try {
            Client client = getClient();
            final String url = "http://www.page1.com";

            addMonitoredPage(client, url);
            List<MonitoredPage> response =
                    getResource(client, mpPrefix + "search?links_to=" + url)
                    .get(new GenericType<List<MonitoredPage>>(){});

            assertEquals("Expect monitored pages to implicitly link to themselves [empty size]", 1, response.size());
            assertEquals("Expect URLs to match", url, response.get(0).getUrl());
        } catch (UniformInterfaceException uie) {
            uieHandler(uie);
        }
    }

    @Test
    public void testLinkSearch() {
        final String target_url = "http://www.brokenlink.com";

        try {
            Client client = getClient();

            addMonitoredPage(client, "http://www.page1.com");
            addMonitoredPage(client, "http://www.page2.com");

            List<Link> page1List = new ArrayList<>();
            page1List.add(new Link(target_url, "Page 1"));
            page1List.add(new Link("http://www.cnn.com", "CNN"));

            postCrawlReport(client, "http://www.page1.com", page1List);
            postCrawlReport(client, "http://anonmonitoredpage.com", page1List);

            List<MonitoredPage> response =
                    getResource(client, mpPrefix + "search?links_to=" + target_url)
                            .get(new GenericType<List<MonitoredPage>>() {
                            });


            assertEquals("Expected found monitored pages to to be 1", 1, response.size());
            assertEquals("Expected URLs to match", "http://www.page1.com", response.get(0).getUrl());
        } catch (UniformInterfaceException uie) {
            uieHandler(uie);
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
            Client client = getClient();

            postCrawlReport(client, target_url, target_links);
            for (Link l : target_links) {
                postCrawlReport(client, l.getUrl(), empty_link);
            }

            List<CrawlReport> response =
                    getResource(client, crPrefix + "search?links_from=" + target_url)
                            .get(new GenericType<List<CrawlReport>>() {
                            });


            assertEquals("Expected found monitored pages to match", target_links.size(), response.size() - 1);

            Set<String> foundUrls = response.stream().map(CrawlReport::getUrl).collect(Collectors.toSet());
            assertTrue("Expected URL to link to itself", foundUrls.contains(target_url));
            for (Link l : target_links) {
                assertTrue("Expected URLs to be in set", foundUrls.contains(l.getUrl()));
            }

        } catch (UniformInterfaceException uie) {
            uieHandler(uie);
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

        getResource(client, crPrefix).entity(crp, MediaType.APPLICATION_JSON).post();
    }

    private WebResource getResource(Client client, String path) {
        return client.resource(
                String.format("http://localhost:%d%s", RULE.getLocalPort(), path));
    }

    private Client getClient() {
        ClientConfig clientConfig = new DefaultClientConfig();
        clientConfig.getClasses().add(JacksonJsonProvider.class);

        return Client.create(clientConfig);
    }

    private static String resourceFilePath(String resourceClassPathLocation) {
        try {
            return new File(Resources.getResource(resourceClassPathLocation).toURI()).getAbsolutePath();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void addMonitoredPage(Client client, String url) {
        getResource(client, mpPrefix + url)
                .entity(new MonitoredPage(url), MediaType.APPLICATION_JSON)
                .put();
    }

    private void uieHandler(UniformInterfaceException uie) {
        ClientResponse resp = uie.getResponse();
        StringBuffer sb = new StringBuffer();
        sb.append("UniformInterfaceException caught: HTTP code ");
        sb.append(resp.getStatus());
        sb.append(", body: ");
        sb.append(resp.getEntity(String.class));
        fail(sb.toString());
    }
}
