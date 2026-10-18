package fr.bnia.facturation.domain;

/** État d'une pièce (spec 08 §3.1) : brouillon, puis émise, sans retour (RG-FAC-14). */
public enum EtatPiece {
    BROUILLON("Brouillon"),
    EMISE("Émise");

    private final String libelle;

    EtatPiece(String libelle) {
        this.libelle = libelle;
    }

    public String libelle() {
        return libelle;
    }
}
