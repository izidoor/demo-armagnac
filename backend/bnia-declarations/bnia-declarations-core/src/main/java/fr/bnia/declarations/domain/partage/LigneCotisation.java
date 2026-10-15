package fr.bnia.declarations.domain.partage;

/**
 * Ligne du détail de la cotisation d'une DSM, et de sa facture (spec 05 §3.1, H-DEC-3) : les lignes de la
 * facture d'exemple, dans leur ordre. Chaque colonne de l'assiette en alimente une (RG-DEC-31, 33). Sur la
 * facture, les lignes d'une même famille forment un groupe (spec 08 §3.13).
 */
public enum LigneCotisation {

    SORTIES_FRANCE("Sorties France", "Commerce de l'Armagnac"),
    SORTIES_EXPORT("Sorties Export", "Commerce de l'Armagnac"),
    EDV_MARCHE_INTERIEUR("EDV autre produit, marché intérieur", "EDV autre produit"),
    EDV_EXPORT("EDV autre produit, export", "EDV autre produit");

    private final String libelle;
    private final String famille;

    LigneCotisation(String libelle, String famille) {
        this.libelle = libelle;
        this.famille = famille;
    }

    public String libelle() {
        return libelle;
    }

    /** Titre du groupe de la facture qui réunit les lignes de cette famille (spec 08 §3.13). */
    public String famille() {
        return famille;
    }
}
