package fr.bnia.facturation.domain;

/** Sens d'une pièce (spec 08 §3.1) : facture (code 380) ou avoir (381, 262). */
public enum SensPiece {
    FACTURE("Facture"),
    AVOIR("Avoir");

    private final String libelle;

    SensPiece(String libelle) {
        this.libelle = libelle;
    }

    public String libelle() {
        return libelle;
    }
}
