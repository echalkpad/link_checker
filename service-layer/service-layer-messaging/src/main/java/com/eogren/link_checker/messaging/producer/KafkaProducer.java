package com.eogren.link_checker.messaging.producer;

import com.eogren.link_checker.messaging.serde.ProtobufSerializer;
import com.eogren.link_checker.service_layer.api.CrawlReport;
import com.eogren.link_checker.service_layer.config.KafkaConfig;
import kafka.producer.KeyedMessage;
import kafka.javaapi.producer.Producer;
import kafka.producer.ProducerConfig;

import java.util.Optional;
import java.util.Properties;

public class KafkaProducer implements MessageEmitter {
    private final KafkaConfig config;
    private final Producer<String, byte[]> producer;

    public KafkaProducer(KafkaConfig config) {
        this.config = config;
        this.producer = getProducer();
    }

    @Override
    public void emitMessage(String topic, String partition_key, byte[] raw_data) {
        producer.send(new KeyedMessage<>(getStringWithPrefix(topic), partition_key, raw_data));
    }

    @Override
    public void notifyCrawlReport(Optional<CrawlReport> oldCrawlReport, CrawlReport newCrawlReport) {
        byte[] buf = ProtobufSerializer.createScrapeUpdate(oldCrawlReport, newCrawlReport);
        emitMessage("scrapeReports", newCrawlReport.getUrl(), buf);
    }

    protected Producer<String, byte[]> getProducer() {
        return new Producer<>(getProducerConfig());
    }

    protected ProducerConfig getProducerConfig() {
        Properties props = new Properties();

        props.put("metadata.broker.list", config.getZkAddress());
        props.put("serializer.class", "kafka.serializer.DefaultEncoder");
        props.put("key.serializer.class", "kafka.serializer.StringEncoder");
        props.put("partitioner.class", "kafka.producer.DefaultPartitioner");
        props.put("request.required.acks", "1");

        return new ProducerConfig(props);
    }

    protected String getStringWithPrefix(String str) {
        String prefix = config.getPrefix();
        if (prefix != null) {
            return prefix + "-" + str;
        }

        return str;
    }
}