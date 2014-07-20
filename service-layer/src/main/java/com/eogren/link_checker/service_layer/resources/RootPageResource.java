package com.eogren.link_checker.service_layer.resources;

import com.codahale.metrics.annotation.Timed;
import com.eogren.link_checker.service_layer.api.APIStatus;
import com.eogren.link_checker.service_layer.api.RootPage;
import com.eogren.link_checker.service_layer.data.RootPageRepository;

import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.PathSegment;
import java.util.List;

@Path("/api/v1/root_page/")
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

    @PUT
    @Timed
    @Path("{url: .*}")
    public APIStatus newRootPage(@PathParam("url") String url,
                                 @Valid RootPage newPage) {
        // TODO: Throw if url doesn't match? Or use key and ignore body

        repository.addPage(newPage);
        return new APIStatus(true, String.format("Successfully added %s", url));

    }

    @GET
    @Timed
    @Path("{url: .*}")
    public APIStatus testing(@PathParam("url") String url) {
        return new APIStatus(true, String.format("%s", url));
    }

}
