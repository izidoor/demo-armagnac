package fr.bnia.referentiels.adapter.persistence;

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
}
