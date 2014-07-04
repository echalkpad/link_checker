package com.eogren.link_checker.service_layer.resources;

import com.codahale.metrics.annotation.Timed;
import com.eogren.link_checker.service_layer.core.RootPage;
import com.eogren.link_checker.service_layer.data.RootPageRepository;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by eric on 7/3/14.
 */
@Path("/api/v1/root_page")
@Produces(MediaType.APPLICATION_JSON)
public class RootPageResource {
    private final RootPageRepository repository;

    public RootPageResource(RootPageRepository repository) {
        this.repository = repository;
    }

    @GET
    @Timed
    public List<RootPage> getListing() {
        return repository.getAllRootPages();
    }

}
