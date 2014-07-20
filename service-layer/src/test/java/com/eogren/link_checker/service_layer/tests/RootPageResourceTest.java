package com.eogren.link_checker.service_layer.tests;

import com.eogren.link_checker.service_layer.api.APIStatusException;
import com.eogren.link_checker.service_layer.api.RootPage;
import com.eogren.link_checker.service_layer.data.RootPageRepository;
import com.eogren.link_checker.service_layer.resources.RootPageResource;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class RootPageResourceTest {
    private class MockRootPageRepository implements RootPageRepository {
        private List<RootPage> pages;

        public MockRootPageRepository(String[] urls) {
            pages = new ArrayList<>();

            for (String url : urls) {
                addPage(new RootPage(url));
            }
        }

        @Override
        public List<RootPage> getAllRootPages() {
            return pages;
        }

        @Override
        public void addPage(RootPage page) {
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
    public void testRootPageResourceSummaryReturnsUrls() {
        List<RootPage> from_sut = sut.getListing();

        for (String url : initialUrls) {
            assertUrlInList(from_sut, url);
        }
    }

    @Test
    public void testRootPageResourceNewAddsANewUrl() {
        String newPageUrl = "http://www.newpage.com";

        RootPage newPage = new RootPage(newPageUrl);

        sut.newRootPage(newPage.getUrl(), newPage);

        List<RootPage> allPages = repository.getAllRootPages();
        assertUrlInList(allPages, newPageUrl);
    }

    @Test(expected = APIStatusException.class)
    public void testRootPageResourceNewThrowsErrorOnMismatchedUrl() {
        String mismatchedUrl = "http://www.newpage.com";

        RootPage newPage = new RootPage("http://www.a.different.url");
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

    protected void assertUrlInList(List<RootPage> list, String urlToFind) {
        assertTrue(
                String.format("Expected to find %s in list but did not", urlToFind),
                findUrlInList(list, urlToFind)
        );
    }

    protected void assertUrlNotInList(List<RootPage> list, String urlToFind) {
        assertFalse(
                String.format("Expected %s to not be in list but found it", urlToFind),
                findUrlInList(list, urlToFind)
        );
    }

    protected boolean findUrlInList(List<RootPage> list, String urlToFind) {
        for (RootPage rp : list) {
            if (rp.getUrl().equals(urlToFind)) {
                return true;
            }
        }

        return false;
    }
}
