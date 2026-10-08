package fr.bnia.operateurs.domain.partage;

/** Réponse de {@code operateurs} à « cet utilisateur peut-il se connecter ? » (spec 03 RG-AUT-04, 09). */
public enum EtatConnexion {

    /** Non bloqué, au moins un rattachement {@code ACTIF} dans un opérateur actif. */
    AUTORISE,
    /** Bloqué par le BNIA (RG-OPE-50). */
    UTILISATEUR_BLOQUE,
    /** Aucun rattachement utilisable : suspendus, ou opérateurs inactifs (RG-OPE-26, 43). */
    AUCUN_ACCES_ACTIF,
    /** L'utilisateur n'existe pas, ou plus. */
    INCONNU
}
