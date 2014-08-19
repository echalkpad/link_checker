package com.eogren.link_checker.service_layer.messaging;

public class InvalidMessageException extends Exception {
    public InvalidMessageException(String msg, Throwable e) {
        super(msg, e);
    }
}
