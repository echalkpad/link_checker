package com.eogren.link_checker.service_layer.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ExchangeReader extends ExchangeMessageHandler {

    protected final String routingKey;
    protected final String requestedQueueName;
    protected final Logger logger;

    protected String actualQueueName;
    protected String consumeTag;

    /**
     * Create an ExchangeReader pointing at the given exchange. This version of
     * the constructor will automatically create a queue that RabbitMQ will auto-delete
     * when the client terminates.
     * @param amqpUri URI to connect to
     * @param exchangeName Exchange to bind the queue to
     */
    public ExchangeReader(String amqpUri, String exchangeName) throws ConnectionException {
        this(amqpUri, exchangeName, null);
    }

    /**
     * Create an ExchangeReader pointing at the given queue. The queue
     * created by this version of the constructor will be bound to the exchange
     * but NOT auto-deleted when the client goes away. The routing key for the queue will be "#".
     * @param amqpUri URI to connect to
     * @param exchangeName Exchange to bind the queue to
     * @param queueName Queue name to create
     */
    public ExchangeReader(String amqpUri, String exchangeName, String queueName) throws ConnectionException {
        this(amqpUri, exchangeName, queueName, "#");
    }

    /**
     * Create an ExchangeReader pointing at the given queue with the given routing key.
     * @param amqpUri URI to connect to
     * @param exchangeName Exchange to bind the queue to
     * @param queueName Queue name to create
     * @param routingKey Routing key for the queue binding
     * @throws ConnectionException If the connection cannot be established
     */
    public ExchangeReader(String amqpUri, String exchangeName, String queueName, String routingKey) throws ConnectionException {
        super(amqpUri, exchangeName);

        this.routingKey = routingKey;
        this.requestedQueueName = queueName;
        this.logger = Logger.getLogger(this.getClass());

        registerQueue();
    }

    /**
     * Start consuming messages. Call shutdown() to stop. Note that message handler
     * callbacks will occur on some thread managed by the RabbitMQ runtime so must ensure
     * data structures are locked appropriately.
     * @param processor MessageProcessor that will process the messages
     */
    public void start(MessageProcessor processor) throws ConnectionException {
        Channel chan = getChannel();

        try {
            logger.debug("Starting consume");
            chan.basicConsume(
                    actualQueueName,
                    false,
                    new MessageConsumer(chan, processor)
            );
        } catch (IOException e) {
            throw new ConnectionException("IO Error: " + e.getMessage(), e);
        }
    }

    @Override
    public void shutdown() {
        try {
            logger.debug("Shutting down");
            if (consumeTag != null) {
                getChannel().basicCancel(consumeTag);
            }
        } catch (ConnectionException|IOException e) {
            /* intentionally ignored */
        } finally {
            consumeTag = null;
        }

        super.shutdown();
    }

    /**
     * Creates (if it doesn't exist) the queue this Reader will use
     * @throws ConnectionException On network errors
     */
    protected void registerQueue() throws ConnectionException {
        Channel chan = getChannel();

        try {
            if (requestedQueueName == null) {
                AMQP.Queue.DeclareOk resp = chan.queueDeclare();
                actualQueueName = resp.getQueue();
            } else {
                actualQueueName = requestedQueueName;
                /* Queue is:
                    * durable [survives server restart]
                    * non-exclusive [not limited to this connection]
                    * non-auto-deleted [not deleted when client goes away]
                 */
                chan.queueDeclare(actualQueueName, true, false, false, null);
            }

            logger.debug(String.format("Binding queue %s to exchange %s with key %s", actualQueueName, exchangeName, routingKey));
            chan.queueBind(actualQueueName, exchangeName, routingKey);
        } catch (IOException e) {
            throw new ConnectionException("IO Error: " + e.getMessage(), e);
        }
    }

    public class MessageConsumer extends DefaultConsumer {
        private final MessageProcessor processor;

        public MessageConsumer(Channel chan, MessageProcessor processor) {
            super(chan);

            this.processor = processor;
        }

        @Override
        public void handleDelivery(String consumerTag,
                                   Envelope envelope,
                                   AMQP.BasicProperties properties,
                                   byte[] body) {
            long deliveryTag = envelope.getDeliveryTag();

            try {
                try {
                    Object deserializedMessage =
                            deserializeMessage(envelope, body);

                    dispatchToProcessor(deserializedMessage);

                    this.getChannel().basicAck(deliveryTag, false);
                } catch (InvalidMessageException e) {
                    logger.warn("Rejecting message due to InvalidMessageException", e);
                    this.getChannel().basicReject(deliveryTag, false);
                }
            } catch (IOException e) {
                /* LOG and ignore... not sure what else to do in this case */
            }
        }

        protected Object deserializeMessage(Envelope envelope, byte[] body) throws InvalidMessageException {
            try {
                Class<?> clz = Class.forName(envelope.getRoutingKey());

                logger.debug(String.format("Trying to deserialize into %s", clz.toString()));
                ObjectMapper om = new ObjectMapper();

                return om.readValue(body, clz);
            } catch (ClassNotFoundException|IOException e) {
                throw new InvalidMessageException("Error while deserializing: " + e.getMessage(), e);
            }
        }

        protected void dispatchToProcessor(Object message) throws InvalidMessageException {
            Class<?> msgClass = message.getClass();
            Class<?> processorClass = processor.getClass();

            Method processorMethod;

            try {
                processorMethod = processorClass.getMethod("process", msgClass);
            } catch (NoSuchMethodException e) {
                try {
                    processorMethod = processorClass.getMethod("processUnknownMessage", BaseMessage.class);
                } catch (NoSuchMethodException e2) {
                    throw new RuntimeException("Something weird happening - could not resolve BaseMessage::processUnknownMessage");
                }
                msgClass = BaseMessage.class;
            }


            try {
                processorMethod.invoke(processor, msgClass.cast(message));
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Something weird happening - don't have access to " + processorMethod.toString());
            } catch (InvocationTargetException e) {
                throw new InvalidMessageException("Error from target method " + e.getCause().getMessage(), e.getCause());
            }
        }
    }
}
