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
    }


    @Override
    public List<RootPage> getAllRootPages() {
        return pages;
    }
}
