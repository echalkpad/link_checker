package com.eogren.link_checker.service_layer.config;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ProtocolVersion;
import com.datastax.driver.core.Session;
import com.eogren.link_checker.service_layer.exceptions.DatabaseException;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class CassandraClusterFactory extends CassandraClusterConfig {
    @JsonIgnore
    protected Session session;

    public Session getSession() throws DatabaseException {
        if (session != null) {
            return session;
        }

        Cluster.Builder builder = Cluster.builder().withProtocolVersion(ProtocolVersion.V3);
        getNodes().stream().forEach(n -> builder.addContactPoint(n));

        Cluster cluster = builder.build();
        try {
            session = cluster.connect(keyspace);
        } catch (Exception e) {
            throw new DatabaseException(e);
        }

        return session;
    }
}
