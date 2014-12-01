package com.eogren.link_checker.messaging.producer;

import com.eogren.link_checker.messaging.common.Utils;
import com.eogren.link_checker.messaging.serde.ProtobufSerializer;
import com.eogren.link_checker.service_layer.api.CrawlReport;
import com.eogren.link_checker.service_layer.config.KafkaConfig;
import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.Properties;

public class KafkaProducer implements MessageEmitter {
    private final KafkaConfig config;
    private final Producer<String, byte[]> producer;

    private static Logger logger = LoggerFactory.getLogger(KafkaProducer.class);

    public KafkaProducer(KafkaConfig config) {
        this.config = config;
        this.producer = getProducer();
    }

    @Override
    public void emitMessage(String unprefixed_topic, String partition_key, byte[] raw_data) {
        String topic = Utils.getStringWithPrefix(config.getPrefix(), unprefixed_topic);
        logger.debug("Sending message to topic " + topic + ", partition " + partition_key);
        producer.send(new KeyedMessage<>(topic, partition_key, raw_data));
    }

    @Override
    public void notifyCrawlReport(Optional<CrawlReport> oldCrawlReport, CrawlReport newCrawlReport) {
        byte[] buf = ProtobufSerializer.createScrapeUpdate(oldCrawlReport, newCrawlReport);
        emitMessage(Utils.SCRAPER_TOPIC, newCrawlReport.getUrl(), buf);
    }

    @Override
    public void emitScrapeRequest(String url) {
        byte[] buf = ProtobufSerializer.createScrapeRequest(url);
        emitMessage(Utils.SCRAPER_TOPIC, url, buf);
    }

    protected Producer<String, byte[]> getProducer() {
        return new Producer<>(getProducerConfig());
    }

    protected ProducerConfig getProducerConfig() {
        Properties props = new Properties();

        props.put("metadata.broker.list", config.getBrokerList());
        props.put("serializer.class", "kafka.serializer.DefaultEncoder");
        props.put("key.serializer.class", "kafka.serializer.StringEncoder");
        props.put("partitioner.class", "kafka.producer.DefaultPartitioner");
        props.put("request.required.acks", "1");

        return new ProducerConfig(props);
    }
}
