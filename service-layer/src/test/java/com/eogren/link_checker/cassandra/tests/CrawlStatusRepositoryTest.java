package com.eogren.link_checker.cassandra.tests;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.eogren.link_checker.service_layer.api.CrawlReport;
import com.eogren.link_checker.service_layer.api.Link;
import com.eogren.link_checker.service_layer.commands.CreateSchemaCommand;
import com.eogren.link_checker.service_layer.data.CassandraCrawlReportRepository;
import com.eogren.link_checker.service_layer.schema.SchemaManager;
import org.joda.time.DateTime;
import org.joda.time.Seconds;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

public class CrawlStatusRepositoryTest {
    protected String keyspace;
    protected Session session;
    protected CassandraCrawlReportRepository repo;

    @Before
    public void setUp() {
        String cluster = System.getenv("CASSANDRA_HOST");
        if (cluster == null) {
            cluster = "127.0.0.1";
        }

        keyspace = "rootpage_" + UUID.randomUUID().toString();
        keyspace = keyspace.replace("-", "_");

        session = Cluster.builder().addContactPoint(cluster).build().connect();
        String q = "CREATE KEYSPACE " + keyspace + " WITH replication={'class':'SimpleStrategy', 'replication_factor': 1};";
        session.execute(q);
        session.execute("USE " + keyspace + ";");

        SchemaManager schemaManager = new SchemaManager(session);
        schemaManager.createSchema();

        repo = new CassandraCrawlReportRepository(session);
    }

    @After
    public void after() {
        session.execute("DROP KEYSPACE " + keyspace + ";");
        session.close();
        session = null;
    }

    @Test
    public void testCanRetrieveEmptyStatus() {
        Optional<CrawlReport> cr = repo.getLatestStatus("http://notadded.com");
        assertFalse("Expected to get empty CrawlReport for uncrawled page", cr.isPresent());
    }

    @Test
    public void testCanAddAndThenRetrieve() {
        final String url = "http://www.cnn.com";

        CrawlReport cr = createCrawlReport(url, new Link("http://www.hello.com", "Hello There") );

        String uuid = repo.addCrawlReport(cr);

        assertNotNull("Expected UUID from addCrawlReport to not be null", uuid);
        assertTrue("Expected UUID length to be > 1", uuid.length() > 1);

        CrawlReport cr2 = repo.getByUuid(url, uuid).orElse(null);

        assertNotNull("Expected to be able to retrieve CR by uuid", cr2);
        assertCrawlReportsEqual(cr, cr2);

        CrawlReport cr3 = repo.getLatestStatus(url).orElse(null);

        assertNotNull("Expected to be able to retrieve latest status", cr3);
        assertCrawlReportsEqual(cr, cr3);
    }

    @Test
    public void testReturnsEmptyLinkedIn() {
        List<String> reports = repo.getLatestLinksFor("http://www.doesntexist.com");
        assertEquals("Expected 0 returns", 0, reports.size());
    }


    @Test
    public void testRetrievesLatest() {
        final String target_page = "http://www.linked_to.com";

        final String url1 = "http://www.page1.com";
        final String url2 = "http://www.page2.com";

        repo.addCrawlReport(createCrawlReport(url1, new Link(target_page, "Older link")));
        repo.addCrawlReport(createCrawlReport(url1, new Link("http://www.somewhereelse.com", "Different link")));
        repo.addCrawlReport(createCrawlReport(url2, new Link(target_page, "Second link")));

        List<String> reports = repo.getLatestLinksFor(target_page);
        assertEquals("Expected latest links to have size 1", 1, reports.size());
        assertEquals("Expected " + url2 + " to be URL returned", url2, reports.get(0));
    }

    protected CrawlReport createCrawlReport(String url, Link... links) {
        return new CrawlReport(url, DateTime.now(), null, 200, Arrays.asList(links));
    }

    protected void assertCrawlReportsEqual(CrawlReport cr1, CrawlReport cr2) {
        assertEquals("Expected URLs to match", cr1.getUrl(), cr2.getUrl());
        assertEquals("Expected status code to match", cr1.getStatusCode(), cr2.getStatusCode());
        assertEquals("Expected error to match", cr1.getError(), cr2.getError());
        assertEquals("Expected links to match", cr1.getLinks(), cr2.getLinks());
        Seconds diff = Seconds.secondsBetween(cr1.getDate(), cr2.getDate());
        assertTrue("Expected crawl time to be less than 2 seconds apart", Math.abs(diff.getSeconds()) <= 2);
    }

}
