package fr.bnia.referentiels.application;

/**
 * Droits sur les référentiels (spec 02 RG-OPE-31 : module Paramétrage, opérateur BNIA seulement),
 * vérifiés par chaque cas d'usage d'écriture (ADR-016). Les droits sont calculés par {@code operateurs},
 * que ce contexte ne peut lire sans cycle : le port est implémenté au câblage, dans {@code bnia-boot}
 * (choix d'implémentation du 2026-10-08). Les lectures sont ouvertes à tout utilisateur connecté (listes
 * de choix du portail).
 */
public interface ControleParametrage {

    /**
     * Gérer le Paramétrage : AGENT ou ADMIN de l'opérateur BNIA.
     *
     * @throws fr.bnia.commun.domain.AccesRefuse sinon
     */
    void exigerGestion(long utilisateur);

    /**
     * RG-REF-14 : créer une marque depuis la fiche d'un opérateur, au back-office comme au portail : gérer
     * le module Contacts de cet opérateur.
     *
     * @throws fr.bnia.commun.domain.AccesRefuse sinon
     */
    void exigerGestionFiche(long utilisateur, long operateur);
}
