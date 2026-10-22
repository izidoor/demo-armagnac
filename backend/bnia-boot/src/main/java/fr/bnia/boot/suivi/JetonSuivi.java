package fr.bnia.boot.suivi;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/** Jeton de l'export du suivi ({@code BNIA_SUIVI_JETON}) ; vide : export fermé. */
record JetonSuivi(String valeur) {

    private static final String PREFIXE = "Bearer ";

    JetonSuivi {
        valeur = valeur == null ? "" : valeur.strip();
    }

    boolean configure() {
        return !valeur.isEmpty();
    }

    /** L'en-tête {@code Authorization} porte-t-il le jeton ? Comparaison en temps constant. */
    boolean accepte(String autorisation) {
        if (!configure() || autorisation == null || !autorisation.startsWith(PREFIXE)) {
            return false;
        }
        return MessageDigest.isEqual(valeur.getBytes(StandardCharsets.UTF_8),
                autorisation.substring(PREFIXE.length()).strip().getBytes(StandardCharsets.UTF_8));
    }
}
