package com.eogren.link_checker.service_layer.resources;

import com.eogren.link_checker.service_layer.api.APIStatus;
import com.eogren.link_checker.service_layer.api.CrawlReport;

import javax.validation.Valid;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/api/v1/crawl_report/")
@Produces(MediaType.APPLICATION_JSON)
public class CrawlReportResource {
    @PUT
    public APIStatus newCrawlReport(@Valid CrawlReport crawlReport) {
        return new APIStatus(true, "Added");
    }
}
