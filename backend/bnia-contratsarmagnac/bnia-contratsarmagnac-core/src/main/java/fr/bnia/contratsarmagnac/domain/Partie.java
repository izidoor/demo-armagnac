package fr.bnia.contratsarmagnac.domain;

/** Partie d'un contrat (spec 06 §3.1, glossaire) : l'acheteur ou le vendeur. */
public enum Partie {

    ACHETEUR("Acheteur"),
    VENDEUR("Vendeur");

    private final String libelle;

    Partie(String libelle) {
        this.libelle = libelle;
    }

    public String libelle() {
        return libelle;
    }

    public Partie autre() {
        return this == ACHETEUR ? VENDEUR : ACHETEUR;
    }
}
