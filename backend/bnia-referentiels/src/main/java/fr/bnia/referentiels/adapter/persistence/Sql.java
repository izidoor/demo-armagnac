package fr.bnia.referentiels.adapter.persistence;

import fr.bnia.referentiels.application.FiltreValeurs;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Utilitaires SQL du contexte.
 */
final class Sql {

    private Sql() {
    }

    /** Motif {@code like} « contient », caractères spéciaux échappés par {@code \}. */
    static String motifContient(String texte) {
        String echappe = texte.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
        return "%" + echappe + "%";
    }

    /**
     * Clause {@code where} d'un listing du back-office : état actif, et recherche sans distinction de
     * casse dans les colonnes données.
     *
     * @param colonnesRecherche colonnes de la table, jamais une saisie utilisateur
     */
    static Where where(FiltreValeurs filtre, String... colonnesRecherche) {
        List<String> conditions = new ArrayList<>();
        Map<String, Object> parametres = new HashMap<>();
        if (filtre.actif() != null) {
            conditions.add("actif = :actif");
            parametres.put("actif", filtre.actif());
        }
        if (filtre.recherche() != null) {
            conditions.add(Stream.of(colonnesRecherche)
                    .map(colonne -> colonne + " ilike :motif escape '\\'")
                    .collect(Collectors.joining(" or ", "(", ")")));
            parametres.put("motif", motifContient(filtre.recherche()));
        }
        String clause = conditions.isEmpty() ? "" : " where " + String.join(" and ", conditions);
        return new Where(clause, parametres);
    }

    record Where(String clause, Map<String, Object> parametres) {
    }
}
