package fr.bnia.operateurs.domain;

import fr.bnia.commun.domain.ModificationConcurrente;

/**
 * RG-OPE-51 : de deux modifications concurrentes d'un même objet, la seconde est refusée. L'écran envoie
 * la version qu'il a lue ; si elle n'est plus la version courante, l'objet a changé entre-temps.
 */
final class Versions {

    private Versions() {
    }

    /**
     * @param lue    version lue par l'écran ; {@code null} : pas de contrôle (appel interne, démonstration)
     * @param objet  « La fiche de Jeanne Durand », pour le message
     */
    static void verifier(Long lue, long courante, String objet) {
        if (lue != null && lue != courante) {
            throw new ModificationConcurrente("RG-OPE-51",
                    objet + " a été modifiée entre-temps par quelqu'un d'autre : rechargez l'écran, puis recommencez.");
        }
    }
}
