package fr.bnia.operateurs.domain;

import fr.bnia.commun.domain.RegleEnfreinte;
import fr.bnia.operateurs.domain.partage.PeriodiciteDsm;
import fr.bnia.operateurs.domain.partage.StatutDeclarationStock;
import fr.bnia.operateurs.domain.partage.TypeOperateur;

/**
 * Paramètres déclaratifs d'un opérateur (C.1.1.1 p.101, C.1.1.2 p.102) : périodicité DSM et statut de
 * déclaration de stock, contrôlés selon le type (RG-OPE-01, 10, 11).
 *
 * @param periodicite {@code null} pour un opérateur qui ne déclare pas ({@code AUTRE}, {@code BNIA})
 */
public record Declaratif(PeriodiciteDsm periodicite, StatutDeclarationStock statut) {

    /** Valeurs d'un opérateur créé (RG-OPE-10, 11). */
    public static Declaratif parDefaut(TypeOperateur type) {
        return new Declaratif(PeriodiciteDsm.parDefaut(type).orElse(null), StatutDeclarationStock.parDefaut(type));
    }

    /** RG-OPE-01, 10, 11 : ce que le type exige. */
    void verifierPour(TypeOperateur type) {
        if (statut == null) {
            throw new RegleEnfreinte("RG-OPE-11", "Le statut de déclaration de stock est obligatoire.");
        }
        if (!type.declare()) {
            if (periodicite != null) {
                throw new RegleEnfreinte("RG-OPE-01",
                        "Un opérateur « " + type.libelle() + " » ne déclare pas : il n'a pas de périodicité DSM.");
            }
            if (statut == StatutDeclarationStock.OUI) {
                throw new RegleEnfreinte("RG-OPE-11",
                        "Un opérateur « " + type.libelle() + " » ne peut pas avoir le statut de déclaration « Oui ».");
            }
        } else if (periodicite == null) {
            throw new RegleEnfreinte("RG-OPE-10", "La périodicité DSM est obligatoire pour un " + type.libelle().toLowerCase() + ".");
        }
    }

    /**
     * Changement de type (décision du 2026-10-06, lot 3) : rien ne change, sauf ce que le nouveau type
     * interdit. Vers {@code AUTRE} : plus de périodicité, et {@code OUI} devient {@code NON}. Depuis
     * {@code AUTRE} : valeurs par défaut du nouveau type. Entre producteur et négociant : inchangé
     * (RG-OPE-10), l'écran suggère la valeur par défaut. Le type {@code BNIA} ne change jamais (RG-OPE-39).
     */
    Declaratif ajustePour(TypeOperateur ancien, TypeOperateur nouveau) {
        if (!nouveau.declare()) {
            return new Declaratif(null, statut == StatutDeclarationStock.OUI ? StatutDeclarationStock.NON : statut);
        }
        return ancien.declare() ? this : parDefaut(nouveau);
    }
}
