package com.eogren.link_checker.service_layer.resources;

import com.codahale.metrics.annotation.Timed;
import com.eogren.link_checker.service_layer.api.APIStatus;
import com.eogren.link_checker.service_layer.api.APIStatusException;
import com.eogren.link_checker.service_layer.api.Page;
import com.eogren.link_checker.service_layer.data.RootPageRepository;

import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
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
    public List<Page> getListing() {
        return repository.getAllRootPages();
    }

    @PUT
    @Timed
    @Path("{url: .*}")
    /**
     * Add a new Page to the system. The root page URL is used as the key.
     * TODO: Normalize URLs
     */
    public APIStatus newRootPage(@PathParam("url") String url,
                                 @Valid Page newPage) {
        if (!url.equals(newPage.getUrl())) {
            throw new APIStatusException(
                    new APIStatus(false, String.format("Key from URL %s does not match key for object %s",
                            url,
                            newPage.getUrl())),
                    400
            );
        }

        repository.addPage(newPage);
        return new APIStatus(true, String.format("Successfully added %s", url));

    }

    @DELETE
    @Timed
    @Path("{url: .*}")
    /**
     * Delete a Page from the system. The root page URL is used as the key.
     */
    public APIStatus deleteRootPage(@PathParam("url") String url) {
        if (!repository.pageExists(url)) {
            throw new APIStatusException(new APIStatus(false, "Resource not found"), 404);
        }

        repository.deletePage(url);
        return new APIStatus(true, "Deleted successfully.");
    }


    @GET
    @Timed
    @Path("{url: .*}")
    public APIStatus testing(@PathParam("url") String url) {
        return new APIStatus(true, String.format("%s", url));
    }

}
