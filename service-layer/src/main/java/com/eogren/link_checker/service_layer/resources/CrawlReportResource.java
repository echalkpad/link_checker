package com.eogren.link_checker.service_layer.resources;

import com.codahale.metrics.annotation.Timed;
import com.eogren.link_checker.service_layer.api.APIStatus;
import com.eogren.link_checker.service_layer.api.CrawlReport;
import com.eogren.link_checker.service_layer.data.CrawlReportRepository;

import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.Optional;

@Path("/api/v1/crawl_report/")
@Produces(MediaType.APPLICATION_JSON)
public class CrawlReportResource {

    private final CrawlReportRepository repo;

    public CrawlReportResource(CrawlReportRepository repo) {
        this.repo = repo;
    }

    @POST
    @Timed
    public Response newCrawlReport(@Valid CrawlReport crawlReport) {
        String uuid = repo.addCrawlReport(crawlReport);

        URI uri = UriBuilder.fromResource(this.getClass()).path("{uuid}").build(uuid);

        return Response.created(uri).entity(new APIStatus(true, "Added")).build();
    }

    @GET
    @Path("/{url}/{uuid}")
    public Response getCrawlReport(@PathParam("url") String url,
                                      @PathParam("uuid") String uuid) {
        Optional<CrawlReport> cr = repo.getByUuid(url, uuid);
        if (!cr.isPresent()) {
            return Response.status(404).entity(new APIStatus(false, "Not found")).build();
        }

        return Response.ok(cr).build();
    }

}
