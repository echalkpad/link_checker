package com.eogren.link_checker.messaging;

public interface MessageEmitter {
    public void emitMessage(String topic, String partition_key, byte[] raw_data);
}
