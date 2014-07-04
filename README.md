This app is an over-engineered 404 checking service. The service should
take a list of important pages to ping, and then every 5 minutes or so
scrape those pages and verify that all links on them are still valid.

It's also a good excuse to experiment with several languages (old or
new) that I have not worked with at all or in a while.

* Docker
* ReactJS
* Go
* Dropwizard/Java Web Services
* Cassandra?

General design:
  - Dropwizard app exposes information about pages we wish to search for
    404s (only depth 1 for now)
  - ReactJS admin app to interact with Dropwizard service to allow users
    to add new sites to examine
  - Go based scraper agent that relies on goroutines to parallelize web
    crawls. The scraper should be somewhat respectful of the site and
    not make more than ~10 requests a second.

