package com.eogren.link_checker.service_layer.data;

import com.datastax.driver.core.*;
import com.eogren.link_checker.service_layer.api.CrawlReport;
import com.eogren.link_checker.service_layer.api.Link;
import com.eogren.link_checker.service_layer.api.Page;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.joda.time.DateTime;

import java.io.IOException;
import java.util.List;

public class CassandraCrawlReportRepository implements CrawlReportRepository {
    private final Session session;

    private final PreparedStatement insertCrawlReportStatement;
    private final PreparedStatement findLatestStatement;

    public CassandraCrawlReportRepository(Session session) {
        this.session = session;
        insertCrawlReportStatement = session.prepare(
                "INSERT INTO crawl_report (url, date, error, status_code, links) VALUES (?, ?, ?, ?, ?);"
        );

        findLatestStatement = session.prepare(
                "SELECT url, date, error, status_code, links FROM crawl_report WHERE url = ? ORDER BY date DESC LIMIT 1"
        );
    }

    @Override
    public void addCrawlReport(CrawlReport report) {
        ObjectMapper mapper = new ObjectMapper();

        try {
            String json_links = mapper.writeValueAsString(report.getLinks());

            BoundStatement bs = insertCrawlReportStatement.bind(
                    report.getUrl(),
                    report.getDate().toDate(),
                    report.getError(),
                    report.getStatusCode(),
                    json_links
            );

            session.execute(bs);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public CrawlReport getLatestStatus(String url) {
        BoundStatement bs = findLatestStatement.bind(url);
        ResultSet rs = session.execute(bs);

        Row r = rs.one();
        if (r == null) {
            return null;
        }

        return deserializeCrawlReport(r);
    }

    protected CrawlReport deserializeCrawlReport(Row r) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            List<Link> links = mapper.readValue(
                    r.getString("links"),
                    new TypeReference<List<Link>>() {
                    });

            return new CrawlReport(
                    r.getString("url"),
                    new DateTime(r.getDate("date")),
                    r.getString("error"),
                    r.getInt("status_code"),
                    links
            );
        } catch (IOException ex) {
            throw new RuntimeException("Error reading from database", ex);
        }
    }
}
