package com.eogren.link_checker.integration.tests;

import com.eogren.link_checker.service_layer.LinkCheckerApplication;
import com.eogren.link_checker.service_layer.LinkCheckerConfiguration;
import com.eogren.link_checker.service_layer.api.MonitoredPage;
import com.eogren.link_checker.service_layer.client.ApiClient;
import com.eogren.link_checker.service_layer.exceptions.DatabaseException;
import com.eogren.link_checker.service_layer.schema.SchemaManager;
import com.eogren.link_checker.tests.categories.IntegrationTest;
import com.eogren.link_checker.tests.utils.TestUtils;
import com.google.common.collect.ImmutableSet;
import com.sun.jersey.api.client.ClientResponse;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class MonitoredPageTest {


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
    public void testMonitoredPageDefaultsAndUpdate() throws IOException {
        ApiClient c = new ApiClient("http://localhost:" + RULE.getLocalPort());

        final String url = "http://www.testpage.com";
        final MonitoredPage.Status newState = MonitoredPage.Status.GOOD;

        TestUtils.addMonitoredPage(url, RULE);

        MonitoredPage mp = TestUtils.getMonitoredPage(url, RULE);
        assertEquals("Expected initial state of MonitoredPage to be UNKNOWN", MonitoredPage.Status.UNKNOWN, mp.getStatus());

        DateTime addTime = mp.getLastUpdated();

        assertTrue("Expected addTime to be before current date", addTime.isBeforeNow());


        c.updateMonitoredPageStatus(mp, MonitoredPage.Status.GOOD);
        mp = TestUtils.getMonitoredPage(url, RULE);

        assertEquals("Expected URLs to match", url, mp.getUrl());
        assertEquals("Expected updated state of MonitoredPage to be " + newState.toString(), newState, mp.getStatus());
        assertTrue("Expected updateTime to be after addTime", mp.getLastUpdated().isAfter(addTime));
    }


    @Test
    public void testMonitoredPage404s() {
        final String url = "http://www.notexist.com";

        try {
            MonitoredPage mp = TestUtils.getMonitoredPage(url, RULE);
            fail("Expected to receive 404 for " + url);
        } catch (com.sun.jersey.api.client.UniformInterfaceException e) {
            ClientResponse r = e.getResponse();
            assertEquals("Expected to receive 404 for " + url, 404, r.getStatus());
        }
    }

    @Test
    public void canGetAllMonitoredPages() throws IOException {
        ApiClient c = new ApiClient("http://localhost:" + RULE.getLocalPort());

        Set<String> urls = ImmutableSet.of(
                "http://www.cnn.com",
                "http://www.nytimes.com",
                "http://www.third.com"
        );

        for (String url : urls) {
            TestUtils.addMonitoredPage(url, RULE);
        }

        List<MonitoredPage> pages = c.retrieveAllMonitoredPages();
        assertEquals("Expected # of monitoredPages to be the same", urls.size(), pages.size());

        for (MonitoredPage p : pages) {
            assertTrue("Expected to find in set", urls.contains(p.getUrl()));
        }
    }
}
