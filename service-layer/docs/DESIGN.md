# Service-Layer design

## Overview

The service-layer contains most of the coordination code for the link_checker project. It is 
responsible for a few things:

* Keeping track of which pages should be monitored
* Persisting crawl reports from the Go scrapers into Cassandra
* Consuming the latest crawl reports to update the status of a monitored page
* (Eventually) Sending email notifications when the status of a monitored page changes

## Architecture

* MonitoredPages [XXX rename from RootPage] are added and deleted via simple REST CRUD resources. These are pages
that users want to check for 404s.

* Currently, the scraper process will download the list of monitored pages every 30 minutes and then go through
and crawl each monitored page as well as every link on a monitored page. [Crawl scheduling should really probably
be handled by a separate component]/

* The scraper posts a CrawlReport for each URL it crawls. These are POSTed to the service layer and persisted
in Cassandra.

* When a new CrawlReport comes in, we need to update the overall status of any Monitored Page that contains the link
(ie need to update the listing of whether that page has broken links or not). We do this every minute or so - this
 is just for performance reasons - since many crawl reports come in at once for a monitored page, we don't want
 to constantly recalculate the status.
 
* Eventually a process should consume overall stauts changes and email users.

## Endpoints

- /api/v1/monitored_page 
 Allows users to retrieve the list of monitored pages as well as add/delete them.
 
- /api/v1/page_status
 Allows users to retrieve the page status of a given page. Page statuses are generated
 by the system so only GET is supported here.
 
- /api/v1/crawl_report
 Used by the scraper to POST crawl reports
 
## Misc Notes

On CrawlRequest posted:
Can be dumb workers
  - Listener that inserts CrawlReq into Cassandra
  - Listener that updates inverted index of URLs

Needs state:
  - Listener that queues dirty updates
  - And fans out msgs


