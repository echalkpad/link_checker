package com.eogren.link_checker.scheduler;

import com.eogren.link_checker.scheduler.commands.Command;
import com.eogren.link_checker.scheduler.commands.ScheduleScrapeIfNecessaryCommand;
import com.eogren.link_checker.service_layer.api.MonitoredPage;
import com.eogren.link_checker.service_layer.client.ApiClient;
import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class CommandExecutorTest {
    protected BlockingQueue<Command> outputQueue;

    @Before
    public void initializeQueue() {
        outputQueue = new ArrayBlockingQueue<>(100);
    }

    @Test
    public void testBasic() throws IOException {
        int scheduled = 0;

        ApiClient mockApiClient = mock(ApiClient.class);
        List<MonitoredPage> mps = ImmutableList.of(
            createMonitoredPage("http://www.page1.com"),
            createMonitoredPage("http://www.page2.com")
        );
        when(mockApiClient.retrieveAllMonitoredPages()).thenReturn(mps);

        CommandExecutor sut = new CommandExecutor(mockApiClient, 10000, outputQueue);
        sut.start();
        try {
            Thread.sleep(1200);

            // This request should be ignored since page1.com will have been crawled
            sut.getInputQueue().put(new ScheduleScrapeIfNecessaryCommand("http://www.page1.com"));
            Thread.sleep(500);
        } catch (InterruptedException ie) {
        } finally {
            sut.stop();
        }

        assertEquals(2, outputQueue.size());
    }

    protected MonitoredPage createMonitoredPage(String url) {
        return new MonitoredPage(url);
    }
}
