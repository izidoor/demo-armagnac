package fr.bnia.facturation.domain;

/**
 * État d'une pièce émise dans Zeendoc (spec 08 §3.10, simulé pour le prototype) : transmise, puis payée. Une
 * pièce non transmise n'en a pas.
 */
public enum EtatZeendoc {
    TRANSMISE("Transmis"),
    PAYEE("Payé");

    private final String libelle;

    EtatZeendoc(String libelle) {
        this.libelle = libelle;
    }

    public String libelle() {
        return libelle;
    }
}
