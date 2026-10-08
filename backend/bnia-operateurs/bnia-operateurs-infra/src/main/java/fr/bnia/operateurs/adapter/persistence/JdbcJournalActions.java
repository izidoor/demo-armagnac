package fr.bnia.operateurs.adapter.persistence;

import fr.bnia.operateurs.application.JournalActions;
import fr.bnia.operateurs.application.LigneJournal;
import fr.bnia.operateurs.domain.ActionJournal;
import fr.bnia.operateurs.domain.Changement;
import fr.bnia.operateurs.domain.ChampJournal;
import fr.bnia.operateurs.domain.TypeCible;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/** Journal des personnes et des accès en SQL direct, en ajout seul (RG-OPE-34). */
@Repository
class JdbcJournalActions implements JournalActions {

    private static final String S = OperateurJdbc.SCHEMA + ".";

    private final JdbcClient jdbcClient;

    JdbcJournalActions(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public void ajouter(LigneJournal ligne) {
        long id = jdbcClient.sql("insert into " + S + "journal (date, acteur_id, nom_acteur, operateur_id, action, cible,"
                        + " id_cible, nom_cible) values (:date, :acteur, :nomActeur, :operateur, :action, :cible, :idCible,"
                        + " :nomCible) returning id")
                .param("date", Timestamp.from(ligne.date()))
                .param("acteur", ligne.acteur(), Types.BIGINT)
                .param("nomActeur", ligne.nomActeur())
                .param("operateur", ligne.operateur(), Types.BIGINT)
                .param("action", ligne.action().name())
                .param("cible", ligne.cible().name())
                .param("idCible", ligne.idCible())
                .param("nomCible", ligne.nomCible())
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
    public List<LigneJournal> deLOperateur(long operateur) {
        return lire("j.operateur_id = :operateur", Map.of("operateur", operateur));
    }

    @Override
    public List<LigneJournal> deLaCible(TypeCible cible, long id) {
        return lire("j.cible = :cible and j.id_cible = :id", Map.of("cible", cible.name(), "id", id));
    }

    /** @param condition constante, jamais une saisie utilisateur */
    private List<LigneJournal> lire(String condition, Map<String, ?> parametres) {
        Map<Long, Entree> entrees = new LinkedHashMap<>();
        jdbcClient.sql("select j.id, j.date, j.acteur_id, j.nom_acteur, j.operateur_id, j.action, j.cible, j.id_cible,"
                        + " j.nom_cible, c.champ, c.avant, c.apres from " + S + "journal j"
                        + " left join " + S + "journal_changement c on c.journal_id = j.id"
                        + " where " + condition + " order by j.date desc, j.id desc, c.rang")
                .params(parametres)
                .query(rs -> {
                    Entree entree = entrees.computeIfAbsent(rs.getLong("id"), id -> entree(rs));
                    String champ = rs.getString("champ");
                    if (champ != null) {
                        entree.changements().add(new Changement(ChampJournal.valueOf(champ), rs.getString("avant"),
                                rs.getString("apres")));
                    }
                });
        return entrees.values().stream()
                .map(e -> new LigneJournal(e.ligne().date(), e.ligne().acteur(), e.ligne().nomActeur(),
                        e.ligne().operateur(), e.ligne().action(), e.ligne().cible(), e.ligne().idCible(),
                        e.ligne().nomCible(), e.changements()))
                .toList();
    }

    private static Entree entree(ResultSet rs) {
        try {
            long acteur = rs.getLong("acteur_id");
            Long idActeur = rs.wasNull() ? null : acteur;
            long operateur = rs.getLong("operateur_id");
            Long idOperateur = rs.wasNull() ? null : operateur;
            return new Entree(new LigneJournal(rs.getTimestamp("date").toInstant(), idActeur, rs.getString("nom_acteur"),
                    idOperateur, ActionJournal.valueOf(rs.getString("action")), TypeCible.valueOf(rs.getString("cible")),
                    rs.getLong("id_cible"), rs.getString("nom_cible"), List.of()), new ArrayList<>());
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    private record Entree(LigneJournal ligne, List<Changement> changements) {
    }
}
