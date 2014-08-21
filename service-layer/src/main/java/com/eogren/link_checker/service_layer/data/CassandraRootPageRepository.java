package com.eogren.link_checker.service_layer.data;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.codahale.metrics.annotation.Timed;
import com.datastax.driver.core.*;

import com.eogren.link_checker.service_layer.api.CrawlReport;
import com.eogren.link_checker.service_layer.api.Page;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;

public class CassandraRootPageRepository implements RootPageRepository {
    private final CrawlReportRepository crawlReportRepository;
    protected Session session;
    protected ExecutorService getWorkerService;

    protected PreparedStatement preparedInsertStatement;
    protected PreparedStatement preparedDeleteStatement;
    protected PreparedStatement preparedExistsStatement;

    private class RootPageRetriever implements Callable<Page> {
        private final String url;
        private final CrawlReportRepository crawlReportRepo;

        public RootPageRetriever(CrawlReportRepository crawlReportRepo, String url) {
            this.crawlReportRepo = crawlReportRepo;
            this.url = url;
        }

        public Page call() {
            Optional<CrawlReport> latestReportDetails = crawlReportRepo.getLatestStatus(url);

            return new Page(url, true, latestReportDetails.orElse(null));
        }
    }

    public CassandraRootPageRepository(CrawlReportRepository crawlReportRepository, Session session) {
        this(crawlReportRepository, session, 20);
    }

    public CassandraRootPageRepository(CrawlReportRepository crawlReportRepository, Session session, int numThreads) {
        this.session = session;
        this.crawlReportRepository = crawlReportRepository;

        preparedInsertStatement = session.prepare("INSERT INTO root_page (url) VALUES (?);");
        preparedDeleteStatement = session.prepare("DELETE FROM root_page WHERE url = ?;");
        preparedExistsStatement = session.prepare("SELECT url from root_page WHERE url = ?;");

        getWorkerService = Executors.newFixedThreadPool(numThreads);
    }

    @Override
    @Timed
    public List<Page> getAllRootPages() {
        ArrayList<Page> ret = new ArrayList<>();

        ResultSet rs = session.execute("SELECT url from root_page;");
        List<Future<Page>> futures = new ArrayList<>();

        // We retrieve rootPage information by going in and pulling all
        // the latest crawl reports; clear use case for caching
        for (Row r : rs) {
            FutureTask<Page> future = new FutureTask<>(new RootPageRetriever(crawlReportRepository, r.getString("url")));
            getWorkerService.execute(future);
            futures.add(future);
        }

        try {
            for (Future<Page> f : futures) {
                ret.add(f.get());
            }
        } catch (Exception e) {
            // XXX must be some better way to deal with interrupted/concurrent Exceptions
            throw new RuntimeException(e);
        }

        return ret;
    }

    @Override
    @Timed
    public void addPage(Page newPage) {
        BoundStatement bs = new BoundStatement(preparedInsertStatement);
        session.execute(bs.bind(newPage.getUrl()));
    }

    @Override
    @Timed
    public void deletePage(String url) {
        BoundStatement bs = new BoundStatement(preparedDeleteStatement);
        session.execute(bs.bind(url));
    }

    @Override
    @Timed
    public boolean pageExists(String url) {
        BoundStatement bs = new BoundStatement(preparedExistsStatement);
        ResultSet rs = session.execute(bs.bind(url));

        return (rs.one() != null);
    }
}
