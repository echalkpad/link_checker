package com.eogren.link_checker.service_layer.data;

import com.codahale.metrics.annotation.Timed;
import com.datastax.driver.core.*;

import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.eogren.link_checker.service_layer.api.MonitoredPage;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class CassandraMonitoredPageRepository implements MonitoredPageRepository {
    protected static final Logger logger = LoggerFactory.getLogger(CassandraMonitoredPageRepository.class);

    protected Session session;

    protected PreparedStatement preparedInsertStatement;
    protected PreparedStatement preparedDeleteStatement;
    protected PreparedStatement preparedExistsStatement;

    public CassandraMonitoredPageRepository(Session session) {
        this(session, 20);
    }

    public CassandraMonitoredPageRepository(Session session, int numThreads) {
        this.session = session;
    }

    @Override
    @Timed
    public List<MonitoredPage> getAllMonitoredPages() {
        logger.debug("Retrieving all monitored pages");
        ArrayList<MonitoredPage> ret = new ArrayList<>();

        ResultSet rs = session.execute("SELECT url, last_updated, status FROM root_page;");

        for (Row r : rs) {
            ret.add(createMonitoredPageFromRow(r));
        }

        return ret;
    }

    @Override
    @Timed
    public void addMonitoredPage(MonitoredPage newPage) {
        if (newPage == null) {
            throw new IllegalArgumentException("newPage cannot be null");
        }

        BoundStatement bs = new BoundStatement(getPreparedInsertStatement());
        logger.debug(String.format("About to insert %s", newPage.toString()));

        session.execute(bs.bind(newPage.getUrl(), newPage.getLastUpdated().toDate(), statusToInt(newPage.getStatus())));
    }

    @Override
    @Timed
    public void deleteMonitoredPage(String url) {
        BoundStatement bs = new BoundStatement(getPreparedDeleteStatement());
        logger.debug(String.format("Deleting monitored page %s", url));
        session.execute(bs.bind(url));
    }

    @Override
    @Timed
    public boolean pageAlreadyMonitored(String url) {
        // XXX intentionally doing extra work here just to keep code
        // simpler; don't actually need to deserialize a MonitoredPage out of the
        // row
        Optional<MonitoredPage> mp = findByUrl(url);
        return mp.isPresent();

    }

    @Override
    @Timed
    public List<MonitoredPage> findByUrl(Collection<String> urls) {
        List<MonitoredPage> ret = new ArrayList<>();
        String[] urlArr = new String[urls.size()];
        urls.toArray(urlArr);

        String s = QueryBuilder
                .select("url")
                .from("root_page")
                .where(QueryBuilder.in("url", urlArr))
                .getQueryString();

        ResultSet rows = session.execute(s, urlArr);
        for (Row r : rows) {
            ret.add(new MonitoredPage(r.getString("url")));
        }

        return ret;
    }

    @Override
    @Timed
    public Optional<MonitoredPage> findByUrl(String url) {
        BoundStatement bs = new BoundStatement(getOneUrl());
        ResultSet rs = session.execute(bs.bind(url));

        Row r = rs.one();
        if (r == null) {
            return Optional.empty();
        }

        return Optional.of(createMonitoredPageFromRow(r));
    }

    protected int statusToInt(MonitoredPage.Status status) {
        return status.ordinal();
    }

    protected MonitoredPage.Status statusFromInt(int val) {
        return MonitoredPage.Status.values()[val];
    }

    protected MonitoredPage createMonitoredPageFromRow(Row r) {
        MonitoredPage ret = new MonitoredPage(
                r.getString("url"),
                statusFromInt(r.getInt("status")),
                new DateTime(r.getDate("last_updated")));

        logger.debug(String.format("Deserializing row url: %s status: %d date: %s ret: %s",
                r.getString("url"),
                r.getInt("status"),
                r.getDate("last_updated"),
                ret.toString()));

        return ret;
    }

    protected PreparedStatement getPreparedInsertStatement() {
        if (preparedInsertStatement == null) {
            preparedInsertStatement = session.prepare("INSERT INTO root_page (url, last_updated, status) VALUES (?, ?, ?);");
        }

        return preparedInsertStatement;
    }

    protected PreparedStatement getPreparedDeleteStatement() {
        if (preparedDeleteStatement == null) {
            preparedDeleteStatement = session.prepare("DELETE FROM root_page WHERE url = ?;");
        }

        return preparedDeleteStatement;
    }

    protected PreparedStatement getOneUrl() {
        if (preparedExistsStatement == null) {
            preparedExistsStatement = session.prepare("SELECT url, last_updated, status from root_page WHERE url = ?;");
        }

        return preparedExistsStatement;
    }
}
