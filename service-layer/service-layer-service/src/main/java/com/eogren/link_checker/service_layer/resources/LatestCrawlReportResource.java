package com.eogren.link_checker.service_layer.resources;

import com.codahale.metrics.annotation.Timed;
import com.eogren.link_checker.service_layer.api.APIStatus;
import com.eogren.link_checker.service_layer.api.APIStatusException;
import com.eogren.link_checker.service_layer.api.CrawlReport;
import com.eogren.link_checker.service_layer.data.CrawlReportRepository;
import com.wordnik.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Optional;

@Path("/api/v1/latest_crawl_report")
@Api(value="/api/v1/latest_crawl_report", description="Retrieve the latest crawl reports for a page")
@Produces(MediaType.APPLICATION_JSON)
public class LatestCrawlReportResource {
    private static Logger logger = LoggerFactory.getLogger(LatestCrawlReportResource.class);

    private final CrawlReportRepository repo;

    public LatestCrawlReportResource(CrawlReportRepository repo) {
        this.repo = repo;
    }

    @GET
    @Timed
    @Path("/{url: .*}")
    @ApiOperation(value = "Retrieve latest crawl report for a page")
    @ApiResponses(value = { @ApiResponse(code=404, message="No crawl reports exist for that page")})
    public CrawlReport getOne(@ApiParam(value="URL to retrieve", required=true) @PathParam("url") String url) {
        Optional<CrawlReport> report = repo.getLatestStatus(url);

        if (!report.isPresent()) {
            throw new APIStatusException(
                    new APIStatus(false, "No crawl reports exist for " + url), 404
            );
        }

        return report.get();
    }
}
