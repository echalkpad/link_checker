package com.eogren.link_checker.service_layer.data;

import com.eogren.link_checker.service_layer.api.MonitoredPage;
import com.eogren.link_checker.service_layer.api.Page;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Simple RootPageRepository that keeps data in memory
 */
public class InMemoryMonitoredPageRepository implements MonitoredPageRepository {
    private Map<String, MonitoredPage> pages;

    public InMemoryMonitoredPageRepository() {
        pages = new ConcurrentHashMap<>();
    }

    @Override
    public List<MonitoredPage> getAllMonitoredPages() {
        return pages.values().stream().collect(Collectors.toList());
    }

    @Override
    public void addMonitoredPage(MonitoredPage newPage) {
        pages.put(newPage.getUrl(), newPage);
    }

    @Override
    public void deleteMonitoredPage(String url) {
        pages.remove(url);
    }

    @Override
    public boolean pageAlreadyMonitored(String url) {
        return pages.containsKey(url);
    }

    @Override
    public List<MonitoredPage> findByUrl(Collection<String> urls) {
        return pages.values().stream().filter(x -> urls.contains(x)).collect(Collectors.toList());
    }

    @Override
    public Optional<MonitoredPage> findByUrl(String url) {
        List<String> urls = new ArrayList<>();
        urls.add(url);

        List<MonitoredPage> ret = findByUrl(urls);
        if (ret.size() == 0) {
            return Optional.empty();
        } else {
            return Optional.of(ret.get(0));
        }
    }
}
