package com.eogren.link_checker.service_layer.tests;

import com.eogren.link_checker.service_layer.api.APIStatusException;
import com.eogren.link_checker.service_layer.api.Page;
import com.eogren.link_checker.service_layer.data.RootPageRepository;
import com.eogren.link_checker.service_layer.resources.RootPageResource;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class RootPageResourceTest {
    private class MockRootPageRepository implements RootPageRepository {
        private List<Page> pages;

        public MockRootPageRepository(String[] urls) {
            pages = new ArrayList<>();

            for (String url : urls) {
                addPage(createPage(url));
            }
        }

        @Override
        public List<Page> getAllRootPages() {
            return pages;
        }

        @Override
        public void addPage(Page page) {
            pages.add(page);
        }

        @Override
        public boolean pageExists(String url) {
            return pages.stream().anyMatch(rp -> rp.getUrl().equals(url));
        }

        @Override
        public void deletePage(String url) {
            pages = pages.stream().filter(rp -> !rp.getUrl().equals(url)).collect(Collectors.toList());
        }

    }

    private RootPageResource sut;
    private MockRootPageRepository repository;
    private String[] initialUrls;

    @Before
    public void setup() {
        initialUrls = new String[] {
                "http://www.cnn.com",
                "http://www.nytimes.com"
        };
        repository = new MockRootPageRepository(initialUrls);
        sut = new RootPageResource(repository);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testRootPageResourceSummaryReturnsUrls() {
        Request mockRequest = mock(Request.class);
        when(mockRequest.evaluatePreconditions()).thenReturn(null);

        Response from_sut = sut.getListing(mockRequest);
        List<Page> sut_urls = (List<Page>) from_sut.getEntity();
        for (String url : initialUrls) {
            assertUrlInList(sut_urls, url);
        }
    }

    @Test
    public void testRootPageResourceNewAddsANewUrl() {
        String newPageUrl = "http://www.newpage.com";

        Page newPage = createPage(newPageUrl);

        sut.newRootPage(newPage.getUrl(), newPage);

        List<Page> allPages = repository.getAllRootPages();
        assertUrlInList(allPages, newPageUrl);
    }

    @Test(expected = APIStatusException.class)
    public void testRootPageResourceNewThrowsErrorOnMismatchedUrl() {
        String mismatchedUrl = "http://www.newpage.com";

        Page newPage = createPage("http://www.a.different.url");
        sut.newRootPage(mismatchedUrl, newPage);
    }

    @Test
    public void testRootPageResourceDeletesANewUrl() {
        String urlToDelete = "http://www.nytimes.com";

        sut.deleteRootPage(urlToDelete);
        assertUrlNotInList(repository.getAllRootPages(), urlToDelete);
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

    protected void assertUrlInList(List<Page> list, String urlToFind) {
        assertTrue(
                String.format("Expected to find %s in list but did not", urlToFind),
                findUrlInList(list, urlToFind)
        );
    }

    protected void assertUrlNotInList(List<Page> list, String urlToFind) {
        assertFalse(
                String.format("Expected %s to not be in list but found it", urlToFind),
                findUrlInList(list, urlToFind)
        );
    }

    protected boolean findUrlInList(List<Page> list, String urlToFind) {
        for (Page rp : list) {
            if (rp.getUrl().equals(urlToFind)) {
                return true;
            }
        }

        return false;
    }

    protected Page createPage(String url) {
        return new Page(url, true, null);
    }
}
