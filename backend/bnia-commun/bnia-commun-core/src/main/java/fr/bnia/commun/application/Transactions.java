package fr.bnia.commun.application;

import java.util.function.Supplier;

/**
 * Exécuter une action dans sa propre transaction (ADR-022), pour les traitements par lot où chaque élément est
 * indépendant : l'émission d'une sélection de brouillons (RG-FAC-19). Un cas d'usage ordinaire reste dans la
 * transaction unique de son appel.
 */
public interface Transactions {

    /**
     * Exécute l'action dans une nouvelle transaction, validée à son retour et annulée si elle lève ; l'exception
     * est alors relancée telle quelle. La transaction de l'appelant n'en est pas affectée.
     */
    <T> T dansUneNouvelle(Supplier<T> action);
}
