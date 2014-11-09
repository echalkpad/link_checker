package com.eogren.link_checker.service_layer;

import com.eogren.link_checker.messaging.EmitterFactory;
import com.eogren.link_checker.messaging.MessageEmitter;
import com.eogren.link_checker.service_layer.commands.CreateSchemaCommand;
import com.eogren.link_checker.service_layer.data.*;
import com.eogren.link_checker.service_layer.exceptions.DatabaseException;
import com.eogren.link_checker.service_layer.health.CassandraHealthCheck;
import com.eogren.link_checker.service_layer.resources.CrawlReportResource;
import com.eogren.link_checker.service_layer.resources.MonitoredPageResource;

import com.wordnik.swagger.config.ConfigFactory;
import com.wordnik.swagger.config.ScannerFactory;
import com.wordnik.swagger.config.SwaggerConfig;
import com.wordnik.swagger.jaxrs.config.DefaultJaxrsScanner;
import com.wordnik.swagger.jaxrs.listing.ApiDeclarationProvider;
import com.wordnik.swagger.jaxrs.listing.ApiListingResourceJSON;
import com.wordnik.swagger.jaxrs.listing.ResourceListingProvider;
import com.wordnik.swagger.jaxrs.reader.DefaultJaxrsApiReader;
import com.wordnik.swagger.reader.ClassReaders;
import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.eclipse.jetty.servlets.CrossOriginFilter;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import java.util.EnumSet;

public class LinkCheckerApplication extends Application<LinkCheckerConfiguration> {
    private MonitoredPageRepository monitoredPageRepo;
    private CrawlReportRepository crawlReportRepo;
    private MessageEmitter messageEmitter;

    public static void main(String[] args) throws Exception {
        new LinkCheckerApplication().run(args);
    }

    @Override
    public String getName() {
        return "link_checker";
    }

    @Override
    public void initialize(Bootstrap<LinkCheckerConfiguration> bootstrap) {
        bootstrap.addBundle(new AssetsBundle("/swagger-ui/dist", "/swagger", "index.html", "swagger-ui"));
        bootstrap.addCommand(new CreateSchemaCommand());
    }

    @Override
    public void run(LinkCheckerConfiguration config, Environment environment) throws DatabaseException {
        // Misc
        messageEmitter = EmitterFactory.create(config.getKafkaConfig());

        // API Resources
        environment.jersey().register(new MonitoredPageResource(getMonitoredPageRepository(config), getCrawlReportRepository(config)));
        environment.jersey().register(new CrawlReportResource(getCrawlReportRepository(config), messageEmitter));

        // Health checks
        environment.healthChecks().register("cassandra", new CassandraHealthCheck(config.getCassandraFactory()));

        registerCorsFilter(environment);

        // Swagger
        environment.jersey().register(new ApiListingResourceJSON());
        environment.jersey().register(new ApiDeclarationProvider());
        environment.jersey().register(new ResourceListingProvider());
        ScannerFactory.setScanner(new DefaultJaxrsScanner());
        ClassReaders.setReader(new DefaultJaxrsApiReader());

        SwaggerConfig swaggerConfig = ConfigFactory.config();
        swaggerConfig.setApiVersion("0.1");
        swaggerConfig.setBasePath("http://devbox:8080");
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
