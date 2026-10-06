package fr.bnia.operateurs.domain.partage;

/**
 * Module du portail ouvert à un contact (§5.13.2 p.78-79, C.1.9.2 p.105, décision du 2026-10-06). Le
 * droit « gestion des contacts » n'est pas un module : c'est le rôle d'administrateur. Un module dont le
 * contexte n'existe pas encore n'ouvre rien.
 */
public enum ModulePortail {

    DSM("Déclarations de stocks et mouvements"),
    CONTRATS_ARMAGNAC("Contrats Armagnac"),
    PLURIANNUELS_ARMAGNAC("Contrats pluriannuels Armagnac"),
    PROPOSITIONS_ARMAGNAC("Propositions Armagnac"),
    CONTRATS_VIN("Contrats vin"),
    PLURIANNUELS_VIN("Contrats pluriannuels vin"),
    PROPOSITIONS_VIN("Propositions vin"),
    FINS_DE_TRAVAUX("Fins de travaux"),
    BOUTIQUE("Boutique"),
    FACTURES("Factures"),
    /** Couvre aussi les ventes export (§5.7). */
    CERTIFICATS_EXPORT("Certificats et ventes export"),
    STATISTIQUES("Statistiques");

    private final String libelle;

    ModulePortail(String libelle) {
        this.libelle = libelle;
    }

    public String libelle() {
        return libelle;
    }
}
