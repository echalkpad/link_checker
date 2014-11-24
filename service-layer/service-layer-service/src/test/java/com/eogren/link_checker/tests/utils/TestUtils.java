package com.eogren.link_checker.tests.utils;

import com.eogren.link_checker.service_layer.LinkCheckerConfiguration;
import com.eogren.link_checker.service_layer.api.MonitoredPage;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.google.common.io.Resources;
import com.sun.jersey.api.client.*;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import io.dropwizard.testing.junit.DropwizardAppRule;

import javax.ws.rs.core.MediaType;
import java.io.File;
import java.util.List;

import static org.junit.Assert.fail;

/**
 * Various test utilities.
 */
public class TestUtils {
    public static final String apiPrefix = "/api/v1";
    public static final String mpPrefix = apiPrefix + "/monitored_page/";
    public static final String crPrefix = apiPrefix + "/crawl_report/";

    public static String resourceFilePath(String resourceClassPathLocation) {
        try {
            return new File(Resources.getResource(resourceClassPathLocation).toURI()).getAbsolutePath();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void uieHandler(UniformInterfaceException uie) {
        ClientResponse resp = uie.getResponse();
        StringBuffer sb = new StringBuffer();
        sb.append("UniformInterfaceException caught: HTTP code ");
        sb.append(resp.getStatus());
        sb.append(", body: ");
        sb.append(resp.getEntity(String.class));
        fail(sb.toString());
    }

    public static List<MonitoredPage> searchMonitoredPagesLinksTo(String url, DropwizardAppRule<LinkCheckerConfiguration> rule) {
        Client client = getClient();

        return getResource(client, TestUtils.mpPrefix + "search?links_to=" + url, rule)
                .get(new GenericType<List<MonitoredPage>>() {
                });
    }

    public static WebResource getResource(Client client, String path, DropwizardAppRule<LinkCheckerConfiguration> rule) {
        return client.resource(
                String.format("http://localhost:%d%s", rule.getLocalPort(), path));
    }

    public static Client getClient() {
        ClientConfig clientConfig = new DefaultClientConfig();
        clientConfig.getClasses().add(JacksonJsonProvider.class);

        return Client.create(clientConfig);
    }

    public static void addMonitoredPage(String url, DropwizardAppRule<LinkCheckerConfiguration> rule) {
       addMonitoredPage(getClient(), url, rule);
    }

    public static void addMonitoredPage(Client client, String url, DropwizardAppRule<LinkCheckerConfiguration> rule) {
        getResource(client, TestUtils.mpPrefix + url, rule)
                .entity(new MonitoredPage(url), MediaType.APPLICATION_JSON)
                .put();
    }

    public static MonitoredPage getMonitoredPage(String url, DropwizardAppRule<LinkCheckerConfiguration> rule) {
        return getResource(getClient(), TestUtils.mpPrefix + url, rule)
                .get(MonitoredPage.class);
    }

    public static void updateMonitoredPage(String url, MonitoredPage.Status newState, DropwizardAppRule<LinkCheckerConfiguration> rule) {
        MonitoredPage updatedPage = new MonitoredPage(url, newState);

        getResource(getClient(), TestUtils.mpPrefix + url, rule)
                .entity(updatedPage, MediaType.APPLICATION_JSON)
                .put();
    }
}