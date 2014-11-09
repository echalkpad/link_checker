package com.eogren.link_checker.messaging;

import com.eogren.link_checker.service_layer.config.KafkaConfig;

/**
 * EmitterFactory is responsible for creating a message emitter based on the 'type'
 * specified in the kafka config section.
 */
public class EmitterFactory {
    /**
     * Create and return a new MessageEmitter.
     * @param config Configuration params
     * @return New emitter
     */
    public static MessageEmitter create(KafkaConfig config) {
        if (config.getType().equals("nullEmitter")) {
            return new NullKafkaProducer();
        }

        return new KafkaProducer(config);
    }
}
