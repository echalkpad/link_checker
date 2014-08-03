package com.eogren.link_checker.service_layer.config;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.eogren.link_checker.service_layer.exceptions.DatabaseException;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;

import java.util.List;

public class CassandraClusterFactory {
    @NotEmpty
    protected List<String> nodes;

    @NotEmpty
    protected String keyspace;

    @JsonIgnore
    protected Session session;

    @JsonProperty
    public List<String> getNodes() {
        return nodes;
    }

    @JsonProperty
    public String getKeyspace() {
        return keyspace;
    }

    @JsonProperty
    public void setNodes(List<String> nodes) {
        this.nodes = nodes;
    }

    public Session getSession() throws DatabaseException {
        if (session != null) {
            return session;
        }

        Cluster.Builder builder = Cluster.builder();
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
