package com.eogren.link_checker.service_layer;

import com.eogren.link_checker.service_layer.commands.CreateSchemaCommand;
import com.eogren.link_checker.service_layer.data.*;
import com.eogren.link_checker.service_layer.exceptions.DatabaseException;
import com.eogren.link_checker.service_layer.health.CassandraHealthCheck;
import com.eogren.link_checker.messaging.KafkaProducer;
import com.eogren.link_checker.service_layer.resources.CrawlReportResource;
import com.eogren.link_checker.service_layer.resources.MonitoredPageResource;

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.eclipse.jetty.servlets.CrossOriginFilter;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import java.util.EnumSet;

public class LinkCheckerApplication extends Application<LinkCheckerConfiguration> {
    private MonitoredPageRepository monitoredPageRepo;
    private CrawlReportRepository crawlReportRepo;
    private KafkaProducer kafkaProducer;

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
        kafkaProducer = new KafkaProducer(config.getKafkaConfig());

        environment.jersey().register(new MonitoredPageResource(getMonitoredPageRepository(config)));
        environment.jersey().register(new CrawlReportResource(getCrawlReportRepository(config), kafkaProducer));

        environment.healthChecks().register("cassandra", new CassandraHealthCheck(config.getCassandraFactory()));

        registerCorsFilter(environment);
   }

    private void registerCorsFilter(Environment environment) {
        final FilterRegistration.Dynamic cors =
                environment.servlets().addFilter("crossOriginRequests", CrossOriginFilter.class);
        cors.setInitParameter("allowedMethods", "OPTIONS,GET,POST,PUT,HEAD,DELETE");
        cors.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), true, "/*");
    }

    protected MonitoredPageRepository getMonitoredPageRepository(LinkCheckerConfiguration config) throws DatabaseException {
        if (monitoredPageRepo != null) {
            return monitoredPageRepo;
        }

        if (config.getRepoType().equals("cassandra")) {
            monitoredPageRepo = new CassandraMonitoredPageRepository(
                    config.getCassandraFactory().getSession());
        } else {
            monitoredPageRepo = new InMemoryMonitoredPageRepository();
        }

        return monitoredPageRepo;
    }

    protected CrawlReportRepository getCrawlReportRepository(LinkCheckerConfiguration config) throws DatabaseException {
        if (crawlReportRepo != null) {
            return crawlReportRepo;
        }

        if (config.getRepoType().equals("cassandra")) {
            crawlReportRepo = new CassandraCrawlReportRepository(config.getCassandraFactory().getSession());
        } else {
            throw new IllegalArgumentException("only know how to use cassandra for now");
        }

        return crawlReportRepo;
    }
}
