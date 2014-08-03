package com.eogren.link_checker.service_layer.data;

import com.datastax.driver.core.*;

import com.eogren.link_checker.service_layer.api.Page;

import java.util.ArrayList;
import java.util.List;

public class CassandraRootPageRepository implements RootPageRepository {
    protected Session session;

    protected PreparedStatement preparedInsertStatement;
    protected PreparedStatement preparedDeleteStatement;
    protected PreparedStatement preparedExistsStatement;

    public CassandraRootPageRepository(Session session) {
        this.session = session;
        preparedInsertStatement = session.prepare("INSERT INTO root_page (url) VALUES (?);");
        preparedDeleteStatement = session.prepare("DELETE FROM root_page WHERE url = ?;");
        preparedExistsStatement = session.prepare("SELECT url from root_page WHERE url = ?;");
    }

    @Override
    public List<Page> getAllRootPages() {
        ArrayList<Page> ret = new ArrayList<>();

        ResultSet rs = session.execute("SELECT url from root_page;");
        for(Row r : rs) {
            ret.add(new Page(r.getString("url"), true, null));
        }

        return ret;
    }

    @Override
    public void addPage(Page newPage) {
        BoundStatement bs = new BoundStatement(preparedInsertStatement);
        session.execute(bs.bind(newPage.getUrl()));
    }

    @Override
    public void deletePage(String url) {
        BoundStatement bs = new BoundStatement(preparedDeleteStatement);
        session.execute(bs.bind(url));
    }

    @Override
    public boolean pageExists(String url) {
        BoundStatement bs = new BoundStatement(preparedExistsStatement);
        ResultSet rs = session.execute(bs.bind(url));

        return (rs.one() != null);
    }
}
