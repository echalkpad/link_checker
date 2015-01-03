package com.eogren.link_checker.service_layer.client;

import com.eogren.link_checker.service_layer.api.CrawlReport;
import com.eogren.link_checker.service_layer.api.MonitoredPage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * The ApiClient is used to contact the service layer API.
 */
public class ApiClient {
    private final String baseUrl;
    private final CloseableHttpClient httpClient;
    protected final ObjectMapper mapper;


    /**
     * Create a new ApiClient
     *
     * @param baseUrl Base url (http, https, hostname, etc) of the API to talk to
     */
    public ApiClient(String baseUrl) {
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }

        this.baseUrl = baseUrl;

        SocketConfig sock_config = SocketConfig.custom().setSoTimeout(3000).build();

        RequestConfig req_config = RequestConfig.custom()
                .setSocketTimeout(3000)
                .setConnectTimeout(3000)
                .setConnectionRequestTimeout(3000)
                .setStaleConnectionCheckEnabled(true)
                .build();

        PoolingHttpClientConnectionManager poolingConnManager = new PoolingHttpClientConnectionManager();
        poolingConnManager.setMaxTotal(10);
        poolingConnManager.setDefaultMaxPerRoute(5);

        httpClient = HttpClients
                .custom()
                        .setConnectionManager(poolingConnManager)
                        .setDefaultRequestConfig(req_config)
                        .setDefaultSocketConfig(sock_config)
                        .build();
        mapper = new ObjectMapper();
    }

    /**
     * Retrieve all Monitored Pages in the system
     */
    public List<MonitoredPage> retrieveAllMonitoredPages() throws IOException {
        return getMonitoredPageListFromUrl(getUrl("/api/v1/monitored_page/"));
    }

    /**
     * Retrieve a single monitored page
     */
    public Optional<MonitoredPage> getMonitoredPage(String url) throws IOException {
        HttpGet req = new HttpGet(getUrl("/api/v1/monitored_page/" + url));
        try (CloseableHttpResponse response = httpClient.execute(req)) {
            if (response.getStatusLine().getStatusCode() == 404) {
                return Optional.empty();
            }

            if (response.getStatusLine().getStatusCode() > 299) {
                throw new IOException("API request to " + req.getURI().toString() + " returned error: " + response.getStatusLine().toString());
            }

            HttpEntity entity = response.getEntity();

            return Optional.of(
                    mapper.readValue(
                            entity.getContent(),
                            MonitoredPage.class)
            );
        }
    }

    /**
     * Retrieve a list of Monitored Page objects that link to the given URL
     *
     * @param url URL to search on
     * @return List of Monitored Pages that link to the given URL
     * @throws IOException TODO: Tests for error handling
     *                     TODO: Name is super long
     */
    public List<MonitoredPage> getMonitoredPagesThatLinkTo(String url) throws IOException {
       return getMonitoredPageListFromUrl(getUrl("/api/v1/monitored_page/search?links_to=" + url));
    }

    /**
     * Helper function to send a get request to a url and parse the result into a MonitoredPage list
     * @param url
     * @return
     * @throws IOException
     */
    protected List<MonitoredPage> getMonitoredPageListFromUrl(String url) throws IOException {
        HttpGet req = new HttpGet(url);
        try (CloseableHttpResponse response = httpClient.execute(req)) {
            if (response.getStatusLine().getStatusCode() > 299) {
                throw new IOException("API request to " + req.getURI().toString() + " returned error: " + response.getStatusLine().toString());
            }

            HttpEntity entity = response.getEntity();

            return mapper.readValue(
                    entity.getContent(),
                    mapper.getTypeFactory().constructCollectionType(List.class, MonitoredPage.class));
        }
    }
    /**
     * Retrieve the latest crawl reports for a URL + all of the pages it links to.
     *
     * @param url URL to search for
     * @return TODO: Name is super long
     */
    public List<CrawlReport> getLatestCrawlReportsFollowingLinksFor(String url) throws IOException {
        HttpGet req = new HttpGet(getUrl("/api/v1/crawl_report/search?links_from=" + url));
        try (CloseableHttpResponse response = httpClient.execute(req)) {
            if (response.getStatusLine().getStatusCode() > 299) {
                throw new IOException("API request to " + req.getURI().toString() + " returned error: " + response.getStatusLine().toString());
            }

            HttpEntity entity = response.getEntity();

            return mapper.readValue(
                    entity.getContent(),
                    mapper.getTypeFactory().constructCollectionType(List.class, CrawlReport.class));
        }
    }

    /** Update monitored page status for the given url.
     *
     * @param page MonitoredPage to update
     * @param pageStatus New status to set
     */
    public void updateMonitoredPageStatus(MonitoredPage page, MonitoredPage.Status pageStatus) throws IOException {
        MonitoredPage updateReq = new MonitoredPage(page.getUrl(), pageStatus);

        HttpPut req = new HttpPut(getUrl("/api/v1/monitored_page/" + updateReq.getUrl()));

        String json = mapper.writeValueAsString(updateReq);

        req.setEntity(new StringEntity(json, ContentType.APPLICATION_JSON));

        try (CloseableHttpResponse response = httpClient.execute(req)) {
            if (response.getStatusLine().getStatusCode() > 299) {
                throw new IOException("API request to " + req.getURI().toString() + " returned error: " + response.getStatusLine().toString());
            }
        }
    }

    protected String getUrl(String path) {
        return baseUrl + "/" + path;
    }


}

