package fr.bnia.operateurs.domain.partage;

/**
 * Module de l'application (spec 02 §3.2.7, RG-OPE-31) : un ensemble de fonctions que l'on lit ou que
 * l'on gère. Chaque nouveau contexte ajoute le sien, et le tableau rôles × modules ({@link Role#gere})
 * une colonne (ADR-016).
 */
public enum Module {

    /** Contacts, fiches professionnelles, fiche et présentation de l'opérateur (dans la limite de RG-OPE-36). */
    CONTACTS("Contacts"),
    /** Invitations, rôles, suspensions, retraits ; utilisateurs et journal des connexions pour le BNIA. */
    ACCES("Accès"),
    /** Référentiels (spec 01) : opérateur BNIA seulement. */
    PARAMETRAGE("Paramétrage"),
    /** DSM, pièces, commentaires, point de départ, stocks déclarés (spec 05 §3.7). */
    DECLARATIONS("Déclarations"),
    /**
     * Factures et avoirs (spec 08 §3.12) : lus par tout rattachement actif (ceux dont son opérateur est le
     * client) ; gérés par le seul rôle tenu dans l'opérateur BNIA, comme le Paramétrage.
     */
    FACTURATION("Facturation");

    private final String libelle;

    Module(String libelle) {
        this.libelle = libelle;
    }

    public String libelle() {
        return libelle;
    }
}
