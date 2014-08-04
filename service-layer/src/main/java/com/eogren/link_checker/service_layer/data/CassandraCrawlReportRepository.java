package com.eogren.link_checker.service_layer.data;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Session;
import com.eogren.link_checker.service_layer.api.CrawlReport;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CassandraCrawlReportRepository implements CrawlReportRepository {
    private final Session session;
    private final PreparedStatement insertCrawlReportStatement;

    public CassandraCrawlReportRepository(Session session) {
        this.session = session;
        insertCrawlReportStatement = session.prepare(
                "INSERT INTO crawl_report VALUES (url, date, status_code, links) VALUES ?, ?, ?, ?"
        );
    }

    @Override
    public void addCrawlReport(CrawlReport report) {
        ObjectMapper mapper = new ObjectMapper();

        try {
            String json_links = mapper.writeValueAsString(report.getLinks());

            BoundStatement bs = insertCrawlReportStatement.bind(
                    report.getUrl(),
                    report.getDate(),
                    report.getStatusCode(),
                    json_links
            );

            session.execute(bs);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
