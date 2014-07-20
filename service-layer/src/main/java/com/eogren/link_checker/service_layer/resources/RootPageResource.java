package com.eogren.link_checker.service_layer.resources;

import com.codahale.metrics.annotation.Timed;
import com.eogren.link_checker.service_layer.api.APIStatus;
import com.eogren.link_checker.service_layer.api.APIStatusException;
import com.eogren.link_checker.service_layer.api.RootPage;
import com.eogren.link_checker.service_layer.data.RootPageRepository;

import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.PathSegment;
import java.util.List;

@Path("/api/v1/root_page/")
@Produces(MediaType.APPLICATION_JSON)
/**
 * RootPageResource represents the API calls necessary to deal with RootPages.
 */
public class RootPageResource {
    private final RootPageRepository repository;

    /**
     * Create a RootPageResource handler that will use the given repository.
     * @param repository Repository that stores/retrieves information about Root Pages.
     */
    public RootPageResource(RootPageRepository repository) {
        this.repository = repository;
    }

    @GET
    @Timed
    /**
     * Return a list of all RootPages in the system.
     * TODO: Pagination
     */
    public List<RootPage> getListing() {
        return repository.getAllRootPages();
    }

    @PUT
    @Timed
    @Path("{url: .*}")
    /**
     * Add a new RootPage to the system. The root page URL is used as the key.
     * TODO: Normalize URLs
     */
    public APIStatus newRootPage(@PathParam("url") String url,
                                 @Valid RootPage newPage) {
        if (!url.equals(newPage.getUrl())) {
            throw new APIStatusException(
                    new APIStatus(false, String.format("Key from URL %s does not match key fro object %s",
                            url,
                            newPage.getUrl())),
                    400
            );
        }

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
