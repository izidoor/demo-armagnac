package fr.bnia.contratsarmagnac.adapter.rest;

import fr.bnia.commun.adapter.rest.Connecte;
import fr.bnia.operateurs.domain.partage.Acteur;

/** L'utilisateur connecté, en acteur pour les cas d'usage (ADR-016 : les droits s'y vérifient). */
final class Acteurs {

    private Acteurs() {
    }

    static Acteur de(Connecte connecte) {
        return new Acteur(connecte.utilisateur());
    }
}
