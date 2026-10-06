package fr.bnia.operateurs.adapter.persistence;

/**
 * Utilitaires SQL du contexte.
 */
final class Sql {

    /**
     * RG-OPE-02 : condition SQL d'une fiche incomplète, sur la table {@code operateur} aliasée {@code o}.
     * Même liste que {@code Operateur.champsManquants()}.
     */
    static final String INCOMPLET =
            "(o.adresse_ligne1 is null or o.code_postal is null or o.ville is null or o.pays_id is null)";

    private Sql() {
    }

    /** Motif {@code like} « contient », caractères spéciaux échappés par {@code \}. */
    static String motifContient(String texte) {
        return "%" + echapper(texte) + "%";
    }

    /** Motif {@code like} « commence par ». */
    static String motifCommencePar(String texte) {
        return echapper(texte) + "%";
    }

    private static String echapper(String texte) {
        return texte.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}
