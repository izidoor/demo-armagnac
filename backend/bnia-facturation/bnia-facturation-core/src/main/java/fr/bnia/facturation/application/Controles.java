package fr.bnia.facturation.application;

import fr.bnia.commun.domain.AccesRefuse;
import fr.bnia.facturation.api.Acteur;
import fr.bnia.facturation.domain.Auteur;

/** Droits de la facturation (RG-FAC-26), évalués par le port de l'hôte. Partagé par les cas d'usage du contexte. */
final class Controles {

    private final DroitsFacturation droits;

    Controles(DroitsFacturation droits) {
        this.droits = droits;
    }

    /** RG-FAC-26 : demander, émettre, paramétrer le vendeur. */
    void exigerGestion(Acteur acteur) {
        if (!droits.gere(acteur)) {
            throw new AccesRefuse("RG-FAC-26",
                    "Action réservée au personnel du BNIA qui gère le module « Facturation ».");
        }
    }

    /** RG-FAC-26 : lire toutes les pièces, brouillons compris (back-office). */
    void exigerLectureBnia(Acteur acteur) {
        if (!droits.litTout(acteur)) {
            throw new AccesRefuse("RG-FAC-26", "L'historique des factures est réservé au personnel du BNIA.");
        }
    }

    /** Copie du nom de l'acteur, à l'instant de l'action (ADR-011 amendé). */
    Auteur auteur(Acteur acteur) {
        return new Auteur(acteur.utilisateur(), droits.nom(acteur.utilisateur())
                .orElse("Utilisateur n° " + acteur.utilisateur()));
    }
}
