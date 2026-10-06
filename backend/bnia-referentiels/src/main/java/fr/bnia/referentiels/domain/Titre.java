package fr.bnia.referentiels.domain;

/**
 * Titre de civilité ou forme d'appel, utilisé dans les fiches, contacts et destinataires (C.10.8.1).
 */
public final class Titre extends ValeurSimple {

    private Titre(Long id, Code code, Libelle libelle, boolean actif) {
        super(id, code, libelle, actif);
    }

    public static Titre creer(Code code, Libelle libelle) {
        return new Titre(null, code, libelle, true);
    }

    public static Titre reconstituer(Long id, Code code, Libelle libelle, boolean actif) {
        return new Titre(id, code, libelle, actif);
    }
}
