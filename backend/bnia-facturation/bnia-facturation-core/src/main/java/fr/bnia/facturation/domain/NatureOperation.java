package fr.bnia.facturation.domain;

/** Nature de l'opération, mention obligatoire déduite des lignes article (RG-FAC-08). */
public enum NatureOperation {
    BIENS("Livraison de biens"),
    SERVICES("Prestation de services"),
    LES_DEUX("Livraison de biens et prestation de services");

    private final String libelle;

    NatureOperation(String libelle) {
        this.libelle = libelle;
    }

    public String libelle() {
        return libelle;
    }
}
