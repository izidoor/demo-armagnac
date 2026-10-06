package fr.bnia.referentiels.domain;

/**
 * Label ou distinction (C.10.8.3).
 */
public final class Label extends ValeurSimple {

    private Label(Code code, Libelle libelle, boolean actif) {
        super(code, libelle, actif);
    }

    public static Label creer(Code code, Libelle libelle) {
        return new Label(code, libelle, true);
    }

    public static Label reconstituer(Code code, Libelle libelle, boolean actif) {
        return new Label(code, libelle, actif);
    }
}
