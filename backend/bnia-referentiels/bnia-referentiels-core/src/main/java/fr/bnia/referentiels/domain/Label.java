package fr.bnia.referentiels.domain;

/**
 * Label ou distinction (C.10.8.3).
 */
public final class Label extends ValeurSimple {

    private Label(Long id, Code code, Libelle libelle, boolean actif) {
        super(id, code, libelle, actif);
    }

    public static Label creer(Code code, Libelle libelle) {
        return new Label(null, code, libelle, true);
    }

    public static Label reconstituer(Long id, Code code, Libelle libelle, boolean actif) {
        return new Label(id, code, libelle, actif);
    }
}
