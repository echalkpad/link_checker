package com.eogren.link_checker.service_layer;

import com.eogren.link_checker.service_layer.resources.RootPageResource;
import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

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
        environment.jersey().register(new RootPageResource(null));
    }
}
