package com.eogren.link_checker.service_layer.tests;

import com.eogren.link_checker.service_layer.api.APIStatusException;
import com.eogren.link_checker.service_layer.api.MonitoredPage;
import com.eogren.link_checker.service_layer.api.Page;
import com.eogren.link_checker.service_layer.data.InMemoryMonitoredPageRepository;
import com.eogren.link_checker.service_layer.data.MonitoredPageRepository;
import com.eogren.link_checker.service_layer.resources.MonitoredPageResource;

import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class MonitoredPageResourceTest {

    private MonitoredPageRepository repo;
    private MonitoredPageResource sut;
    private String[] initialUrls;

    @Before
    public void setup() {
        initialUrls = new String[] {
                "http://www.cnn.com",
                "http://www.nytimes.com"
        };

        repo = new InMemoryMonitoredPageRepository();
        sut = new MonitoredPageResource(repo);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testRootPageResourceSummaryReturnsUrls() {
        Request mockRequest = mock(Request.class);
        when(mockRequest.evaluatePreconditions()).thenReturn(null);
        for (String url : initialUrls) {
            repo.addMonitoredPage(createMonitoredPage(url));
        }

        Response from_sut = sut.getListing(mockRequest);
        List<MonitoredPage> sut_urls = (List<MonitoredPage>) from_sut.getEntity();
        for (String url : initialUrls) {
            assertUrlInList(sut_urls, url);
        }
    }


    @Test
    public void testRootPageResourceNewAddsANewUrl() {
        String newPageUrl = "http://www.newpage.com";

        MonitoredPage newPage = createMonitoredPage(newPageUrl);

        sut.newMonitoredPage(newPage.getUrl(), newPage);

        List<MonitoredPage> allPages = repo.getAllMonitoredPages();
        assertUrlInList(allPages, newPageUrl);
    }

    @Test(expected = APIStatusException.class)
    public void testRootPageResourceNewThrowsErrorOnMismatchedUrl() {

        String mismatchedUrl = "http://www.newpage.com";

        MonitoredPage newPage = createMonitoredPage("http://www.a.different.url");
        sut.newMonitoredPage(mismatchedUrl, newPage);
    }

    @Test
    public void testRootPageResourceDeletesANewUrl() {
        for (String url : initialUrls) {
            repo.addMonitoredPage(createMonitoredPage(url));
        }

        String urlToDelete = initialUrls[0];

        sut.deleteRootPage(urlToDelete);
        assertUrlNotInList(repo.getAllMonitoredPages(), urlToDelete);
    }

    @Test
    public void testDeleteThrows404WhenKeyNotFound() {
        String urlToDelete = "http://www.idontexist.com";

        try {
            sut.deleteRootPage(urlToDelete);
            fail("Expected to catch APIStatusException but did not");
        } catch (APIStatusException e) {
            assertEquals(e.getResponse().getStatus(), 404);
        }
    }

    protected void assertUrlInList(List<MonitoredPage> list, String urlToFind) {
        assertTrue(
                String.format("Expected to find %s in list but did not", urlToFind),
                findUrlInList(list, urlToFind)
        );
    }

    protected void assertUrlNotInList(List<MonitoredPage> list, String urlToFind) {
        assertFalse(
                String.format("Expected %s to not be in list but found it", urlToFind),
                findUrlInList(list, urlToFind)
        );
    }

    protected boolean findUrlInList(List<MonitoredPage> list, String urlToFind) {
        for (MonitoredPage rp : list) {
            if (rp.getUrl().equals(urlToFind)) {
                return true;
            }
        }

        return false;
    }

    private MonitoredPage createMonitoredPage(String url) {
        return new MonitoredPage(url);
    }
}
