This app is an over-engineered 404 checking service. The service should
take a list of important pages to ping, and then every 5 minutes or so
scrape those pages and verify that all links on them are still valid.

It's also a good excuse to experiment with several languages (old or
new) that I have not worked with at all or in a while.

* Docker
* ReactJS
* Go
* Dropwizard
* Cassandra

See docs/design.md for general design

To get integration tests to run, you must manually create the initial keyspace:
    
    create keyspace link_checker_int_tests with replication={'class':'SimpleStrategy', 'replication_factor':1}
    
The integration tests will create/drop CFs in this keyspace.
    