package fr.bnia.declarations.domain.partage;

/**
 * Ligne du détail de la cotisation d'une DSM, et de sa facture (spec 05 §3.1, H-DEC-3) : les lignes de la
 * facture d'exemple, dans leur ordre. Chaque colonne de l'assiette en alimente une (RG-DEC-31, 33).
 */
public enum LigneCotisation {

    SORTIES_FRANCE("Sorties France"),
    SORTIES_EXPORT("Sorties Export"),
    EDV_MARCHE_INTERIEUR("EDV autre produit, marché intérieur"),
    EDV_EXPORT("EDV autre produit, export");

    private final String libelle;

    LigneCotisation(String libelle) {
        this.libelle = libelle;
    }

    public String libelle() {
        return libelle;
    }
}
