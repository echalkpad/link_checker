package com.eogren.link_checker.service_layer.tests;

import com.eogren.link_checker.service_layer.core.RootPage;
import com.eogren.link_checker.service_layer.data.RootPageRepository;
import com.eogren.link_checker.service_layer.resources.RootPageResource;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by eric on 7/3/14.
 */
public class RootPageResourceTest {
    private class MockRootPageRepository implements RootPageRepository {

        @Override
        public List<RootPage> getAllRootPages() {
            RootPage rp1 = new RootPage("http://www.cnn.com");
            RootPage rp2 = new RootPage("http://www.nytimes.com");

            ArrayList<RootPage> ret = new ArrayList<>();
            ret.add(rp1);
            ret.add(rp2);

            return ret;
        }
    }

    private RootPageRepository repository;

    @Before
    public void setup() {
        repository = new MockRootPageRepository();
    }

    @Test
    public void testRootPageResourceSummaryReturnsUrls() {
        RootPageResource sut = new RootPageResource(repository);
        List<RootPage> from_sut = sut.getListing();

        assertEquals(from_sut.get(0), new RootPage("http://www.cnn.com"));
        assertEquals(from_sut.get(1), new RootPage("http://www.nytimes.com"));
    }
}
