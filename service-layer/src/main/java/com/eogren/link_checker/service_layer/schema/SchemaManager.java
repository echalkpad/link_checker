package com.eogren.link_checker.service_layer.schema;

import com.datastax.driver.core.Session;
import com.datastax.driver.core.exceptions.AlreadyExistsException;
import com.datastax.driver.core.exceptions.NoHostAvailableException;
import com.datastax.driver.core.exceptions.QueryExecutionException;
import com.datastax.driver.core.exceptions.QueryValidationException;

public class SchemaManager {
    private final Session session;

    public SchemaManager(Session session) {
        this.session = session;
    }

    /**
     * Create a new Cassandra schema from scratch.
     * [Eventually this will probably need some form of db migration]
     */
    public void createSchema() {
        try {
            executeIgnoreNotExists(session, "CREATE TYPE IF NOT EXISTS " + session.getLoggedKeyspace() + ".found_link ( " +
                "url text, " +
                "anchorText text " +
            ");");

            executeIgnoreNotExists(session, "CREATE TABLE IF NOT EXISTS meta_data ( key text PRIMARY KEY, value text ) WITH comment='config metadata';");
            executeIgnoreNotExists(session, "CREATE TABLE IF NOT EXISTS root_page( " +
                    "url text PRIMARY KEY );");
            executeIgnoreNotExists(session, "CREATE TABLE IF NOT EXISTS crawl_report( " +
                            "url text, " +
                            "date timeuuid, " +
                            "error text, " +
                            "status_code int, " +
                            "links map<text, frozen <found_link>>, " +
                            "PRIMARY KEY (url, date)" +
                            ") WITH CLUSTERING ORDER BY (date DESC);"
            );
            executeIgnoreNotExists(session, "CREATE TABLE IF NOT EXISTS latest_crawl_report( " +
                            "url text, " +
                            "date timeuuid, " +
                            "error text, " +
                            "status_code int, " +
                            "links map<text, frozen <found_link>>, " +
                            "PRIMARY KEY (url)" +
                            ");"
            );

            executeIgnoreNotExists(session, "CREATE INDEX latest_report_index ON latest_crawl_report(KEYS(links))");
            executeIgnoreNotExists(session, "INSERT INTO meta_data (key, value) VALUES ('db_version', '1');");
        } catch (QueryValidationException | QueryExecutionException | NoHostAvailableException ex) {
            System.out.println(ex.getMessage());
        }
    }

    public void dropSchema() {
        try {
            executeIgnoreNotExists(session, "DROP TABLE crawl_report");
            executeIgnoreNotExists(session, "DROP TABLE latest_crawl_report");
            executeIgnoreNotExists(session, "DROP TABLE root_page");
            executeIgnoreNotExists(session, "DROP TABLE meta_data");
            executeIgnoreNotExists(session, "DROP TYPE " + session.getLoggedKeyspace() + ".found_link");
        } catch (QueryValidationException | QueryExecutionException | NoHostAvailableException ex) {
            System.out.println(ex.getMessage());
        }
    }

    public void closeSession() {
        session.close();
    }

    protected void executeIgnoreNotExists(Session session, String query) {
        try {
            session.execute(query);
        } catch (AlreadyExistsException ex) {
            System.out.println("Ignoring AlreadyExistsException for " + query);
        }
    }

}
