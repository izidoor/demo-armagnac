package fr.bnia.referentiels.domain;

/**
 * Titre de civilité ou forme d'appel, utilisé dans les fiches, contacts et destinataires (C.10.8.1).
 */
public final class Titre extends ValeurSimple {

    private Titre(Code code, Libelle libelle, boolean actif) {
        super(code, libelle, actif);
    }

    public static Titre creer(Code code, Libelle libelle) {
        return new Titre(code, libelle, true);
    }

    public static Titre reconstituer(Code code, Libelle libelle, boolean actif) {
        return new Titre(code, libelle, actif);
    }
}
