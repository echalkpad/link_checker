package com.eogren.link_checker.service_layer.messaging;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import java.io.IOException;

public class ExchangeReader extends ExchangeMessageHandler {

    protected final String routingKey;
    protected final String requestedQueueName;

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
            chan.basicConsume(
                    actualQueueName,
                    false,
                    new DefaultConsumer(chan) {
                        @Override
                        public void handleDelivery(String consumerTag,
                                                   Envelope envelope,
                                                   AMQP.BasicProperties properties,
                                                   byte[] body) {
                            processor.processBaseMessage(new BaseMessage());
                        }
                    }
            );
        } catch (IOException e) {
            throw new ConnectionException("IO Error: " + e.getMessage(), e);
        }
    }

    @Override
    public void shutdown() {
        try {
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

            chan.queueBind(actualQueueName, exchangeName, routingKey);
        } catch (IOException e) {
            throw new ConnectionException("IO Error: " + e.getMessage(), e);
        }
    }
}
