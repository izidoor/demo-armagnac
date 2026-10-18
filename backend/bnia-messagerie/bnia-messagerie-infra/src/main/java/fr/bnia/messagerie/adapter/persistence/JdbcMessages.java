package fr.bnia.messagerie.adapter.persistence;

import fr.bnia.commun.domain.DocumentDepose;
import fr.bnia.messagerie.application.MessageRepository;
import fr.bnia.messagerie.domain.Auteur;
import fr.bnia.messagerie.domain.Cote;
import fr.bnia.messagerie.domain.Message;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/** Messages en SQL direct (spec 15 §3.4), à part de leur conversation ; leurs pièces jointes en table fille. */
@Repository
class JdbcMessages implements MessageRepository {

    private static final String COLONNES = "id, conversation_id, auteur_id, nom_auteur, cote, ecrit_le, modifie_le,"
            + " texte, protege, lu_operateur, lu_bnia";

    private final JdbcClient jdbcClient;

    JdbcMessages(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public Message inserer(Message m) {
        long id = jdbcClient.sql("insert into messagerie.message (conversation_id, auteur_id, nom_auteur, cote, ecrit_le,"
                        + " modifie_le, texte, protege, lu_operateur, lu_bnia) values (:conversation, :auteur, :nomAuteur,"
                        + " :cote, :ecritLe, :modifieLe, :texte, :protege, :luOperateur, :luBnia) returning id")
                .param("conversation", m.conversation())
                .param("auteur", m.auteur().utilisateur())
                .param("nomAuteur", m.auteur().nom())
                .param("cote", m.cote().name())
                .param("ecritLe", Timestamp.from(m.ecritLe()))
                .param("modifieLe", m.modifieLe() == null ? null : Timestamp.from(m.modifieLe()), Types.TIMESTAMP)
                .param("texte", m.texte())
                .param("protege", m.protege())
                .param("luOperateur", m.luParOperateur())
                .param("luBnia", m.luParBnia())
                .query(Long.class)
                .single();
        List<DocumentDepose> pieces = m.pieces();
        for (int rang = 0; rang < pieces.size(); rang++) {
            DocumentDepose d = pieces.get(rang);
            jdbcClient.sql("insert into messagerie.piece_jointe (message_id, rang, document_id, document_nom,"
                            + " document_type, document_taille) values (:message, :rang, :document, :nom, :type, :taille)")
                    .param("message", id)
                    .param("rang", rang)
                    .param("document", d.cle())
                    .param("nom", d.nom())
                    .param("type", d.type())
                    .param("taille", d.taille())
                    .update();
        }
        return parId(id).orElseThrow();
    }

    @Override
    public void mettreAJour(Message m) {
        jdbcClient.sql("update messagerie.message set modifie_le = :modifieLe, texte = :texte,"
                        + " lu_operateur = :luOperateur, lu_bnia = :luBnia where id = :id")
                .param("id", m.id())
                .param("modifieLe", m.modifieLe() == null ? null : Timestamp.from(m.modifieLe()), Types.TIMESTAMP)
                .param("texte", m.texte())
                .param("luOperateur", m.luParOperateur())
                .param("luBnia", m.luParBnia())
                .update();
    }

    @Override
    public Optional<Message> parId(long id) {
        List<Message> trouves = lire("where id = :id", Map.of("id", id));
        return trouves.stream().findFirst();
    }

    @Override
    public List<Message> deLaConversation(long conversation) {
        return lire("where conversation_id = :conversation order by ecrit_le, id", Map.of("conversation", conversation));
    }

    @Override
    public int compter(long conversation) {
        return jdbcClient.sql("select count(*) from messagerie.message where conversation_id = :conversation")
                .param("conversation", conversation)
                .query(Integer.class)
                .single();
    }

    @Override
    public void supprimer(long id) {
        jdbcClient.sql("delete from messagerie.message where id = :id").param("id", id).update();
    }

    @Override
    public void marquerLus(long conversation, Cote cote) {
        String colonne = cote == Cote.OPERATEUR ? "lu_operateur" : "lu_bnia";
        jdbcClient.sql("update messagerie.message set " + colonne + " = true where conversation_id = :conversation and not "
                        + colonne)
                .param("conversation", conversation)
                .update();
    }

    private List<Message> lire(String condition, Map<String, ?> parametres) {
        List<Ligne> lignes = jdbcClient.sql("select " + COLONNES + " from messagerie.message " + condition)
                .params(parametres)
                .query(JdbcMessages::ligne)
                .list();
        if (lignes.isEmpty()) {
            return List.of();
        }
        Map<Long, List<DocumentDepose>> pieces = jdbcClient.sql("select message_id, document_id, document_nom,"
                        + " document_type, document_taille from messagerie.piece_jointe where message_id in (:ids)"
                        + " order by message_id, rang")
                .param("ids", lignes.stream().map(Ligne::id).toList())
                .query((rs, n) -> Map.entry(rs.getLong("message_id"), new DocumentDepose(rs.getLong("document_id"),
                        rs.getString("document_nom"), rs.getString("document_type"), rs.getLong("document_taille"))))
                .list().stream()
                .collect(Collectors.groupingBy(Map.Entry::getKey,
                        Collectors.mapping(Map.Entry::getValue, Collectors.toCollection(ArrayList::new))));
        return lignes.stream().map(l -> l.versDomaine(pieces.getOrDefault(l.id(), new ArrayList<>()))).toList();
    }

    private static Ligne ligne(ResultSet rs, int n) throws SQLException {
        Timestamp modifieLe = rs.getTimestamp("modifie_le");
        return new Ligne(rs.getLong("id"), rs.getLong("conversation_id"), new Auteur(rs.getLong("auteur_id"),
                rs.getString("nom_auteur")), Cote.valueOf(rs.getString("cote")), rs.getTimestamp("ecrit_le").toInstant(),
                modifieLe == null ? null : modifieLe.toInstant(), rs.getString("texte"), rs.getBoolean("protege"),
                rs.getBoolean("lu_operateur"), rs.getBoolean("lu_bnia"));
    }

    private record Ligne(long id, long conversation, Auteur auteur, Cote cote, java.time.Instant ecritLe,
            java.time.Instant modifieLe, String texte, boolean protege, boolean luOperateur, boolean luBnia) {

        Message versDomaine(List<DocumentDepose> pieces) {
            return Message.reconstituer(id, conversation, auteur, cote, ecritLe, modifieLe, texte, protege, luOperateur,
                    luBnia, pieces);
        }
    }
}
