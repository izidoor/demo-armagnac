package fr.bnia.referentiels.domain;

/**
 * Type de visite ou de prestation d'accueil proposé par un opérateur (C.10.8.4).
 */
public final class TypeVisite extends ValeurSimple {

    private TypeVisite(Code code, Libelle libelle, boolean actif) {
        super(code, libelle, actif);
    }

    public static TypeVisite creer(Code code, Libelle libelle) {
        return new TypeVisite(code, libelle, true);
    }

    public static TypeVisite reconstituer(Code code, Libelle libelle, boolean actif) {
        return new TypeVisite(code, libelle, actif);
    }
}
