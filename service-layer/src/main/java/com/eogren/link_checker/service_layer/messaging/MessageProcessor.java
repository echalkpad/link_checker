package com.eogren.link_checker.service_layer.messaging;

/**
 * Base definition of a MessageProcessor.
 * Expected to be overriden.
 */
public class MessageProcessor {
    /**
     * No-op implementation that processes a BaseMessage.
     * @param m BaseMessage to process
     */
    public void processBaseMessage(BaseMessage m) {}
}
