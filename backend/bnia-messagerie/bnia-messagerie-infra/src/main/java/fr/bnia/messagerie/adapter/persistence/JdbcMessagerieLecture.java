package fr.bnia.messagerie.adapter.persistence;

import fr.bnia.messagerie.api.TypeObjet;
import fr.bnia.messagerie.application.MessagerieLecture;
import fr.bnia.messagerie.domain.Cote;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/**
 * Nouveaux messages et conversations en SQL direct (spec 15 §5.2, ADR-004). La portée se traduit en conditions sur
 * la conversation ; la recherche ignore la casse et les accents ({@code unaccent}, A-MSG-8).
 */
@Repository
class JdbcMessagerieLecture implements MessagerieLecture {

    private final JdbcClient jdbcClient;

    JdbcMessagerieLecture(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public List<NouveauMessage> nouveaux(Portee portee) {
        Map<String, Object> parametres = new HashMap<>();
        String condition = portee(portee, parametres) + " and not m." + lu(portee.cote());
        return jdbcClient.sql("select m.id, m.conversation_id, m.nom_auteur, m.ecrit_le, m.texte, c.operateur_id,"
                        + " c.objet_type, c.objet_id, coalesce(c.objet_libelle, c.titre) as intitule"
                        + " from messagerie.message m join messagerie.conversation c on c.id = m.conversation_id"
                        + " where " + condition + " order by m.ecrit_le desc, m.id desc")
                .params(parametres)
                .query(JdbcMessagerieLecture::nouveau)
                .list();
    }

    @Override
    public List<ConversationListee> conversations(Portee portee, Filtre filtre) {
        Map<String, Object> parametres = new HashMap<>();
        List<String> conditions = new ArrayList<>();
        conditions.add(portee(portee, parametres));
        filtre.operateur().ifPresent(o -> {
            conditions.add("c.operateur_id = :filtreOperateur");
            parametres.put("filtreOperateur", o);
        });
        if (filtre.libres()) {
            conditions.add("c.objet_type is null");
        } else {
            filtre.type().ifPresent(t -> {
                conditions.add("c.objet_type = :filtreType");
                parametres.put("filtreType", t.name());
            });
        }
        String nonLus = "(select count(*) from messagerie.message n where n.conversation_id = c.id and not n."
                + lu(portee.cote()) + ")";
        if (filtre.nonLuesSeulement()) {
            conditions.add(nonLus + " > 0");
        }
        String extrait = "(select e.texte from messagerie.message e where e.conversation_id = c.id"
                + " order by e.ecrit_le desc, e.id desc limit 1)";
        if (filtre.recherche().isPresent()) {
            parametres.put("motif", "%" + echappe(filtre.recherche().get()) + "%");
            String correspond = "unaccent(lower(%s)) like unaccent(lower(:motif)) escape '\\'";
            conditions.add("(exists (select 1 from messagerie.message r where r.conversation_id = c.id and "
                    + correspond.formatted("r.texte") + ") or " + correspond.formatted("coalesce(c.objet_libelle, c.titre)")
                    + ")");
            extrait = "coalesce((select e.texte from messagerie.message e where e.conversation_id = c.id and "
                    + correspond.formatted("e.texte") + " order by e.ecrit_le desc, e.id desc limit 1), " + extrait + ")";
        }
        return jdbcClient.sql("select c.id, c.operateur_id, c.objet_type, c.objet_id,"
                        + " coalesce(c.objet_libelle, c.titre) as intitule, c.nom_auteur,"
                        + " (select max(d.ecrit_le) from messagerie.message d where d.conversation_id = c.id) as dernier,"
                        + " (select count(*) from messagerie.message t where t.conversation_id = c.id) as messages, "
                        + nonLus + " as non_lus, " + extrait + " as extrait"
                        + " from messagerie.conversation c where " + String.join(" and ", conditions)
                        + " order by dernier desc nulls last, c.id desc")
                .params(parametres)
                .query(JdbcMessagerieLecture::listee)
                .list();
    }

    /** RG-MSG-01, 02, 11 : les conversations de la portée. */
    private static String portee(Portee portee, Map<String, Object> parametres) {
        List<String> conditions = new ArrayList<>();
        portee.operateur().ifPresent(o -> {
            conditions.add("c.operateur_id = :operateur");
            parametres.put("operateur", o);
        });
        String objets = portee.types().isEmpty() ? "false" : "c.objet_type in (" + portee.types().stream()
                .map(t -> "'" + t.name() + "'").collect(Collectors.joining(", ")) + ")";
        String libres = portee.auteur().map(a -> {
            parametres.put("auteur", a);
            return "(c.objet_type is null and c.auteur_id = :auteur)";
        }).orElse("c.objet_type is null");
        conditions.add("(" + objets + " or " + libres + ")");
        return String.join(" and ", conditions);
    }

    private static String lu(Cote cote) {
        return cote == Cote.OPERATEUR ? "lu_operateur" : "lu_bnia";
    }

    private static String echappe(String texte) {
        return texte.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }

    private static NouveauMessage nouveau(ResultSet rs, int n) throws SQLException {
        String type = rs.getString("objet_type");
        return new NouveauMessage(rs.getLong("id"), rs.getLong("conversation_id"),
                Optional.ofNullable(type).map(TypeObjet::valueOf), Optional.ofNullable(rs.getString("objet_id")),
                rs.getString("intitule"), rs.getLong("operateur_id"), rs.getString("nom_auteur"),
                rs.getTimestamp("ecrit_le").toInstant(), rs.getString("texte"));
    }

    private static ConversationListee listee(ResultSet rs, int n) throws SQLException {
        String type = rs.getString("objet_type");
        var dernier = rs.getTimestamp("dernier");
        return new ConversationListee(rs.getLong("id"), Optional.ofNullable(type).map(TypeObjet::valueOf),
                Optional.ofNullable(rs.getString("objet_id")), rs.getString("intitule"), rs.getLong("operateur_id"),
                Optional.ofNullable(rs.getString("nom_auteur")), dernier == null ? null : dernier.toInstant(),
                rs.getInt("non_lus"), rs.getInt("messages"), rs.getString("extrait"));
    }
}
