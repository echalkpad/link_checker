package com.eogren.link_checker.service_layer.data;

import com.codahale.metrics.annotation.Timed;
import com.datastax.driver.core.*;
import com.datastax.driver.core.utils.UUIDs;
import com.eogren.link_checker.service_layer.api.CrawlReport;
import com.eogren.link_checker.service_layer.api.Link;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.joda.time.DateTime;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class CassandraCrawlReportRepository implements CrawlReportRepository {
    private final Session session;

    private PreparedStatement insertCrawlReportStatement;
    private PreparedStatement findLatestStatement;
    private PreparedStatement findByUuidStatement;

    public CassandraCrawlReportRepository(Session session) {
        this.session = session;
    }

    @Override
    @Timed
    public String addCrawlReport(CrawlReport report) {
        ObjectMapper mapper = new ObjectMapper();

        UUID uuid = UUIDs.timeBased();

        try {
            String json_links = mapper.writeValueAsString(report.getLinks());

            BoundStatement bs = getInsertCrawlReportStatement().bind(
                    report.getUrl(),
                    uuid, // XXX this is based on time of POST not on crawlTime, but probably ok
                    report.getError(),
                    report.getStatusCode(),
                    json_links
            );

            session.execute(bs);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return uuid.toString();
    }

    @Override
    @Timed
    public Optional<CrawlReport> getLatestStatus(String url) {
        BoundStatement bs = getFindLatestStatement().bind(url);
        return getOneCrawlReport(bs);
    }


    @Override
    @Timed
    public Optional<CrawlReport> getByUuid(String url, String uuidString) {
        UUID uuid = UUID.fromString(uuidString);

        BoundStatement bs = getFindByUuidStatement().bind(url, uuid);
        return getOneCrawlReport(bs);
    }

    private Optional<CrawlReport> getOneCrawlReport(BoundStatement bs) {
        ResultSet rs = session.execute(bs);

        Row r = rs.one();
        if (r == null) {
            return Optional.empty();
        }

        return Optional.of(deserializeCrawlReport(r));
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
                    new DateTime(UUIDs.unixTimestamp(r.getUUID("date"))),
                    r.getString("error"),
                    r.getInt("status_code"),
                    links
            );
        } catch (IOException ex) {
            throw new RuntimeException("Error reading from database", ex);
        }
    }

    protected PreparedStatement getInsertCrawlReportStatement() {
        if (insertCrawlReportStatement == null) {
            insertCrawlReportStatement = session.prepare(
                    "INSERT INTO crawl_report (url, date, error, status_code, links) VALUES (?, ?, ?, ?, ?);"
            );
        }

        return insertCrawlReportStatement;
    }

    protected PreparedStatement getFindLatestStatement() {
        if (findLatestStatement == null) {
            findLatestStatement = session.prepare(
                    "SELECT url, date, error, status_code, links FROM crawl_report WHERE url = ? ORDER BY date DESC LIMIT 1"
            );
        }

        return findLatestStatement;
    }

    protected PreparedStatement getFindByUuidStatement() {
        if (findByUuidStatement == null) {
            findByUuidStatement = session.prepare(
                    "SELECT url, date, error, status_code, links FROM crawl_report WHERE url = ? AND date = ?"
            );
        }

        return findByUuidStatement;
    }

}
