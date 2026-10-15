package fr.bnia.facturation.domain;

/** Normalisation des saisies textuelles du contexte. */
final class Textes {

    private Textes() {
    }

    /** Un texte vide ou blanc vaut « non renseigné » ({@code null}) ; sinon, sans espaces autour. */
    static String facultatif(String texte) {
        return texte == null || texte.isBlank() ? null : texte.strip();
    }

    /** Sans aucun espace, en majuscules : SIREN, SIRET, n° de TVA, IBAN, BIC. */
    static String compact(String texte) {
        return texte == null ? null : facultatif(texte.replaceAll("\\s", "").toUpperCase(java.util.Locale.ROOT));
    }
}
