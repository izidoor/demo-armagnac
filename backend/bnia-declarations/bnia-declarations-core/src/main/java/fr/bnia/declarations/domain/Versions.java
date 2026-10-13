package fr.bnia.declarations.domain;

import fr.bnia.commun.domain.ModificationConcurrente;

/**
 * Verrou optimiste (spec 05 §3.2, §3.3) : l'écran envoie la version qu'il a lue ; si elle n'est plus la
 * version courante, l'objet a changé entre-temps.
 */
final class Versions {

    private Versions() {
    }

    /**
     * @param lue   version lue par l'écran ; {@code null} : pas de contrôle (appel interne, démonstration)
     * @param objet « La DSM de juin 2026 », pour le message
     */
    static void verifier(Long lue, long courante, String objet) {
        if (lue != null && lue != courante) {
            throw new ModificationConcurrente(null,
                    objet + " a été modifiée entre-temps par quelqu'un d'autre : rechargez l'écran, puis recommencez.");
        }
    }
}
