package com.eogren.link_checker.service_layer.resources;

import com.codahale.metrics.annotation.Timed;
import com.eogren.link_checker.service_layer.api.APIStatus;
import com.eogren.link_checker.service_layer.api.CrawlReport;
import com.eogren.link_checker.service_layer.api.Link;
import com.eogren.link_checker.service_layer.data.CrawlReportRepository;
import com.eogren.link_checker.messaging.producer.MessageEmitter;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@Path("/api/v1/crawl_report")
@Api(value="/api/v1/crawl_report", description="Operations about crawl reports")
@Produces(MediaType.APPLICATION_JSON)
public class CrawlReportResource {
    private static Logger logger = LoggerFactory.getLogger(CrawlReportResource.class);

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

        msgEmitter.notifyCrawlReport(oldCrawlReport, crawlReport);

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

    @GET
    @Timed
    @Path("/search")
    @ApiOperation(value="Search for a crawl report")
    public List<CrawlReport> search(
            @ApiParam(value="Crawl reports that have links from") @QueryParam("links_from") String linksFrom
    ) {
        // get latest crawl report for linksFrom
        Optional<CrawlReport> cr = repo.getLatestStatus(linksFrom);
        if (!cr.isPresent()) {
            logger.debug("No crawl report for " + linksFrom);
            return new ArrayList<>();
        }

        List<String> urls = cr.get().getLinks().stream().map(Link::getUrl).collect(Collectors.toList());
        List<CrawlReport> link_crs = repo.getLatestStatus(urls);

        // Pages implicitly link to themselves - merge the two lists.
        List<CrawlReport> ret = new ArrayList<>();
        ret.add(cr.get());
        ret.addAll(link_crs);

        return ret;
    }


}
