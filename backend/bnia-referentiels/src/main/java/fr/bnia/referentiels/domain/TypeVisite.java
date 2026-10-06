package fr.bnia.referentiels.domain;

/**
 * Type de visite ou de prestation d'accueil proposé par un opérateur (C.10.8.4).
 */
public final class TypeVisite extends ValeurSimple {

    private TypeVisite(Long id, Code code, Libelle libelle, boolean actif) {
        super(id, code, libelle, actif);
    }

    public static TypeVisite creer(Code code, Libelle libelle) {
        return new TypeVisite(null, code, libelle, true);
    }

    public static TypeVisite reconstituer(Long id, Code code, Libelle libelle, boolean actif) {
        return new TypeVisite(id, code, libelle, actif);
    }
}
