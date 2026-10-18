package fr.bnia.facturation.adapter.persistence;

import static fr.bnia.facturation.adapter.persistence.Lignes.SCHEMA;

import fr.bnia.facturation.application.NumerotationRepository;
import fr.bnia.facturation.domain.Numerotation;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/**
 * Compteur verrouillé (A-FAC-5) : la ligne de l'année est créée au besoin, puis lue {@code FOR UPDATE} ; elle
 * reste verrouillée jusqu'à la fin de la transaction d'émission, qui s'annule d'un bloc (RG-FAC-12).
 */
@Repository
class JdbcNumerotation implements NumerotationRepository {

    private final JdbcClient jdbcClient;

    JdbcNumerotation(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public Numerotation verrouiller(int annee) {
        jdbcClient.sql("insert into " + SCHEMA + ".numerotation (annee, dernier) values (:annee, 0) on conflict do nothing")
                .param("annee", annee)
                .update();
        long dernier = jdbcClient.sql("select dernier from " + SCHEMA + ".numerotation where annee = :annee for update")
                .param("annee", annee)
                .query(Long.class)
                .single();
        return new Numerotation(annee, dernier);
    }

    @Override
    public void enregistrer(Numerotation numerotation) {
        jdbcClient.sql("update " + SCHEMA + ".numerotation set dernier = :dernier where annee = :annee")
                .param("annee", numerotation.annee())
                .param("dernier", numerotation.dernier())
                .update();
    }
}
