package com.eogren.link_checker.service_layer.data;

import com.eogren.link_checker.service_layer.core.RootPage;

import java.util.List;

/**
 * Created by eric on 7/3/14.
 */
public interface RootPageRepository {
    List<RootPage> getAllRootPages();
}
