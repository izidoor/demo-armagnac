package fr.bnia.declarations.adapter.persistence;

import static fr.bnia.declarations.adapter.persistence.Lignes.SCHEMA;

import fr.bnia.declarations.application.Commentaires;
import fr.bnia.declarations.domain.Auteur;
import fr.bnia.declarations.domain.Commentaire;
import fr.bnia.declarations.domain.partage.Cote;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/** Commentaires des DSM en SQL direct (spec 05 §3.2.4), à part de l'agrégat : ils ne changent pas sa version. */
@Repository
class JdbcCommentaires implements Commentaires {

    private static final String COLONNES = "id, dsm_id, ligne_id, ligne_supprimee, auteur_id, nom_auteur, cote,"
            + " ecrit_le, modifie_le, texte, interne, motif";

    private final JdbcClient jdbcClient;

    JdbcCommentaires(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public Commentaire inserer(Commentaire c) {
        long id = jdbcClient.sql("insert into " + SCHEMA + ".commentaire (dsm_id, ligne_id, ligne_supprimee, auteur_id,"
                        + " nom_auteur, cote, ecrit_le, modifie_le, texte, interne, motif) values (:dsm, :ligne,"
                        + " :ligneSupprimee, :auteur, :nomAuteur, :cote, :ecritLe, :modifieLe, :texte, :interne, :motif)"
                        + " returning id")
                .param("dsm", c.dsm())
                .param("ligne", c.ligne(), Types.BIGINT)
                .param("ligneSupprimee", c.ligneSupprimee(), Types.VARCHAR)
                .param("auteur", c.auteur().utilisateur())
                .param("nomAuteur", c.auteur().nom())
                .param("cote", c.cote().name())
                .param("ecritLe", Timestamp.from(c.ecritLe()))
                .param("modifieLe", c.modifieLe() == null ? null : Timestamp.from(c.modifieLe()), Types.TIMESTAMP)
                .param("texte", c.texte())
                .param("interne", c.interne())
                .param("motif", c.motif())
                .query(Long.class)
                .single();
        return parId(id).orElseThrow();
    }

    @Override
    public void mettreAJour(Commentaire c) {
        jdbcClient.sql("update " + SCHEMA + ".commentaire set ligne_id = :ligne, ligne_supprimee = :ligneSupprimee,"
                        + " modifie_le = :modifieLe, texte = :texte, interne = :interne where id = :id")
                .param("id", c.id())
                .param("ligne", c.ligne(), Types.BIGINT)
                .param("ligneSupprimee", c.ligneSupprimee(), Types.VARCHAR)
                .param("modifieLe", c.modifieLe() == null ? null : Timestamp.from(c.modifieLe()), Types.TIMESTAMP)
                .param("texte", c.texte())
                .param("interne", c.interne())
                .update();
    }

    @Override
    public Optional<Commentaire> parId(long id) {
        return jdbcClient.sql("select " + COLONNES + " from " + SCHEMA + ".commentaire where id = :id")
                .param("id", id)
                .query(JdbcCommentaires::commentaire)
                .optional();
    }

    @Override
    public List<Commentaire> deLaDsm(long dsm) {
        return jdbcClient.sql("select " + COLONNES + " from " + SCHEMA + ".commentaire where dsm_id = :dsm"
                        + " order by ecrit_le, id")
                .param("dsm", dsm)
                .query(JdbcCommentaires::commentaire)
                .list();
    }

    @Override
    public void supprimer(long id) {
        jdbcClient.sql("delete from " + SCHEMA + ".commentaire where id = :id").param("id", id).update();
    }

    @Override
    public void supprimerDeLaDsm(long dsm) {
        jdbcClient.sql("delete from " + SCHEMA + ".commentaire where dsm_id = :dsm").param("dsm", dsm).update();
    }

    private static Commentaire commentaire(ResultSet rs, int n) throws SQLException {
        Timestamp modifieLe = rs.getTimestamp("modifie_le");
        return Commentaire.reconstituer(rs.getLong("id"), rs.getLong("dsm_id"), rs.getObject("ligne_id", Long.class),
                rs.getString("ligne_supprimee"), new Auteur(rs.getLong("auteur_id"), rs.getString("nom_auteur")),
                Cote.valueOf(rs.getString("cote")), rs.getTimestamp("ecrit_le").toInstant(),
                modifieLe == null ? null : modifieLe.toInstant(), rs.getString("texte"), rs.getBoolean("interne"),
                rs.getBoolean("motif"));
    }
}
