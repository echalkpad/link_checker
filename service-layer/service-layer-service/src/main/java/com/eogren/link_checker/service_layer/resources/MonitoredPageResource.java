package com.eogren.link_checker.service_layer.resources;

import com.codahale.metrics.annotation.Timed;
import com.eogren.link_checker.service_layer.api.APIStatus;
import com.eogren.link_checker.service_layer.api.APIStatusException;
import com.eogren.link_checker.service_layer.api.CrawlReport;
import com.eogren.link_checker.service_layer.api.MonitoredPage;
import com.eogren.link_checker.service_layer.data.CrawlReportRepository;
import com.eogren.link_checker.service_layer.data.MonitoredPageRepository;
import com.wordnik.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.hibernate.validator.constraints.NotEmpty;

import java.net.MalformedURLException;
import java.net.URL;

import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.*;

@Path("/api/v1/monitored_page")
@Api(value="/api/v1/monitored_page", description="Deal with monitored pages")
@Produces(MediaType.APPLICATION_JSON)
/**
 * RootPageResource represents the API calls necessary to deal with RootPages.
 */
public class MonitoredPageResource {
    private final MonitoredPageRepository monitoredPageRepository;
    private final CrawlReportRepository crawlReportRepository;
    private static final Logger logger = LoggerFactory.getLogger(MonitoredPageResource.class);
    /**
     * Create a RootPageResource handler that will use the given repository.
     *
     * @param monitoredPageRepository Repository that stores/retrieves information about Root Pages.
     */
    public MonitoredPageResource(MonitoredPageRepository monitoredPageRepository, CrawlReportRepository crawlReportRepository) {
        this.monitoredPageRepository = monitoredPageRepository;
        this.crawlReportRepository = crawlReportRepository;
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
        List<MonitoredPage> body = monitoredPageRepository.getAllMonitoredPages();

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

    @GET
    @Timed
    @Path("/{url: .*}")
    @ApiOperation(value = "Retrieve one Monitored Page")
    @ApiResponses(value = { @ApiResponse(code=404, message="Monitored Page doesn't exist")})
    public MonitoredPage getOne(@ApiParam(value="URL to retrieve", required=true) @PathParam("url") String url) {
        Optional<MonitoredPage> mp = monitoredPageRepository.findByUrl(url);

        if (!mp.isPresent()) {
            throw new APIStatusException(
                    new APIStatus(false, "Monitored Page does not exist"), 404
            );
        }

        return mp.get();
    }

    @POST
    @Timed
    @ApiOperation(value = "Add a new Monitored Page to the system")
    @ApiResponses(value = {
            @ApiResponse(code=422, message="URL not valid")
    })
    public APIStatus newMonitoredPage(@Valid MonitoredPage requestedPage) {
        final String url = requestedPage.getUrl();

        try {
            java.net.URL parsedUrl = new java.net.URL(url);
            String protocol = parsedUrl.getProtocol().toLowerCase();

            if (!protocol.equals("http") && !protocol.equals("https")) {
                throw new APIStatusException(
                        new APIStatus(false, String.format("%s does not start with http or https", url)), 422
                );
            }
        } catch (MalformedURLException e) {
            throw new APIStatusException(
                    new APIStatus(false, String.format("Could not parse %s as a URL: %s", url, e.getMessage())), 422
            );
        }

        MonitoredPage newPage = new MonitoredPage(url);
        monitoredPageRepository.addMonitoredPage(newPage);

        return new APIStatus(true, String.format("Successfully added %s", url));
    }

    @PUT
    @Timed
    @Path("/{url: .*}")
    @ApiOperation(value = "Update a new Monitored Page in the system")
    @ApiResponses(value = {
            @ApiResponse(code=404, message="URL does not exist"),
            @ApiResponse(code=405, message="Invalid Input"),
            @ApiResponse(code=422, message="Body could not be parsed"),
    })
    /**
     * Update a new Monitored Page in the system. The root page URL is used as the key.
     * TODO: Normalize URLs
     */
    public APIStatus updateMonitoredPage(@ApiParam(value = "URL to update", required = true) @PathParam("url") String url,
                                         @Valid MonitoredPage newPage) {
        if (!url.equals(newPage.getUrl())) {
            throw new APIStatusException(
                    new APIStatus(false, String.format("Key from URL %s does not match key for object %s",
                            url,
                            newPage.getUrl())),
                    400
            );
        }

        if (!monitoredPageRepository.pageAlreadyMonitored(url)) {
            throw new APIStatusException(
                    new APIStatus(false, String.format("%s does not exist", url)), 404
            );
        }

        if (newPage.getLastUpdated() == null) {
            throw new APIStatusException(
                    new APIStatus(false, "LastUpdated must be set in this request"), 422
            );
        }

        if (newPage.getStatus() == null) {
            throw new APIStatusException(
                    new APIStatus(false, "Status must be set in this request"), 422
            );
        }

        monitoredPageRepository.addMonitoredPage(newPage);
        return new APIStatus(true, String.format("Successfully updated %s", url));

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
        if (!monitoredPageRepository.pageAlreadyMonitored(url)) {
            throw new APIStatusException(new APIStatus(false, "Resource not found"), 404);
        }

        monitoredPageRepository.deleteMonitoredPage(url);
        return new APIStatus(true, "Deleted successfully.");
    }

    @GET
    @Timed
    @Path("/search")
    @ApiOperation(value="Search for monitored pages that meet the given criteria.")
    public List<MonitoredPage> searchForMonitoredPages(
            @ApiParam(value="Filter to pages that link to a given page", required=true)
            @NotEmpty
            @QueryParam("links_to") String links_to
    ) {
        // 1. Retrieve the list of crawled pages that link to links_to. Note that pages
        // implicitly link to themselves.
        //
        // 2. Intersect that list with the list of monitored pages
        //
        // TODO: This impl may be inefficient - # of monitored pages is probably smaller
        // than step 1.

        Set<String> crawled_pages = new HashSet<>(crawlReportRepository.getLatestLinksFor(links_to));
        crawled_pages.add(links_to);

        logger.debug(String.format("searchForMonitoredPages: crawled_pages is %s", crawled_pages.toString()));

        return monitoredPageRepository.findByUrl(crawled_pages);
    }
}
