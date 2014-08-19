package com.eogren.link_checker.service_layer.messaging;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

public class ExchangeMessageHandler {
    protected String amqpUri;
    protected String exchangeName;
    protected Connection conn;
    protected Channel channel;

    public ExchangeMessageHandler(String amqpUri, String exchangeName) {
        this.exchangeName = exchangeName;
        this.amqpUri = amqpUri;
    }

    /**
     * Shutdown any resources used by the publisher
     */
    public void shutdown() {
        try {
            if (channel != null) {
                channel.close();
            }

            if (conn != null) {
                conn.close();
            }
        } catch (IOException e) {
            /* intentionally ignored */
        } finally {
            channel = null;
            conn = null;
        }
    }

    /**
     * Retrieve a Connection to the RabbitMQ server specified in the ctor
     * @return Connection object
     */
    protected Connection getConn() throws ConnectionException {
        if (conn != null) {
            return conn;
        }

        try {
            ConnectionFactory cf = new ConnectionFactory();
            cf.setUri(amqpUri);
            cf.setAutomaticRecoveryEnabled(true);
            cf.setConnectionTimeout(3000);

            conn = cf.newConnection();
            return conn;
        } catch (URISyntaxException |NoSuchAlgorithmException |KeyManagementException e) {
            throw new ConnectionException("Malformed url " + amqpUri + ": " + e.getMessage(), e);
        } catch (IOException e) {
            throw new ConnectionException("IO error: " + e.getMessage(), e);
        }
    }

    protected Channel getChannel() throws ConnectionException {
        if (channel != null) {
            return channel;
        }

        try {
            channel = getConn().createChannel();
        } catch (IOException e) {
            throw new ConnectionException("IOException on create channel: " + e.getMessage(), e);
        }

        return channel;
    }
}
