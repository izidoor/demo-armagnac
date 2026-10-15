package fr.bnia.facturation.adapter.rest;

import fr.bnia.commun.adapter.rest.Connecte;
import fr.bnia.facturation.api.Acteur;

/** L'utilisateur connecté, traduit en acteur des cas d'usage (ADR-016, RG-FAC-26). */
final class Acteurs {

    private Acteurs() {
    }

    static Acteur de(Connecte connecte) {
        return new Acteur(connecte.utilisateur());
    }
}
