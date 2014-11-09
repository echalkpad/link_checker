package com.eogren.link_checker.service_layer.resources;

import com.codahale.metrics.annotation.Timed;
import com.eogren.link_checker.service_layer.api.APIStatus;
import com.eogren.link_checker.service_layer.api.APIStatusException;
import com.eogren.link_checker.service_layer.api.MonitoredPage;
import com.eogren.link_checker.service_layer.data.MonitoredPageRepository;
import com.wordnik.swagger.annotations.*;

import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.ArrayList;
import java.util.List;

@Path("/api/v1/monitored_page")
@Api(value="/api/v1/monitored_page", description="Deal with monitored pages")
@Produces(MediaType.APPLICATION_JSON)
/**
 * RootPageResource represents the API calls necessary to deal with RootPages.
 */
public class MonitoredPageResource {
    private final MonitoredPageRepository repository;

    /**
     * Create a RootPageResource handler that will use the given repository.
     *
     * @param repository Repository that stores/retrieves information about Root Pages.
     */
    public MonitoredPageResource(MonitoredPageRepository repository) {
        this.repository = repository;
    }

    @GET
    @Timed
    @ApiOperation(value = "Retrieve all monitored pages in the system.",
            notes="This list can be large; the client can" +
                "cache the ETag returned by this method and use it to perform client-side caching.",
            response = MonitoredPage.class,
            responseContainer = "List")
    /**
     * Return a list of all Monitored Pages in the system.
     * TODO: Pagination
     */
    public Response getListing(@Context Request request) {
        // TODO: Could possibly use better caching too; this should stop the client from getting a bunch of data
        // when the ETag hasn't changed, but the server is still doing all the work to calculate it
        // in the first place.
        List<MonitoredPage> body = repository.getAllMonitoredPages();

        // TODO: Refactor etag/cc into own function
        EntityTag etag = new EntityTag(String.valueOf(body.hashCode()));

        CacheControl cc = new CacheControl();
        cc.setMaxAge(0);
        cc.setMustRevalidate(true);
        cc.setPrivate(true);

        Response.ResponseBuilder rb = request.evaluatePreconditions(etag);
        if (rb != null) {
            return rb.cacheControl(cc).build();
        } else {
            return Response.ok(body).cacheControl(cc).tag(etag).build();
        }
    }

    @PUT
    @Timed
    @Path("/{url: .*}")
    @ApiOperation(value = "Add a new Monitored Page to the system")
    @ApiResponses(value = { @ApiResponse(code=405, message="Invalid Input")})
    /**
     * Add a new Monitored Page to the system. The root page URL is used as the key.
     * TODO: Normalize URLs
     */
    public APIStatus newMonitoredPage(@ApiParam(value = "URL to add to the system", required=true) @PathParam("url") String url,
                                      @Valid MonitoredPage newPage) {
        if (!url.equals(newPage.getUrl())) {
            throw new APIStatusException(
                    new APIStatus(false, String.format("Key from URL %s does not match key for object %s",
                            url,
                            newPage.getUrl())),
                    400
            );
        }

        repository.addMonitoredPage(newPage);
        return new APIStatus(true, String.format("Successfully added %s", url));

    }

    @DELETE
    @Timed
    @Path("/{url: .*}")
    @ApiOperation(value="Delete a Monitored Page from the system.")
    @ApiResponses(value={ @ApiResponse(code=404, message="Monitored Page does not exist")})
    /**
     * Delete a Page from the system. The root page URL is used as the key.
     */
    public APIStatus deleteRootPage(@ApiParam(value="URL to delete") @PathParam("url") String url) {
        if (!repository.pageAlreadyMonitored(url)) {
            throw new APIStatusException(new APIStatus(false, "Resource not found"), 404);
        }

        repository.deleteMonitoredPage(url);
        return new APIStatus(true, "Deleted successfully.");
    }

    @GET
    @Timed
    @Path("/search")
    @ApiOperation(value="Search for monitored pages that meet the given criteria.")
    public List<MonitoredPage> searchForMonitoredPages(
            @ApiParam(value="Filter to pages that link to a given page") @DefaultValue("") @QueryParam("links_to") String links_to
    ) {
        return new ArrayList<>();
    }
}
