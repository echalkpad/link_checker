package com.eogren.link_checker.service_layer;

import com.eogren.link_checker.service_layer.commands.CreateSchemaCommand;
import com.eogren.link_checker.service_layer.data.CassandraRootPageRepository;
import com.eogren.link_checker.service_layer.data.InMemoryRootPageRepository;
import com.eogren.link_checker.service_layer.data.RootPageRepository;
import com.eogren.link_checker.service_layer.exceptions.DatabaseException;
import com.eogren.link_checker.service_layer.health.CassandraHealthCheck;
import com.eogren.link_checker.service_layer.resources.RootPageResource;

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.eclipse.jetty.servlets.CrossOriginFilter;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import java.util.EnumSet;

public class LinkCheckerApplication extends Application<LinkCheckerConfiguration> {
    public static void main(String[] args) throws Exception {
        new LinkCheckerApplication().run(args);
    }

    @Override
    public String getName() {
        return "link_checker";
    }

    @Override
    public void initialize(Bootstrap<LinkCheckerConfiguration> bootstrap) {
        bootstrap.addCommand(new CreateSchemaCommand());
    }

    @Override
    public void run(LinkCheckerConfiguration config, Environment environment) throws DatabaseException {
        environment.jersey().register(new RootPageResource(getRootPageRepository(config)));

        environment.healthChecks().register("cassandra", new CassandraHealthCheck(config.getCassandraFactory()));

        registerCorsFilter(environment);
   }

    private void registerCorsFilter(Environment environment) {
        final FilterRegistration.Dynamic cors =
                environment.servlets().addFilter("crossOriginRequests", CrossOriginFilter.class);
        cors.setInitParameter("allowedMethods", "OPTIONS,GET,POST,PUT,HEAD,DELETE");
        cors.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), true, "/*");
    }

    protected RootPageRepository getRootPageRepository(LinkCheckerConfiguration config) throws DatabaseException {
        if (config.getRepoType().equals("cassandra")) {
            return new CassandraRootPageRepository(config.getCassandraFactory().getSession());
        } else {
            return new InMemoryRootPageRepository();
        }
    }
}
