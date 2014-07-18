package com.eogren.link_checker.service_layer.data;

import com.eogren.link_checker.service_layer.core.RootPage;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple RootPageRepository that keeps data in memory
 */
public class InMemoryRootPageRepository implements RootPageRepository {
    private List<RootPage> pages;

    public InMemoryRootPageRepository() {
        pages = new ArrayList<>();
        pages.add(new RootPage("http://www.cnn.com"));
        pages.add(new RootPage("http://www.nytimes.com"));
    }


    @Override
    public List<RootPage> getAllRootPages() {
        return pages;
    }
}
