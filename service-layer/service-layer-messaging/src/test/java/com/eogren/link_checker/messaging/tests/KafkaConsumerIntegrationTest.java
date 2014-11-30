package com.eogren.link_checker.messaging.tests;

import com.eogren.link_checker.messaging.common.Utils;
import com.eogren.link_checker.messaging.consumer.ScraperMessageKafkaConsumer;
import com.eogren.link_checker.messaging.consumer.ScraperMessageProcessor;
import com.eogren.link_checker.messaging.producer.KafkaProducer;
import com.eogren.link_checker.protobuf.ScraperMessages;
import com.eogren.link_checker.service_layer.api.CrawlReport;
import com.eogren.link_checker.service_layer.config.KafkaConfig;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class KafkaConsumerIntegrationTest {
    protected static String zkAddress;
    protected static String kafkaShell;
    protected static String brokerList;

    protected String topicName;

    public class TestProcessor extends ScraperMessageProcessor {
        private int numProcessed;
        private List<ScraperMessages.ScrapeUpdate> messages;


        public TestProcessor() {
            numProcessed = 0;
            messages = new ArrayList<>();
        }

        public int getNumProcessed() {
            return numProcessed;
        }

        public boolean updateProcessedForUrl(String url) {
            for (ScraperMessages.ScrapeUpdate msg : messages) {
                if (msg.getNewStatus().getUrl().equals(url)) {
                    return true;
                }
            }

            return false;
        }

        @Override
        public void consumeScrapeUpdate(ScraperMessages.ScrapeUpdate msg) {
            super.consumeScrapeUpdate(msg);
            numProcessed++;
            messages.add(msg);
        }
    }

    @BeforeClass
    public static void loadEnvironmentAndCheckPrereqs() {
        zkAddress = getEnvWithDefault("ZOOKEEPER_ADDRESS", "localhost:2181");
        brokerList = getEnvWithDefault("BROKER_ADDRESS", "localhost:9092");
        kafkaShell = getEnvWithDefault("KAFKA_SHELL", "/opt/kafka/bin");

        checkFileExists("kafka-topics.sh");
    }

    @Test
    public void testCanReceiveMessages() throws InterruptedException {
        // Emit a set of messages and make sure they all come the other side.
        // Emit first half before consumer starts, second half after

        final int NUM_MESSAGES = 1500;
        final long SLEEP_DURATION = 100;
        final long MAX_DURATION = 5000;

        KafkaProducer producer = new KafkaProducer(getConfig());
        TestProcessor testProcessor = new TestProcessor();
        ScraperMessageKafkaConsumer consumer = new ScraperMessageKafkaConsumer(getConfig(), "int-test", testProcessor);


        List<String> urls = getUrls(NUM_MESSAGES);
        List<String> first_half = urls.subList(0, urls.size() / 2);
        List<String> second_half = urls.subList(urls.size() / 2, NUM_MESSAGES);

        first_half.parallelStream().forEach(x -> producer.notifyCrawlReport(Optional.empty(), createCrawlReport(x)));

        consumer.start(2);

        second_half.parallelStream().forEach(x -> producer.notifyCrawlReport(Optional.empty(), createCrawlReport(x)));

        long duration = 0;

        while (testProcessor.getNumProcessed() < urls.size() && duration < MAX_DURATION) {
            Thread.sleep(SLEEP_DURATION);
            duration += SLEEP_DURATION;
        }

        consumer.stop();
        if (duration >= MAX_DURATION) {
            fail("Waited too long to receive all " + urls.size() + " messages. Received" + testProcessor.getNumProcessed() + " out of " + urls.size());
        }

        assertEquals("Expected to receive exactly the right # of messages", urls.size(), testProcessor.getNumProcessed());
        for (String url: urls) {
            assertTrue("Expected to get message for " + url, testProcessor.updateProcessedForUrl(url));
        }
    }


    @Before
    public void createTopic() {
        topicName = String.format("KafkaConsumerIntegrationTest-%s", UUID.randomUUID().toString());

        String cmdLine = String.format("%s/kafka-topics.sh --create --zookeeper %s --replication-factor 1 --partitions 2 --topic %s-%s",
                kafkaShell,
                zkAddress,
                topicName,
                Utils.SCRAPER_TOPIC);


        runWithTimeout(cmdLine, 3000, TimeUnit.MILLISECONDS);
    }

    @After
    public void deleteTopic() {
        // XXX not supported in 0.8.1
    }

    protected KafkaConfig getConfig() {
        return new KafkaConfig(brokerList, zkAddress, topicName, null);
    }

    protected CrawlReport createCrawlReport(String url) {
        return new CrawlReport(url, DateTime.now(), "", 200, new ArrayList<>());
    }



    protected List<String> getUrls(int NUM_MESSAGES) {
        List<String> urls = new ArrayList<>();

        for (int i =0 ; i < NUM_MESSAGES; i++) {
            urls.add(String.format("http://www.page%d.com", i));
        }

        return urls;
    }
}
