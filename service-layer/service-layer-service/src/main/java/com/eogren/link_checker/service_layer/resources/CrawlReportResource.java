package com.eogren.link_checker.service_layer.resources;

import com.codahale.metrics.annotation.Timed;
import com.eogren.link_checker.service_layer.api.APIStatus;
import com.eogren.link_checker.service_layer.api.CrawlReport;
import com.eogren.link_checker.service_layer.data.CrawlReportRepository;
import com.eogren.link_checker.messaging.MessageEmitter;
import com.eogren.link_checker.messaging.ProtobufSerializer;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.Optional;

@Path("/api/v1/crawl_report")
@Api(value="/api/v1/crawl_report", description="Operations about crawl reports")
@Produces(MediaType.APPLICATION_JSON)
public class CrawlReportResource {

    private final CrawlReportRepository repo;
    private final MessageEmitter msgEmitter;

    public CrawlReportResource(CrawlReportRepository repo, MessageEmitter msgEmitter) {
        this.repo = repo;
        this.msgEmitter = msgEmitter;
    }

    @POST
    @Timed
    @ApiOperation(value="Submit a new crawl report", notes = "UUID of crawl report should be returned in response.")
    public Response newCrawlReport(@Valid CrawlReport crawlReport) {
        Optional<CrawlReport> oldCrawlReport = repo.getLatestStatus(crawlReport.getUrl());
        String uuid = repo.addCrawlReport(crawlReport);

        URI uri = UriBuilder.fromResource(this.getClass()).path("{uuid}").build(uuid);

        emitScraperMessage(oldCrawlReport, crawlReport);

        return Response.created(uri).entity(new APIStatus(true, "Added")).build();
    }

    @GET
    @Path("/{url}/{uuid}")
    @ApiOperation(value="Retrieve a crawl report")
    public Response getCrawlReport(@ApiParam(value="URL crawl report is for") @PathParam("url") String url,
                                   @ApiParam(value="UUID of crawl report") @PathParam("uuid") String uuid) {
        Optional<CrawlReport> cr = repo.getByUuid(url, uuid);
        if (!cr.isPresent()) {
            return Response.status(404).entity(new APIStatus(false, "Not found")).build();
        }

        return Response.ok(cr).build();
    }

    // XXX Doesn't really fit here, move to its own class
    private void emitScraperMessage(Optional<CrawlReport> oldCrawlReport, CrawlReport newCrawlReport) {
        byte[] buf = ProtobufSerializer.createScrapeUpdate(oldCrawlReport, newCrawlReport);
        msgEmitter.emitMessage("scrapeReports", newCrawlReport.getUrl(), buf);
    }

}