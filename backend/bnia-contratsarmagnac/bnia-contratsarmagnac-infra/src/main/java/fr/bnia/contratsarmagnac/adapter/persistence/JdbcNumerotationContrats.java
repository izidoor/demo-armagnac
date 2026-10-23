package fr.bnia.contratsarmagnac.adapter.persistence;

import static fr.bnia.contratsarmagnac.adapter.persistence.ContratJdbc.SCHEMA;

import fr.bnia.contratsarmagnac.application.Numerotation;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/** RG-CA-06 : le rang suivant dans l'année, tiré en une instruction (sûr entre transactions concurrentes). */
@Repository
class JdbcNumerotationContrats implements Numerotation {

    private final JdbcClient jdbcClient;

    JdbcNumerotationContrats(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public int suivant(int annee) {
        return jdbcClient.sql("insert into " + SCHEMA + ".numerotation (annee, dernier) values (:annee, 1)"
                        + " on conflict (annee) do update set dernier = numerotation.dernier + 1 returning dernier")
                .param("annee", annee)
                .query(Integer.class)
                .single();
    }
}
