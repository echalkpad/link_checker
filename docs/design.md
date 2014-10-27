# Link Checker Design Document

## Overview

The Link Checker service is a monitoring tool that monitors a set of URLs for broken links (where broken link is defined as something that does not return 200) and alerts a monitoring address if broken pages are detected.

## Functional Requirements

  1. Users must be able to add a list of monitored pages to the system that will be checked for broken links.
  
  1. Every 30 minutes (+ or minus a random offset so all requests aren't triggered at the same time), the Link Checker service should retrieve the contents of each monitored page. If the page is no longer reachable, an error should be triggered; if it is reachable then the system should retrieve all HTTP or HTTPS links from the monitored page.
  
  1. The Link Checker service should retrieve the contents of each page found in the previous step. If a page cannot be retrieved, then an error should be triggered.
  
  1. When a page and all of its direct descendants are reachable when they previously were not, the user should be notified that their page is no longer broken.
  
## System Architecture

 The Link Checker service consists of a few components:
 
 * **DB Abstraction Layer**
 
 The DB abstraction layer is responsible for saving or retrieving information that needs to be persisted in the system. For example, the list of monitored pages, or crawl report for a given page.
 
 * **Crawl Scheduler**
 
 The crawl scheduler is responsible for deciding when a URL should be crawled, and dispatching a message to the crawlers at the appropriate time. 
 
 * **Crawlers**
 
 Crawlers are responsible for retrieving the contents of a given web page, parsing links out of the page if it contains HTML, and then reporting the crawl status.
 
 * **Crawl Status Updater**

 After a given page is crawled, if the state has transitioned to broken the system needs to find all monitored pages that link to the crawled page and mark them as now broken; if the state has transitioned from broken -> successful, the system also needs to recalculate page status.
 
 The crawl status updater is responsible for this.
 
 * **Status Notifier**
 
 Emails users watching a monitored page if the status has gone red or green.
 
 * **Frontend**
 
 Allows users to add monitored pages / see their status in real time.
 
## Messaging Infrastructure
 
Kafka and Protobuf (mainly because Avro doesn't seem to have solid support for Go)

TODO: fillin details

## Crawler

The crawler is responsible for:

 * Listening to a Kafka topic for ScrapeRequests
 * Rate-limiting requests to a domain to N/sec (currently 3)
 * Retrieving a given URL [report error if code is not in the 200s]
 * Parsing the HTML output and extracting any href links
 
## Crawl Scheduler
 
## Status Updater

The status updater is responsible for processing CrawlReports for various URLs and updating the status of any monitored pages that link to said URL. If the CrawlReport is for a monitored URL itself then the status must also be updated.

The general algorithm upon processing a CrawlReport should look something like this:

 * [Optimization] Retrieve the last known status of the page the CrawlReport is for. If the last known status and the current status are the same, we don't need to do anything.
 
 * Retrieve the list of monitored pages and their current status. that link to the URL the CrawlReport is for. Note that every URL implicitly links to itself (eg if a monitored page can't be retrieved; its status should also be marked as broken)  
 * For each page:
 	* Retrieve latest CrawlReports for all links on it and set status appropriately (GOOD, CRAWL_IN_PROGRESS, BAD)
 	
To think about:

 * CrawlReports get written to Data API, which then fires CrawlReportAdded event -- how can optimization above be done?
   
 * Further optimizations possible -- monitored pages have a 'good', 'bad', and 'unknown' set of links, so then when a page flips we don't need to recheck all of the links on the page
 
 	* This makes processing a CrawlReport trickier and more expensive (because the PUT /CrawlReport operator needs to retrieve status info for any new links on the page) at the cost of not having to do as many reads when a given page changes status.
 	* And processing cost is only slightly more expensive unless a new link is discovered on a monitored page (or one goes away)

Status updater v1: Ignore any optimization tricks until we know they are needed. Just pull all links when a page changes status and update any monitored pages appropriately.