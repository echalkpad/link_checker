package com.eogren.link_checker.service_layer.messaging;

/**
 * ConnectionException is thrown when some kind of network error impedes communication
 * to the RabbitMQ server.
 */
public class ConnectionException extends Exception {
    /**
     * Create a new ConnectionException.
     * @param message Human readable message
     * @param inner Inner readable exception
     */
    public ConnectionException(String message, Exception inner) {
        super(message, inner);
    }
}
