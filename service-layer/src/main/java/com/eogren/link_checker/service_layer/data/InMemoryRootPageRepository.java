package com.eogren.link_checker.service_layer.data;

import com.eogren.link_checker.service_layer.api.Page;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Simple RootPageRepository that keeps data in memory
 */
public class InMemoryRootPageRepository implements RootPageRepository {
    private Map<String, Page> pages;

    public InMemoryRootPageRepository() {
        pages = new ConcurrentHashMap<>();
        pages.put("http://www.cnn.com", createPage("http://www.cnn.com"));
        pages.put("http://www.nytimes.com", createPage("http://www.nytimes.com"));
    }


    @Override
    public List<Page> getAllRootPages() {
        return pages.values().stream().collect(Collectors.toList());
    }

    @Override
    public void addPage(Page newPage) {
        pages.put(newPage.getUrl(), newPage);
    }

    @Override
    public void deletePage(String url) {
        pages.remove(url);
    }

    @Override
    public boolean pageExists(String url) {
        return pages.containsKey(url);
    }

    protected Page createPage(String url) {
        return new Page(url, true, null);
    }
}
