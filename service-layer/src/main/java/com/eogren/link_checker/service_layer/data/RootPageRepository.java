package com.eogren.link_checker.service_layer.data;

import com.eogren.link_checker.service_layer.api.RootPage;

import java.util.List;


/**
 * RootPageRepository is the interface for an object that knows how to retrieve information
 * about root pages from some sort of storage.
 */
public interface RootPageRepository {
    /**
     * Retrieve all known root pages.
     * @return A list of RootPages contained in the repository.
     */
    List<RootPage> getAllRootPages();

    /**
     * Add a new page to the repository.
     * TODO: Should throw exception
     * @param newPage New page to add.
     */
    void addPage(RootPage newPage);
}
