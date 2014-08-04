package com.eogren.link_checker.cassandra.tests;

import com.datastax.driver.core.Session;
import com.datastax.driver.core.PreparedStatement;

import com.eogren.link_checker.service_layer.api.CrawlReport;
import com.eogren.link_checker.service_layer.api.Link;
import com.eogren.link_checker.service_layer.data.CassandraCrawlReportRepository;
import org.joda.time.DateTime;
import org.junit.Test;


import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

public class CassandraCrawlReportRepositoryTest {
    @Test
    public void testAddSerializesLinksCorrectly() {
        Session mockSession = mock(Session.class);
        PreparedStatement mockedInsert = mock(PreparedStatement.class);

        when(mockedInsert.bind(any(), any(), any(), any())).thenReturn(null);
        when(mockSession.prepare(anyString())).thenReturn(mockedInsert);

        CassandraCrawlReportRepository repo = new CassandraCrawlReportRepository(mockSession);

        final String url = "http://www.eogren.com";
        final DateTime when = new DateTime(204, 01, 03, 05, 07);
        final int status_code = 200;

        final List<Link> links = new ArrayList<>();
        links.add(new Link("http://www.link1.com", "Link1"));
        links.add(new Link("http://www.link2.com", "Link2"));

        final String links_text = "[{\"url\":\"http://www.link1.com\",\"anchorText\":\"Link1\"},{\"url\":\"http://www.link2.com\",\"anchorText\":\"Link2\"}]";

        CrawlReport report = new CrawlReport(
            url,
            when,
            status_code,
            links
        );

        repo.addCrawlReport(report);

        verify(mockedInsert).bind(
                url,
                when,
                status_code,
                links_text
        );
    }
}
