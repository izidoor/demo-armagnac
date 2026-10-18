package fr.bnia.messagerie.adapter.persistence;

import fr.bnia.messagerie.api.ObjetRef;
import fr.bnia.messagerie.api.TypeObjet;
import fr.bnia.messagerie.application.ConversationRepository;
import fr.bnia.messagerie.domain.Auteur;
import fr.bnia.messagerie.domain.Conversation;
import fr.bnia.messagerie.domain.ObjetMetier;
import fr.bnia.messagerie.domain.SujetLibre;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Optional;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/** Conversations en SQL direct (spec 15 §3.1) : un objet opaque (A-MSG-5) ou un sujet libre. */
@Repository
class JdbcConversations implements ConversationRepository {

    static final String COLONNES = "c.id, c.operateur_id, c.objet_type, c.objet_id, c.objet_libelle, c.titre,"
            + " c.auteur_id, c.nom_auteur, c.ouverte_le";

    private final JdbcClient jdbcClient;

    JdbcConversations(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public Conversation inserer(Conversation c) {
        ObjetMetier objet = c.sujet() instanceof ObjetMetier o ? o : null;
        SujetLibre libre = c.sujet() instanceof SujetLibre l ? l : null;
        long id = jdbcClient.sql("insert into messagerie.conversation (operateur_id, objet_type, objet_id, objet_libelle,"
                        + " titre, auteur_id, nom_auteur, ouverte_le) values (:operateur, :type, :objet, :libelle,"
                        + " :titre, :auteur, :nomAuteur, :ouverteLe) returning id")
                .param("operateur", c.operateur())
                .param("type", objet == null ? null : objet.type().name(), Types.VARCHAR)
                .param("objet", objet == null ? null : objet.objet().identifiant(), Types.VARCHAR)
                .param("libelle", objet == null ? null : objet.libelle(), Types.VARCHAR)
                .param("titre", libre == null ? null : libre.titre(), Types.VARCHAR)
                .param("auteur", libre == null ? null : libre.auteur().utilisateur(), Types.BIGINT)
                .param("nomAuteur", libre == null ? null : libre.auteur().nom(), Types.VARCHAR)
                .param("ouverteLe", Timestamp.from(c.ouverteLe()))
                .query(Long.class)
                .single();
        return c.avecId(id);
    }

    @Override
    public Optional<Conversation> parId(long id) {
        return jdbcClient.sql("select " + COLONNES + " from messagerie.conversation c where c.id = :id")
                .param("id", id)
                .query(JdbcConversations::conversation)
                .optional();
    }

    @Override
    public Optional<Conversation> parObjet(ObjetRef objet) {
        return jdbcClient.sql("select " + COLONNES + " from messagerie.conversation c"
                        + " where c.objet_type = :type and c.objet_id = :objet")
                .param("type", objet.type().name())
                .param("objet", objet.identifiant())
                .query(JdbcConversations::conversation)
                .optional();
    }

    @Override
    public void supprimer(long id) {
        jdbcClient.sql("delete from messagerie.conversation where id = :id").param("id", id).update();
    }

    static Conversation conversation(ResultSet rs, int n) throws SQLException {
        String type = rs.getString("objet_type");
        var sujet = type != null
                ? new ObjetMetier(new ObjetRef(TypeObjet.valueOf(type), rs.getString("objet_id")),
                        rs.getString("objet_libelle"))
                : new SujetLibre(rs.getString("titre"), new Auteur(rs.getLong("auteur_id"), rs.getString("nom_auteur")));
        return Conversation.reconstituer(rs.getLong("id"), rs.getLong("operateur_id"), sujet,
                rs.getTimestamp("ouverte_le").toInstant());
    }
}
