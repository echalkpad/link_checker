package com.eogren.link_checker.service_layer.data;

import com.eogren.link_checker.service_layer.api.MonitoredPage;

import java.util.Collection;
import java.util.List;

public interface MonitoredPageRepository {
    /**
     * Retrieve all known root pages.
     * @return A list of RootPages contained in the repository.
     */
    List<MonitoredPage> getAllMonitoredPages();

    /**
     * Filter a url list to those that are actually monitored page
     * @param urlList URLs to scan
     * @return Monitored page objects that correspond to the correct ones.
     */
    List<MonitoredPage> findByUrl(Collection<String> urlList);

    /**
     * Add a new page to the repository.
     * TODO: Should throw exception
     * @param newPage New page to add.
     */
    void addMonitoredPage(MonitoredPage newPage);

    /**
     * Delete a page from the repository.
     * @param url URL of the page to delete.
     */
    void deleteMonitoredPage(String url);

    /**
     * Checks whether a page exists in the repository.
     * @param url URL of the page to look for
     * @return True if found in the repository; false if not
     */
    boolean pageAlreadyMonitored(String url);
}
