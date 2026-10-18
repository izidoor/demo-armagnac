package fr.bnia.facturation.adapter.persistence;

import static fr.bnia.facturation.adapter.persistence.Lignes.SCHEMA;

import fr.bnia.facturation.application.JournalFacturation;
import fr.bnia.facturation.domain.CoteJournal;
import fr.bnia.facturation.domain.EntreeJournal;
import fr.bnia.facturation.domain.EvenementJournal;
import java.sql.Timestamp;
import java.util.List;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/** Journal des pièces, en ajout seul (RG-FAC-20). */
@Repository
class JdbcJournalFacturation implements JournalFacturation {

    private final JdbcClient jdbcClient;

    JdbcJournalFacturation(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public void ajouter(EntreeJournal entree) {
        jdbcClient.sql("insert into " + SCHEMA + ".journal (facture_id, date, evenement, utilisateur_id, nom_acteur, cote,"
                        + " detail) values (:piece, :date, :evenement, :utilisateur, :nom, :cote, :detail)")
                .param("piece", entree.piece())
                .param("date", Timestamp.from(entree.date()))
                .param("evenement", entree.evenement().name())
                .param("utilisateur", entree.utilisateur(), java.sql.Types.BIGINT)
                .param("nom", entree.nomActeur())
                .param("cote", entree.cote().name())
                .param("detail", entree.detail())
                .update();
    }

    @Override
    public List<EntreeJournal> deLaPiece(long piece) {
        return jdbcClient.sql("select facture_id, date, evenement, utilisateur_id, nom_acteur, cote, detail from " + SCHEMA
                        + ".journal where facture_id = :piece order by date desc, id desc")
                .param("piece", piece)
                .query((rs, n) -> new EntreeJournal(rs.getLong("facture_id"), rs.getTimestamp("date").toInstant(),
                        EvenementJournal.valueOf(rs.getString("evenement")),
                        rs.getObject("utilisateur_id", Long.class), rs.getString("nom_acteur"),
                        CoteJournal.valueOf(rs.getString("cote")), rs.getString("detail")))
                .list();
    }
}
