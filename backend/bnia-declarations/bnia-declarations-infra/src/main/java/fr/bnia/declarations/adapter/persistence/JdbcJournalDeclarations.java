package fr.bnia.declarations.adapter.persistence;

import static fr.bnia.declarations.adapter.persistence.Lignes.SCHEMA;

import fr.bnia.declarations.application.EntreeJournal;
import fr.bnia.declarations.application.JournalDeclarations;
import fr.bnia.declarations.domain.ActionJournalDeclarations;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.List;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/** Journal des déclarations en SQL direct, en ajout seul (RG-DEC-23). */
@Repository
class JdbcJournalDeclarations implements JournalDeclarations {

    private static final String COLONNES = "date, acteur_id, nom_acteur, operateur_id, dsm_id, periode, action, detail";

    private final JdbcClient jdbcClient;

    JdbcJournalDeclarations(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public void ajouter(EntreeJournal entree) {
        jdbcClient.sql("insert into " + SCHEMA + ".journal (" + COLONNES + ") values (:date, :acteur, :nomActeur,"
                        + " :operateur, :dsm, :periode, :action, :detail)")
                .param("date", Timestamp.from(entree.date()))
                .param("acteur", entree.acteur())
                .param("nomActeur", entree.nomActeur())
                .param("operateur", entree.operateur())
                .param("dsm", entree.dsm(), Types.BIGINT)
                .param("periode", entree.periode(), Types.VARCHAR)
                .param("action", entree.action().name())
                .param("detail", entree.detail(), Types.VARCHAR)
                .update();
    }

    @Override
    public List<EntreeJournal> deLaDsm(long dsm) {
        return lire("dsm_id = :id", dsm);
    }

    @Override
    public List<EntreeJournal> deLOperateur(long operateur) {
        return lire("operateur_id = :id", operateur);
    }

    @Override
    public void supprimerDeLaDsm(long dsm) {
        jdbcClient.sql("delete from " + SCHEMA + ".journal where dsm_id = :dsm").param("dsm", dsm).update();
    }

    /** @param condition constante, jamais une saisie utilisateur */
    private List<EntreeJournal> lire(String condition, long id) {
        return jdbcClient.sql("select " + COLONNES + " from " + SCHEMA + ".journal where " + condition
                        + " order by date desc, id desc")
                .param("id", id)
                .query((rs, n) -> new EntreeJournal(rs.getTimestamp("date").toInstant(), rs.getLong("acteur_id"),
                        rs.getString("nom_acteur"), rs.getLong("operateur_id"), (Long) rs.getObject("dsm_id"),
                        rs.getString("periode"), ActionJournalDeclarations.valueOf(rs.getString("action")), rs.getString("detail")))
                .list();
    }
}
