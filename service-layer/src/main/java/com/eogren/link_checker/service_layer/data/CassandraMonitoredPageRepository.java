package com.eogren.link_checker.service_layer.data;

import com.codahale.metrics.annotation.Timed;
import com.datastax.driver.core.*;

import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.eogren.link_checker.service_layer.api.MonitoredPage;
import com.eogren.link_checker.service_layer.api.Page;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;

public class CassandraMonitoredPageRepository implements MonitoredPageRepository {
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
        ArrayList<MonitoredPage> ret = new ArrayList<>();

        ResultSet rs = session.execute("SELECT url from root_page;");

        for (Row r : rs) {
            ret.add(new MonitoredPage(r.getString("url")));
        }

        return ret;
    }

    @Override
    @Timed
    public void addMonitoredPage(MonitoredPage newPage) {
        BoundStatement bs = new BoundStatement(getPreparedInsertStatement());
        session.execute(bs.bind(newPage.getUrl()));
    }

    @Override
    @Timed
    public void deleteMonitoredPage(String url) {
        BoundStatement bs = new BoundStatement(getPreparedDeleteStatement());
        session.execute(bs.bind(url));
    }

    @Override
    @Timed
    public boolean pageAlreadyMonitored(String url) {
        BoundStatement bs = new BoundStatement(getPreparedExistsStatement());
        ResultSet rs = session.execute(bs.bind(url));

        return (rs.one() != null);
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

    protected PreparedStatement getPreparedInsertStatement() {
        if (preparedInsertStatement == null) {
            preparedInsertStatement = session.prepare("INSERT INTO root_page (url) VALUES (?);");
        }

        return preparedInsertStatement;
    }

    protected PreparedStatement getPreparedDeleteStatement() {
        if (preparedDeleteStatement == null) {
            preparedDeleteStatement = session.prepare("DELETE FROM root_page WHERE url = ?;");
        }

        return preparedDeleteStatement;
    }

    protected PreparedStatement getPreparedExistsStatement() {
        if (preparedExistsStatement == null) {
            preparedExistsStatement = session.prepare("SELECT url from root_page WHERE url = ?;");
        }

        return preparedExistsStatement;
    }
}
