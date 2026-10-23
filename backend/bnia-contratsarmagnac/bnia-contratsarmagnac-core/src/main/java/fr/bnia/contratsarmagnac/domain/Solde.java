package fr.bnia.contratsarmagnac.domain;

/** Paiement du solde (CA-05 §4, AGI 3.1.1 p.16). */
public enum Solde {

    COMPTANT("Comptant"),
    TRENTE_JOURS_FIN_DE_MOIS("30 jours fin de mois");

    private final String libelle;

    Solde(String libelle) {
        this.libelle = libelle;
    }

    public String libelle() {
        return libelle;
    }
}
