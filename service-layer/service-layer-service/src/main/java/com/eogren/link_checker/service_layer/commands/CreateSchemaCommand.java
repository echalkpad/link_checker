package com.eogren.link_checker.service_layer.commands;

import com.datastax.driver.core.Session;
import com.datastax.driver.core.exceptions.AlreadyExistsException;
import com.datastax.driver.core.exceptions.NoHostAvailableException;
import com.datastax.driver.core.exceptions.QueryExecutionException;
import com.datastax.driver.core.exceptions.QueryValidationException;
import com.eogren.link_checker.service_layer.LinkCheckerConfiguration;
import com.eogren.link_checker.service_layer.exceptions.DatabaseException;
import com.eogren.link_checker.service_layer.schema.SchemaManager;
import io.dropwizard.cli.ConfiguredCommand;
import io.dropwizard.setup.Bootstrap;
import net.sourceforge.argparse4j.inf.Namespace;

public class CreateSchemaCommand extends ConfiguredCommand<LinkCheckerConfiguration> {
    private SchemaManager mgr;

    public CreateSchemaCommand() {
        super("create_schema", "Create the necessary tables in Cassandra");
    }

    public CreateSchemaCommand(Session session) {
        super("create_schema", "Create the necessary tables in Cassandra");
        this.mgr = new SchemaManager(session);
    }

    @Override
    public void run(Bootstrap<LinkCheckerConfiguration> bootstrap,
                    Namespace namespace,
                    LinkCheckerConfiguration config) throws DatabaseException {
        mgr = new SchemaManager(config.getCassandraFactory().getSession());
        try {
            mgr.createSchema();
        } finally {
            mgr.closeSession();
        }

        System.exit(0);
    }


}
