package com.eogren.link_checker.service_layer;

import com.eogren.link_checker.service_layer.data.InMemoryRootPageRepository;
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
    }

    @Override
    public void run(LinkCheckerConfiguration config, Environment environment) {
        environment.jersey().register(new RootPageResource(new InMemoryRootPageRepository()));
        final FilterRegistration.Dynamic cors = environment.servlets().addFilter("crossOriginRequests", CrossOriginFilter.class);
        cors.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), true, "/*");
    }
}
