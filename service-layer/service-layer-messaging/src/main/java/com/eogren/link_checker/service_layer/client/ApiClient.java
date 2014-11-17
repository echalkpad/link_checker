package com.eogren.link_checker.service_layer.client;

import com.eogren.link_checker.service_layer.api.CrawlReport;
import com.eogren.link_checker.service_layer.api.MonitoredPage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.util.List;

/**
 * The ApiClient is used to contact the service layer API.
 */
public class ApiClient {
    private final String baseUrl;
    private final CloseableHttpClient httpClient;


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
        httpClient = HttpClients.createDefault();
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
        HttpGet req = new HttpGet(getUrl("/api/v1/monitored_page/search?links_to=" + url));
        try (CloseableHttpResponse response = httpClient.execute(req)) {
            if (response.getStatusLine().getStatusCode() > 299) {
                throw new IOException("API request to " + req.getURI().toString() + " returned error: " + response.getStatusLine().toString());
            }

            HttpEntity entity = response.getEntity();

            ObjectMapper mapper = new ObjectMapper();

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

            ObjectMapper mapper = new ObjectMapper();

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
    public void updateMonitoredPageStatus(MonitoredPage page, MonitoredPage.Status pageStatus) {

    }

    protected String getUrl(String path) {
        return baseUrl + "/" + path;
    }


}

