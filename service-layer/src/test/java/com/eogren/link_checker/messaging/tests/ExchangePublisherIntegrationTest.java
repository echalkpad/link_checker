package com.eogren.link_checker.messaging.tests;

import com.eogren.link_checker.service_layer.messaging.*;
import org.junit.Test;

import static org.junit.Assert.*;

public class ExchangePublisherIntegrationTest {
    final private String amqpUri = "amqp://localhost";

    private class TestMessageProcessor extends MessageProcessor {
        public boolean seenMessage = false;

        @Override
        public void processBaseMessage(BaseMessage m) {
            seenMessage = true;
        }
    }

    @Test
    public void testCanStartPublisherTwiceInARow() throws ConnectionException {
        final String myExchange = "test-exchange";

        ExchangePublisher p1 = new ExchangePublisher(amqpUri, myExchange);
        p1.shutdown();

        ExchangePublisher p2 = new ExchangePublisher(amqpUri, myExchange);
        p2.shutdown();
    }

    @Test
    public void testCanPublishMessage() throws ConnectionException {
        final String myExchange = "test-exchange";

        ExchangePublisher pub = new ExchangePublisher(amqpUri, myExchange);
        pub.publishMessage(new BaseMessage());
    }

    @Test
    public void testCanReceiveMessage() throws ConnectionException {
        final String myExchange = "test-exchange";



        ExchangePublisher pub = new ExchangePublisher(amqpUri, myExchange);
        ExchangeReader reader = new ExchangeReader(amqpUri, myExchange);

        try {
            TestMessageProcessor tmp = new TestMessageProcessor();
            reader.start(tmp);

            pub.publishMessage(new BaseMessage());

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                /* intentionally ignored */
            }

            assertTrue("Expected to receive BaseMessage in 100ms", tmp.seenMessage);

        } finally {
            pub.shutdown();
            reader.shutdown();
        }
    }
}
