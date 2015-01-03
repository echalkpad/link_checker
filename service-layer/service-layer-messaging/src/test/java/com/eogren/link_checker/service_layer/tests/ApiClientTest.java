package com.eogren.link_checker.service_layer.tests;

import com.eogren.link_checker.service_layer.api.MonitoredPage;
import com.eogren.link_checker.service_layer.client.ApiClient;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockserver.client.proxy.ProxyClient;
import org.mockserver.client.server.MockServerClient;
import org.mockserver.junit.MockServerRule;
import org.mockserver.junit.ProxyRule;
import org.mockserver.model.Header;

import java.io.IOException;
import java.util.Optional;

import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.junit.Assert.*;

public class ApiClientTest {
    private MockServerClient mockServerClient;
    private ProxyClient proxyClient;
    private ApiClient apiClient;

    @Rule
    public MockServerRule mockServerRule = new MockServerRule(9999, this);

    @Before
    public void initApiClient() {
        apiClient = new ApiClient("http://localhost:9999");
    }

    @Test
    public void testGetOneMonitoredPage404() throws Exception {
        mockServerClient
                .when(request().withPath("/api/v1/monitored_page/http://idontexist.com"))
                .respond(
                        response()
                                .withStatusCode(404)
                                .withBody("{ status: false }")
                );


        Optional<MonitoredPage> mp = apiClient.getMonitoredPage("http://idontexist.com");
        assertFalse("Expected an empty MonitoredPage object", mp.isPresent());
    }

    @Test
    public void testMonitoredPageValid() throws Exception {
        mockServerClient
                .when(request().withPath("/api/v1/monitored_page/http://idoexist.com"))
                .respond(
                        response()
                        .withStatusCode(200)
                        .withHeader(new Header("Content-Type", "application/json"))
                        .withBody("{\"url\":\"http://idoexist.com\",\"status\":\"GOOD\",\"lastUpdated\":1418549840262}")

                );

        Optional<MonitoredPage> mp = apiClient.getMonitoredPage("http://idoexist.com");
        assertTrue("Expected to get a MonitoredPage object", mp.isPresent());
        assertEquals("Expected URLs to match", "http://idoexist.com", mp.get().getUrl());
    }
}