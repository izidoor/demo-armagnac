package fr.bnia.referentiels.domain;

/**
 * Langue utilisée dans les fiches opérateurs (C.10.8.5).
 */
public final class Langue extends ValeurSimple {

    private Langue(Code code, Libelle libelle, boolean actif) {
        super(code, libelle, actif);
    }

    public static Langue creer(Code code, Libelle libelle) {
        return new Langue(code, libelle, true);
    }

    public static Langue reconstituer(Code code, Libelle libelle, boolean actif) {
        return new Langue(code, libelle, actif);
    }
}
