package fr.bnia.referentiels.domain;

/**
 * Type de produit commercialisé par un opérateur : Armagnac, Blanche, Floc… (C.10.9.3).
 */
public final class ProduitCommercialise extends ValeurSimple {

    private ProduitCommercialise(Long id, Code code, Libelle libelle, boolean actif) {
        super(id, code, libelle, actif);
    }

    public static ProduitCommercialise creer(Code code, Libelle libelle) {
        return new ProduitCommercialise(null, code, libelle, true);
    }

    public static ProduitCommercialise reconstituer(Long id, Code code, Libelle libelle, boolean actif) {
        return new ProduitCommercialise(id, code, libelle, actif);
    }
}
