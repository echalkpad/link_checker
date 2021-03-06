package com.eogren.link_checker.cassandra.tests;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.eogren.link_checker.service_layer.api.MonitoredPage;
import com.eogren.link_checker.service_layer.api.Page;
import com.eogren.link_checker.service_layer.commands.CreateSchemaCommand;
import com.eogren.link_checker.service_layer.data.CassandraMonitoredPageRepository;
import com.eogren.link_checker.service_layer.schema.SchemaManager;
import com.eogren.link_checker.tests.categories.IntegrationTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class CassandraMonitoredPageRepositoryTest {
    protected String keyspace;
    protected Session session;
    protected CassandraMonitoredPageRepository repo;

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

        repo = new CassandraMonitoredPageRepository(session);
    }

    @After
    public void after() {
        session.execute("DROP KEYSPACE " + keyspace + ";");
        session.close();
        session = null;
    }

    @Test
    public void testBlankRepoHasNoPage() {
        List<MonitoredPage> all_pages = repo.getAllMonitoredPages();
        assertEquals("Expected all_pages to be empty", 0, all_pages.size());
    }

    @Test
    public void testExistsNoPages() {
        assertFalse("Expected dummy page not to exist", repo.pageAlreadyMonitored("http://www.notadded.com"));
    }

    @Test
    public void testCanAdd() {
        final String pageUrl = "http://www.eogren.com";

        MonitoredPage p = createMonitoredPage(pageUrl);
        repo.addMonitoredPage(p);

        assertTrue("Expected page " + pageUrl + " to exist", repo.pageAlreadyMonitored(pageUrl));

        List<MonitoredPage> all_pages = repo.getAllMonitoredPages();
        assertEquals("Expected all_pages to have 1 element", 1, all_pages.size());
        assertEquals("Expected all_pages[0] to have url " + pageUrl, pageUrl, all_pages.get(0).getUrl());
    }

    @Test
    public void testCanFilter() {
        repo.addMonitoredPage(createMonitoredPage("http://www.cnn.com"));
        repo.addMonitoredPage(createMonitoredPage("http://www.nytimes.com"));

        Set<String> testUrls = new HashSet<>();
        testUrls.add("http://www.cnn.com");
        testUrls.add("http://www.someother.page.com");
        testUrls.add("http://www.athirdpage.com");

        List<MonitoredPage> res = repo.findByUrl(testUrls);

        assertEquals("Expected to find only 1 filtered page", 1, res.size());
        assertEquals("Expected URL match", "http://www.cnn.com", res.get(0).getUrl());
    }

    private MonitoredPage createMonitoredPage(String pageUrl) {
        return new MonitoredPage(pageUrl);
    }
}
