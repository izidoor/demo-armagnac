package fr.bnia.operateurs.adapter.persistence;

import fr.bnia.operateurs.application.FiltreUtilisateurs;
import fr.bnia.operateurs.application.UtilisateurLigne;
import fr.bnia.operateurs.application.UtilisateursLecture;
import fr.bnia.operateurs.domain.StatutRattachement;
import fr.bnia.operateurs.domain.StatutUtilisateur;
import fr.bnia.operateurs.domain.Textes;
import fr.bnia.operateurs.domain.partage.Role;
import fr.bnia.operateurs.domain.partage.TypeOperateur;
import java.sql.Types;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/** Utilisateurs et leurs rattachements, en SQL direct (RG-OPE-53, « Mon compte »). */
@Repository
class JdbcUtilisateursLecture implements UtilisateursLecture {

    private static final String S = OperateurJdbc.SCHEMA + ".";

    private final JdbcClient jdbcClient;

    JdbcUtilisateursLecture(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public List<UtilisateurLigne> lister(FiltreUtilisateurs filtre) {
        String recherche = Textes.facultatif(filtre.recherche());
        return lire("(cast(:motif as text) is null or u.nom ilike :motif escape '\\' or u.prenom ilike :motif escape '\\'"
                        + " or u.email ilike :motif escape '\\')"
                        + " and (cast(:statut as text) is null or u.statut = :statut)"
                        + " and (cast(:operateur as bigint) is null or exists (select 1 from " + S + "rattachement x"
                        + " where x.utilisateur_id = u.id and x.operateur_id = :operateur))",
                Map.of(), recherche == null ? null : Sql.motifContient(recherche),
                filtre.statut() == null ? null : filtre.statut().name(), filtre.operateur());
    }

    @Override
    public Optional<UtilisateurLigne> utilisateur(long id) {
        return lire("u.id = :id", Map.of("id", id), null, null, null).stream().findFirst();
    }

    /** @param condition constante, jamais une saisie utilisateur */
    private List<UtilisateurLigne> lire(String condition, Map<String, ?> parametres, String motif, String statut,
            Long operateur) {
        Map<Long, Entree> utilisateurs = new LinkedHashMap<>();
        var requete = jdbcClient.sql("select u.id, u.nom, u.prenom, u.email, u.statut, r.id as rattachement,"
                        + " o.id as operateur, o.code, o.raison_sociale, o.type, r.role, r.statut as statut_rattachement,"
                        + " o.actif from " + S + "utilisateur u"
                        + " left join " + S + "rattachement r on r.utilisateur_id = u.id"
                        + " left join " + S + "operateur o on o.id = r.operateur_id"
                        + " where " + condition + " order by u.nom, u.prenom, u.id, o.raison_sociale")
                .params(parametres);
        if (!parametres.containsKey("id")) {
            requete = requete.param("motif", motif, Types.VARCHAR).param("statut", statut, Types.VARCHAR)
                    .param("operateur", operateur, Types.BIGINT);
        }
        requete.query(rs -> {
            Entree entree = utilisateurs.computeIfAbsent(rs.getLong("id"), id -> {
                try {
                    return new Entree(id, rs.getString("nom"), rs.getString("prenom"), rs.getString("email"),
                            StatutUtilisateur.valueOf(rs.getString("statut")), new ArrayList<>());
                } catch (java.sql.SQLException e) {
                    throw new IllegalStateException(e);
                }
            });
            long rattachement = rs.getLong("rattachement");
            if (!rs.wasNull()) {
                entree.rattachements().add(new UtilisateurLigne.RattachementLigne(rattachement, rs.getLong("operateur"),
                        rs.getString("code"), rs.getString("raison_sociale"), TypeOperateur.valueOf(rs.getString("type")),
                        Role.valueOf(rs.getString("role")), StatutRattachement.valueOf(rs.getString("statut_rattachement")),
                        rs.getBoolean("actif")));
            }
        });
        return utilisateurs.values().stream()
                .map(e -> new UtilisateurLigne(e.id(), e.nom(), e.prenom(), e.email(), e.statut(), e.rattachements()))
                .toList();
    }

    private record Entree(long id, String nom, String prenom, String email, StatutUtilisateur statut,
            List<UtilisateurLigne.RattachementLigne> rattachements) {
    }
}
