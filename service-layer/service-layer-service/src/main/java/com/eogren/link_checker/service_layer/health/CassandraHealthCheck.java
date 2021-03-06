package com.eogren.link_checker.service_layer.health;

import com.codahale.metrics.health.HealthCheck;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;
import com.eogren.link_checker.service_layer.config.CassandraClusterFactory;

public class CassandraHealthCheck extends HealthCheck {
    private final CassandraClusterFactory config;

    public CassandraHealthCheck(CassandraClusterFactory config) {
        this.config = config;
    }

    @Override
    protected Result check() throws Exception {
        Session session = config.getSession();
        ResultSet rs = session.execute("SELECT * from health_check LIMIT 1");
        if (rs.one() == null) {
            return Result.unhealthy("Unable to select even one row from health_check");
        }

        return Result.healthy("Able to connect to cassandra");
    }
}
