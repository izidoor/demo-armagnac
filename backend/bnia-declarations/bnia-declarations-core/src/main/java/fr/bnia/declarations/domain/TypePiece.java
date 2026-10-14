package fr.bnia.declarations.domain;

/** Type de pièce d'un justificatif (spec 05 §3.2.2, RG-DEC-26). {@code CONTRAT} viendra avec la spec 06. */
public enum TypePiece {

    FACTURE("Facture"),
    DAE("DAE"),
    AVOIR("Avoir"),
    DECLARATION_FABRICATION_DOUANES("Déclaration de fabrication douanes");

    private final String libelle;

    TypePiece(String libelle) {
        this.libelle = libelle;
    }

    public String libelle() {
        return libelle;
    }
}
