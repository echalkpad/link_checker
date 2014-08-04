package com.eogren.link_checker.service_layer.commands;

import com.datastax.driver.core.Session;
import com.datastax.driver.core.exceptions.AlreadyExistsException;
import com.datastax.driver.core.exceptions.NoHostAvailableException;
import com.datastax.driver.core.exceptions.QueryExecutionException;
import com.datastax.driver.core.exceptions.QueryValidationException;
import com.eogren.link_checker.service_layer.LinkCheckerConfiguration;
import com.eogren.link_checker.service_layer.exceptions.DatabaseException;
import io.dropwizard.cli.ConfiguredCommand;
import io.dropwizard.setup.Bootstrap;
import net.sourceforge.argparse4j.inf.Namespace;

public class CreateSchemaCommand extends ConfiguredCommand<LinkCheckerConfiguration> {
    protected Session session;

    public CreateSchemaCommand() {
        super("create_schema", "Create the necessary tables in Cassandra");
    }

    public CreateSchemaCommand(Session session) {
        super("create_schema", "Create the necessary tab les in Cassandra");
        this.session = session;
    }

    @Override
    public void run(Bootstrap<LinkCheckerConfiguration> bootstrap,
                    Namespace namespace,
                    LinkCheckerConfiguration config) throws DatabaseException {
        session = config.getCassandraFactory().getSession();
        try {
            createSchema();
        } finally {
            session.close();
        }

        System.exit(0);
    }

    /**
     * Create a new Cassandra schema from scratch.
     * [Eventually this will probably need some form of db migration]
     */
    public void createSchema() {
        try {
            executeIgnoreNotExists(session, "CREATE TABLE IF NOT EXISTS meta_data ( key text PRIMARY KEY, value text ) WITH comment='config metadata';");
            executeIgnoreNotExists(session, "CREATE TABLE IF NOT EXISTS root_page( " +
                    "url text PRIMARY KEY );");
            executeIgnoreNotExists(session, "CREATE TABLE IF NOT EXISTS crawl_report( " +
                    "url text, " +
                    "date timestamp, " +
                    "error text, " +
                    "status_code int, " +
                    "links text, " +
                    "PRIMARY KEY (url, date)" +
                    ") WITH CLUSTERING ORDER BY (date DESC);"
            );
            executeIgnoreNotExists(session, "INSERT INTO meta_data (key, value) VALUES ('db_version', '1');");
        } catch (QueryValidationException | QueryExecutionException | NoHostAvailableException ex) {
            System.out.println(ex.getMessage());
        }
    }

    protected void executeIgnoreNotExists(Session session, String query) {
        try {
            session.execute(query);
        } catch (AlreadyExistsException ex) {
            System.out.println("Ignoring AlreadyExistsException for " + query);
        }
    }
}
