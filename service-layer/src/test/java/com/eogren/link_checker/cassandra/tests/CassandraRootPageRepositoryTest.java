package com.eogren.link_checker.cassandra.tests;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.eogren.link_checker.service_layer.api.Page;
import com.eogren.link_checker.service_layer.commands.CreateSchemaCommand;
import com.eogren.link_checker.service_layer.data.CassandraRootPageRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.Assert.*;

public class CassandraRootPageRepositoryTest {
    protected String keyspace;
    protected Session session;
    protected CassandraRootPageRepository repo;

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
        System.out.println(q);
        session.execute(q);
        session.execute("USE " + keyspace + ";");

        CreateSchemaCommand schemaCommand = new CreateSchemaCommand(session);
        schemaCommand.createSchema();

        repo = new CassandraRootPageRepository(session);
    }

    @After
    public void after() {
        session.execute("DROP KEYSPACE " + keyspace + ";");
        session.close();
        session = null;
    }

    @Test
    public void testBlankRepoHasNoPage() {
        List<Page> all_pages = repo.getAllRootPages();
        assertEquals("Expected all_pages to be empty", 0, all_pages.size());
    }

    @Test
    public void testExistsNoPages() {
        assertFalse("Expected dummy page not to exist", repo.pageExists("http://www.notadded.com"));
    }

    @Test
    public void testCanAdd() {
        final String pageUrl = "http://www.eogren.com";

        Page p = new Page(pageUrl, true, null);
        repo.addPage(p);

        assertTrue("Expected page " + pageUrl + " to exist", repo.pageExists(pageUrl));

        List<Page> all_pages = repo.getAllRootPages();
        assertEquals("Expected all_pages to have 1 element", 1, all_pages.size());
        assertEquals("Expected all_pages[0] to have url " + pageUrl, pageUrl, all_pages.get(0).getUrl());
    }
}
