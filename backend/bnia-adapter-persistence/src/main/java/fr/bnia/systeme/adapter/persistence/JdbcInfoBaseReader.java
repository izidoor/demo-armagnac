package fr.bnia.systeme.adapter.persistence;

import fr.bnia.systeme.application.InfoBaseReader;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/**
 * Port de lecture en SQL direct (ADR-004).
 */
@Repository
class JdbcInfoBaseReader implements InfoBaseReader {

    private final JdbcClient jdbcClient;

    JdbcInfoBaseReader(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public String versionBase() {
        return jdbcClient.sql("select version()").query(String.class).single();
    }
}
