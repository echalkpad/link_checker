package com.eogren.link_checker.service_layer.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

public class ExchangePublisher extends ExchangeMessageHandler {

    protected ObjectMapper jsonMapper;

    /**
     * Create a new ExchangePublisher.
     * @param amqpUri URI of the RabbitMQ server we are talking to
     * @param exchangeName Name of the exchange to create
     */
    public ExchangePublisher(String amqpUri, String exchangeName) throws ConnectionException {
        super(amqpUri, exchangeName);

        this.jsonMapper = new ObjectMapper();

        registerExchange();
    }

    /**
     * Publish a message to the given exchange.
     * @param msg Message to publish
     * @throws ConnectionException If there is an IO error while publishing the message
     */
    public void publishMessage(BaseMessage msg) throws ConnectionException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        Writer w = new BufferedWriter(new OutputStreamWriter(bytes, Charset.forName("UTF-8")));

        try {
            jsonMapper.writeValue(w, msg);
            getChannel().basicPublish(exchangeName, msg.getType(), getMessageHeaders(), bytes.toByteArray());
        } catch (IOException e) {
            throw new ConnectionException("IOError publishing message: " + e.getMessage(), e);
        }
    }

    /**
     * Get the exchange type this publisher. Intended to be overridden if required.
     * @return AMQP exchange type
     */
    protected String getExchangeType() {
        return "topic";
    }

    protected AMQP.BasicProperties getMessageHeaders() {
        AMQP.BasicProperties.Builder builder = new AMQP.BasicProperties.Builder();

        return builder.
                contentType("application/json").
                build();
    }

    /**
     * Registers the exchange this publisher posts to.
     * @throws ConnectionException If there is an IO error creating the exchange
     */
    protected void registerExchange() throws ConnectionException {
        try {
            Channel chan = getConn().createChannel();
            chan.exchangeDeclare(exchangeName, getExchangeType(), true);
            chan.close();
        } catch (IOException e) {
            throw new ConnectionException("IO exception", e);
        }
    }

}
