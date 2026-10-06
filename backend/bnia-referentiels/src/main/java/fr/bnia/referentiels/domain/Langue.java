package fr.bnia.referentiels.domain;

/**
 * Langue utilisée dans les fiches opérateurs (C.10.8.5).
 */
public final class Langue extends ValeurSimple {

    private Langue(Long id, Code code, Libelle libelle, boolean actif) {
        super(id, code, libelle, actif);
    }

    public static Langue creer(Code code, Libelle libelle) {
        return new Langue(null, code, libelle, true);
    }

    public static Langue reconstituer(Long id, Code code, Libelle libelle, boolean actif) {
        return new Langue(id, code, libelle, actif);
    }
}
