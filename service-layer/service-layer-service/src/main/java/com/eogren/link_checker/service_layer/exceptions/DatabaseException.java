package com.eogren.link_checker.service_layer.exceptions;

/** Thrown when there is some sort of database exception. [Meant to abstract out Cassandra vs MySQL etc] */
public class DatabaseException extends Exception {
    public DatabaseException(Throwable e) {
        super(e);
    }
}
