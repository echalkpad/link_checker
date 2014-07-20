package com.eogren.link_checker.service_layer.data;

import com.eogren.link_checker.service_layer.api.RootPage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Simple RootPageRepository that keeps data in memory
 */
public class InMemoryRootPageRepository implements RootPageRepository {
    private Map<String, RootPage> pages;

    public InMemoryRootPageRepository() {
        pages = new HashMap<>();
        pages.put("http://www.cnn.com", new RootPage("http://www.cnn.com"));
        pages.put("http://www.nytimes.com", new RootPage("http://www.nytimes.com"));
    }


    @Override
    public List<RootPage> getAllRootPages() {
        return pages.values().stream().collect(Collectors.toList());
    }

    @Override
    public void addPage(RootPage newPage) {
        pages.put(newPage.getUrl(), newPage);
    }
}
