package com.eogren.link_checker.service_layer.data;

import com.codahale.metrics.annotation.Timed;
import com.datastax.driver.core.*;
import com.datastax.driver.core.utils.UUIDs;
import com.eogren.link_checker.service_layer.api.CrawlReport;
import com.eogren.link_checker.service_layer.api.Link;
import org.joda.time.DateTime;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

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
        UUID uuid = UUIDs.timeBased();

        Map<String, UDTValue> links = serializeLinkToUdt(report.getLinks());

        BoundStatement bs = getInsertCrawlReportStatement().bind(
                report.getUrl(),
                uuid, // XXX this is based on time of POST not on crawlTime, but probably ok
                report.getError(),
                report.getStatusCode(),
                links
        );

        session.execute(bs);

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
        List<Link> links = extractLinksFromRow(r);

        return new CrawlReport(
                r.getString("url"),
                new DateTime(UUIDs.unixTimestamp(r.getUUID("date"))),
                r.getString("error"),
                r.getInt("status_code"),
                links
        );
    }

    /**
     * Convert a Cassandra row [map and UDT] to the Link POJO
     * @param r row to convert
     * @return
     */
    private List<Link> extractLinksFromRow(Row r) {
        return r.getMap("links", String.class, UDTValue.class).values()
                .stream()
                .map(v -> new Link(v.getString("url"), v.getString("anchorText")))
                .collect(Collectors.toList());
    }

    /**
     * Given a list of Link POJOs, translate them to a map of UDT's that
     * can be inserted into Cassandra
     * @param links links to convert
     * @return
     */
    private Map<String, UDTValue> serializeLinkToUdt(List<Link> links) {
        UserType linkType =
                session
                        .getCluster()
                        .getMetadata()
                        .getKeyspace(session.getLoggedKeyspace())
                        .getUserType("found_link");

        HashMap<String, UDTValue> ret = new HashMap<>();
        for (Link link : links) {
            UDTValue v = linkType.newValue()
                    .setString("url", link.getUrl())
                    .setString("anchorText", link.getAnchorText());

            ret.put(link.getUrl(), v);
        }

        return ret;
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
