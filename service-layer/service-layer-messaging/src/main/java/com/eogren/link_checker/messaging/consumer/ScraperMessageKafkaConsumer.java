package com.eogren.link_checker.messaging.consumer;

import com.eogren.link_checker.messaging.common.Utils;
import com.eogren.link_checker.messaging.serde.ProtobufSerializer;
import com.eogren.link_checker.protobuf.ScraperMessages;
import com.eogren.link_checker.service_layer.config.KafkaConfig;
import kafka.consumer.ConsumerConfig;
import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;
import kafka.message.MessageAndMetadata;
import kafka.serializer.DefaultDecoder;
import kafka.serializer.StringDecoder;
import kafka.utils.VerifiableProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ScraperMessageKafkaConsumer {
    private final ScraperMessageProcessor scraperMessageProcessor;
    private final KafkaConfig config;
    private final String groupName;
    private ConsumerConnector kafkaConsumer;
    private ExecutorService executor;
    private final Logger logger = LoggerFactory.getLogger(ScraperMessageKafkaConsumer.class);

    /**
     * The ConsumerThread is the thread function that actually sits and tries to retrieve messages from
     * Kafka.
     */
    public class ConsumerThread implements Callable<Void> {
        private final int threadNumber;
        private final KafkaStream stream;
        private final ScraperMessageProcessor processor;
        private final Logger logger = LoggerFactory.getLogger(ConsumerThread.class);

        public ConsumerThread(ScraperMessageProcessor processor, KafkaStream stream, int threadNumber) {
            this.processor = processor;
            this.stream = stream;
            this.threadNumber = threadNumber;
        }

        public Void call() {
            logger.debug("Starting thread " + threadNumber);
            ConsumerIterator<String, byte[]> it = stream.iterator();

            try {

                logger.debug(threadNumber + ": Waiting for messages...");
                while (it.hasNext()) {
                    logger.debug(threadNumber + ": Retrieved message");

                    MessageAndMetadata<String, byte[]> kafka_msg = it.next();
                    byte[] raw = kafka_msg.message();
                    try {
                        ScraperMessages.ScraperMessage msg = ProtobufSerializer.deserializeScraperMessage(raw);
                        processor.consumeScraperMessage(msg);
                    } catch (IllegalArgumentException e) {
                        logger.warn(threadNumber + ": Failed to decode kafka msg at " + kafka_msg.offset());
                    }

                }
            } catch (Throwable t) {
                logger.error(threadNumber + ": Exception!", t);
                throw t;
            }

            return null;
        }
    }

    public ScraperMessageKafkaConsumer(KafkaConfig config,
                                       String groupName,
                                       ScraperMessageProcessor scraperMessageProcessor) {
        this.config = config;
        this.scraperMessageProcessor = scraperMessageProcessor;
        this.groupName = groupName;
    }

    public void start(int numThreads) {
        logger.debug("Start called");
        kafkaConsumer = kafka.consumer.Consumer.createJavaConsumerConnector(createConsumerConfig());
        final String topic = Utils.getStringWithPrefix(config.getPrefix(), Utils.SCRAPER_TOPIC);

        Map<String, Integer> topicCountMap = new HashMap<>();
        topicCountMap.put(topic, numThreads);
        logger.debug(String.format("Adding topic %s with %d threads", topic, numThreads));

        Map<String, List<KafkaStream<String, byte[]>>> consumerMap = kafkaConsumer.createMessageStreams(
                topicCountMap, new StringDecoder(new VerifiableProperties()), new DefaultDecoder(new VerifiableProperties()));

        List<KafkaStream<String, byte[]>> streams = consumerMap.get(topic);


        // now launch all the threads
        //
        executor = Executors.newFixedThreadPool(numThreads);

        // now create an object to consume the messages
        //
        int threadNumber = 0;
        for (final KafkaStream stream : streams) {
            executor.submit(new ConsumerThread(scraperMessageProcessor, stream, threadNumber));
            threadNumber++;
        }
    }

    public void stop() {
        executor.shutdown();
    }

    protected ConsumerConfig createConsumerConfig() {
        Properties props = new Properties();
        props.put("zookeeper.connect", config.getZkAddress());
        props.put("group.id", groupName);
        props.put("zookeeper.session.timeout.ms", "400");
        props.put("zookeeper.sync.time.ms", "200");
        props.put("auto.commit.interval.ms", "1000");
        props.put("auto.offset.reset", "smallest");

        return new ConsumerConfig(props);
    }
}
