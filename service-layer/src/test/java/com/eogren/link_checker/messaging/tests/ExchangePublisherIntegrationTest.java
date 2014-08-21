package com.eogren.link_checker.messaging.tests;

import com.eogren.link_checker.service_layer.messaging.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.log4j.Logger;
import org.junit.Test;

import static org.junit.Assert.*;

public class ExchangePublisherIntegrationTest {
    final private String amqpUri = "amqp://localhost";

    public static class TestMessage extends BaseMessage {
        protected String testString;

        @JsonProperty
        public String getTestString() {
            return testString;
        }

        public TestMessage(String s) {
            testString = s;
        }

        public TestMessage() {

        }
    }

    public static class UnregisteredTestMessage extends BaseMessage {

    }

    public class TestMessageProcessor extends MessageProcessor {
        public boolean seenBaseMessage = false;
        private boolean seenTestMessage;
        private String testMessageString;
        private String unknownMessageType;

        @Override
        public void processUnknownMessage(BaseMessage m) {
            seenBaseMessage = true;
            unknownMessageType = m.getType();
        }

        public void process(TestMessage m) {
            seenTestMessage = true;
            testMessageString = m.getTestString();

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
    public void testUnknownMessageCallsProcessUnhandled() throws ConnectionException {
        final String myExchange = "test-exchange";

        ExchangePublisher pub = new ExchangePublisher(amqpUri, myExchange);
        ExchangeReader reader = new ExchangeReader(amqpUri, myExchange);

        try {
            TestMessageProcessor tmp = new TestMessageProcessor();
            reader.start(tmp);

            pub.publishMessage(new UnregisteredTestMessage());

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                /* intentionally ignored */
            }

            assertTrue("Expected to receive UnregisteredTestMessage in 100ms", tmp.seenBaseMessage);
            assertEquals(tmp.unknownMessageType, "com.eogren.link_checker.messaging.tests.ExchangePublisherIntegrationTest$UnregisteredTestMessage");

        } finally {
            pub.shutdown();
            reader.shutdown();
        }
    }

    @Test
    public void testCanReceiveDerivedMessage() throws ConnectionException {
        final String myExchange = "test-exchange";
        final String myTestMessage = "This is a test of the emergency broadcast system.";

        ExchangePublisher pub = new ExchangePublisher(amqpUri, myExchange);
        ExchangeReader reader = new ExchangeReader(amqpUri, myExchange);

        try {
            TestMessageProcessor tmp = new TestMessageProcessor();
            reader.start(tmp);

            pub.publishMessage(new TestMessage(myTestMessage));

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                /* intentionally ignored */
            }

            assertTrue("Expected to receive TestMessage in 100ms", tmp.seenTestMessage);
            assertEquals("Expected to decode TestMessage properly", tmp.testMessageString, myTestMessage);

        } finally {
            pub.shutdown();
            reader.shutdown();
        }
    }
}
