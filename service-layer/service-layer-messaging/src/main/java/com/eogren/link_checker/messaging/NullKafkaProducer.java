package com.eogren.link_checker.messaging;

public class NullKafkaProducer implements MessageEmitter {
    @Override
    public void emitMessage(String topic, String partition_key, byte[] raw_data) {
        // intentionally empty
    }
}
