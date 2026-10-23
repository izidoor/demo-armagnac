package fr.bnia.contratsarmagnac.adapter.persistence;

import static fr.bnia.contratsarmagnac.adapter.persistence.ContratJdbc.SCHEMA;

import fr.bnia.contratsarmagnac.application.EntreeJournal;
import fr.bnia.contratsarmagnac.application.JournalContrats;
import fr.bnia.contratsarmagnac.domain.ActionContrat;
import fr.bnia.contratsarmagnac.domain.Fait;
import fr.bnia.contratsarmagnac.domain.Partie;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.List;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/** Journal du contrat (RG-CA-19), en ajout seul. */
@Repository
class JdbcJournalContrats implements JournalContrats {

    private final JdbcClient jdbcClient;

    JdbcJournalContrats(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public void ajouter(long contrat, Fait fait) {
        jdbcClient.sql("insert into " + SCHEMA + ".journal (contrat_id, date, partie, utilisateur_id, nom, action,"
                        + " detail) values (:contrat, :date, :partie, :utilisateur, :nom, :action, :detail)")
                .param("contrat", contrat)
                .param("date", Timestamp.from(fait.auteur().le()))
                .param("partie", fait.auteur().partie().name())
                .param("utilisateur", fait.auteur().utilisateur())
                .param("nom", fait.auteur().nom())
                .param("action", fait.action().name())
                .param("detail", fait.detail(), Types.INTEGER)
                .update();
    }

    @Override
    public List<EntreeJournal> duContrat(long contrat) {
        return jdbcClient.sql("select * from " + SCHEMA + ".journal where contrat_id = :contrat order by date, id")
                .param("contrat", contrat)
                .query((rs, n) -> {
                    String partie = rs.getString("partie");
                    return new EntreeJournal(rs.getTimestamp("date").toInstant(),
                            partie == null ? null : Partie.valueOf(partie), rs.getLong("utilisateur_id"),
                            rs.getString("nom"), ActionContrat.valueOf(rs.getString("action")),
                            (Integer) rs.getObject("detail"));
                })
                .list();
    }
}
