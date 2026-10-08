package fr.bnia.operateurs.adapter.persistence;

import fr.bnia.operateurs.application.JournalContacts;
import fr.bnia.operateurs.application.LigneJournal;
import fr.bnia.operateurs.domain.ActionContact;
import fr.bnia.operateurs.domain.Changement;
import fr.bnia.operateurs.domain.ChampContact;
import java.sql.Timestamp;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/** Journal des contacts en SQL direct, en ajout seul (RG-OPE-34). */
@Repository
class JdbcJournalContacts implements JournalContacts {

    private static final String S = OperateurJdbc.SCHEMA + ".";

    private final JdbcClient jdbcClient;

    JdbcJournalContacts(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public void ajouter(long operateur, LigneJournal ligne) {
        long id = jdbcClient.sql("insert into " + S + "journal_contact (operateur_id, contact_id, nom_contact, date,"
                        + " auteur, action) values (:operateur, :contact, :nom, :date, :auteur, :action) returning id")
                .param("operateur", operateur)
                .param("contact", ligne.contact())
                .param("nom", ligne.nomContact())
                .param("date", Timestamp.from(ligne.date()))
                .param("auteur", ligne.auteur())
                .param("action", ligne.action().name())
                .query(Long.class)
                .single();
        int rang = 0;
        for (Changement changement : ligne.changements()) {
            jdbcClient.sql("insert into " + S + "journal_changement (journal_id, rang, champ, avant, apres)"
                            + " values (:journal, :rang, :champ, :avant, :apres)")
                    .param("journal", id)
                    .param("rang", rang++)
                    .param("champ", changement.champ().name())
                    .param("avant", changement.avant())
                    .param("apres", changement.apres())
                    .update();
        }
    }

    @Override
    public List<LigneJournal> duContact(long operateur, long contact) {
        Map<Long, Entree> entrees = new LinkedHashMap<>();
        jdbcClient.sql("select j.id, j.date, j.auteur, j.nom_contact, j.action, c.champ, c.avant, c.apres"
                        + " from " + S + "journal_contact j left join " + S + "journal_changement c on c.journal_id = j.id"
                        + " where j.operateur_id = :operateur and j.contact_id = :contact"
                        + " order by j.date desc, j.id desc, c.rang")
                .param("operateur", operateur)
                .param("contact", contact)
                .query(rs -> {
                    Entree entree = entrees.computeIfAbsent(rs.getLong("id"), id -> {
                        try {
                            return new Entree(rs.getTimestamp("date").toInstant(), rs.getString("auteur"),
                                    rs.getString("nom_contact"), ActionContact.valueOf(rs.getString("action")),
                                    new java.util.ArrayList<>());
                        } catch (java.sql.SQLException e) {
                            throw new IllegalStateException(e);
                        }
                    });
                    String champ = rs.getString("champ");
                    if (champ != null) {
                        entree.changements().add(new Changement(ChampContact.valueOf(champ), rs.getString("avant"),
                                rs.getString("apres")));
                    }
                });
        return entrees.values().stream()
                .map(e -> new LigneJournal(e.date(), e.auteur(), contact, e.nomContact(), e.action(), e.changements()))
                .toList();
    }

    private record Entree(java.time.Instant date, String auteur, String nomContact, ActionContact action,
            List<Changement> changements) {
    }
}
