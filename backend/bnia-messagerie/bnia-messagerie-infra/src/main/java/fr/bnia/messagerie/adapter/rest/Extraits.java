package fr.bnia.messagerie.adapter.rest;

/** La vue tronquée d'un message (RG-MSG-11) : sa première ligne, 140 caractères au plus. */
final class Extraits {

    private static final int LONGUEUR = 140;

    private Extraits() {
    }

    static String premiereLigne(String texte) {
        if (texte == null) {
            return "";
        }
        String ligne = texte.strip().lines().findFirst().orElse("").strip();
        boolean coupe = ligne.length() > LONGUEUR || texte.strip().lines().count() > 1;
        ligne = ligne.length() > LONGUEUR ? ligne.substring(0, LONGUEUR).strip() : ligne;
        return coupe ? ligne + "…" : ligne;
    }
}
