package com.eogren.link_checker.service_layer.messaging;

/**
 * Base definition of a MessageProcessor.
 * Expected to be overriden. To process a given type of message,
 * the MessageProcessor subclass just needs to define a process() method
 * with the appropriate type:
 *
 * process(TestMessage m)
 *
 * The deserialization code will use reflection to find and invoke this method.
 */
public class MessageProcessor {
    /**
     * No-op implementation that processes a BaseMessage.
     * @param m BaseMessage to process
     */
    public void processUnknownMessage(BaseMessage m) {}
}
