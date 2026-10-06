package fr.bnia.referentiels.domain;

/**
 * Type de produit commercialisé par un opérateur : Armagnac, Blanche, Floc… (C.10.9.3).
 */
public final class ProduitCommercialise extends ValeurSimple {

    private ProduitCommercialise(Code code, Libelle libelle, boolean actif) {
        super(code, libelle, actif);
    }

    public static ProduitCommercialise creer(Code code, Libelle libelle) {
        return new ProduitCommercialise(code, libelle, true);
    }

    public static ProduitCommercialise reconstituer(Code code, Libelle libelle, boolean actif) {
        return new ProduitCommercialise(code, libelle, actif);
    }
}
