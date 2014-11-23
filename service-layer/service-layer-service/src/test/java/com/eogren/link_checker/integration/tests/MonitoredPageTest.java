package com.eogren.link_checker.integration.tests;

import com.eogren.link_checker.service_layer.LinkCheckerApplication;
import com.eogren.link_checker.service_layer.LinkCheckerConfiguration;
import com.eogren.link_checker.service_layer.exceptions.DatabaseException;
import com.eogren.link_checker.service_layer.schema.SchemaManager;
import com.eogren.link_checker.tests.categories.IntegrationTest;
import com.eogren.link_checker.tests.utils.TestUtils;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.experimental.categories.Category;

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
    public void testMonitoredPageComesBackWithCorrectDefaults() {

    }

}
