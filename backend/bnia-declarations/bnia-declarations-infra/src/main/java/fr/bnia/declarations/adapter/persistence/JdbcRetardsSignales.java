package fr.bnia.declarations.adapter.persistence;

import static fr.bnia.declarations.adapter.persistence.Lignes.SCHEMA;

import fr.bnia.declarations.application.RetardsSignales;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/** Retards déjà signalés (A-DEC-7, ADR-019) : la clé primaire garantit l'unicité, même sur deux instances. */
@Repository
class JdbcRetardsSignales implements RetardsSignales {

    private final JdbcClient jdbcClient;

    JdbcRetardsSignales(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public boolean signaler(long operateur, LocalDate debut, Instant le) {
        return jdbcClient.sql("insert into " + SCHEMA + ".retard_signale (operateur_id, debut, signale_le)"
                        + " values (:operateur, :debut, :le) on conflict do nothing")
                .param("operateur", operateur)
                .param("debut", debut)
                .param("le", Timestamp.from(le))
                .update() == 1;
    }
}
