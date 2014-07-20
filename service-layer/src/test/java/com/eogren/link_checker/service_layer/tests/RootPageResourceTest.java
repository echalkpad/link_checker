package com.eogren.link_checker.service_layer.tests;

import com.eogren.link_checker.service_layer.api.APIStatusException;
import com.eogren.link_checker.service_layer.api.RootPage;
import com.eogren.link_checker.service_layer.data.RootPageRepository;
import com.eogren.link_checker.service_layer.resources.RootPageResource;

import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.PathSegment;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RootPageResourceTest {
    private class MockRootPageRepository implements RootPageRepository {
        private ArrayList<RootPage> pages;

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

    }

    private MockRootPageRepository repository;
    private String[] initialUrls;

    @Before
    public void setup() {
        initialUrls = new String[] {
                "http://www.cnn.com",
                "http://www.nytimes.com"
        };
        repository = new MockRootPageRepository(initialUrls);
    }

    @Test
    public void testRootPageResourceSummaryReturnsUrls() {
        RootPageResource sut = new RootPageResource(repository);
        List<RootPage> from_sut = sut.getListing();

        for (String url : initialUrls) {
            findUrlInList(from_sut, url);
        }
    }

    @Test
    public void testRootPageResourceNewAddsANewUrl() {
        RootPageResource sut = new RootPageResource(repository);
        String newPageUrl = "http://www.newpage.com";

        RootPage newPage = new RootPage(newPageUrl);

        sut.newRootPage(newPage.getUrl(), newPage);

        List<RootPage> allPages = repository.getAllRootPages();
        findUrlInList(allPages, newPageUrl);
    }

    @Test(expected = APIStatusException.class)
    public void testRootPageResourceNewThrowsErrorOnMismatchedUrl() {
        RootPageResource sut = new RootPageResource(repository);
        String mismatchedUrl = "http://www.newpage.com";

        RootPage newPage = new RootPage("http://www.a.different.url");
        sut.newRootPage(mismatchedUrl, newPage);
    }

    protected void findUrlInList(List<RootPage> list, String urlToFind) {
        for (RootPage rp : list) {
            if (rp.getUrl() == urlToFind) {
                return;
            }
        }

        fail(String.format("Expected to find %s in list but did not", urlToFind));
    }
}
