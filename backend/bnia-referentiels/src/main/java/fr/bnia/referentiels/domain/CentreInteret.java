package fr.bnia.referentiels.domain;

/**
 * Domaine d'activité d'un contact : comptabilité, marketing, production… (C.10.8.6).
 */
public final class CentreInteret extends ValeurSimple {

    private CentreInteret(Code code, Libelle libelle, boolean actif) {
        super(code, libelle, actif);
    }

    public static CentreInteret creer(Code code, Libelle libelle) {
        return new CentreInteret(code, libelle, true);
    }

    public static CentreInteret reconstituer(Code code, Libelle libelle, boolean actif) {
        return new CentreInteret(code, libelle, actif);
    }
}
