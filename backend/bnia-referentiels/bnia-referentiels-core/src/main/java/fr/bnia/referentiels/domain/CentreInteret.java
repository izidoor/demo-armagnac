package fr.bnia.referentiels.domain;

/**
 * Domaine d'activité d'un contact : comptabilité, marketing, production… (C.10.8.6).
 */
public final class CentreInteret extends ValeurSimple {

    private CentreInteret(Long id, Code code, Libelle libelle, boolean actif) {
        super(id, code, libelle, actif);
    }

    public static CentreInteret creer(Code code, Libelle libelle) {
        return new CentreInteret(null, code, libelle, true);
    }

    public static CentreInteret reconstituer(Long id, Code code, Libelle libelle, boolean actif) {
        return new CentreInteret(id, code, libelle, actif);
    }
}
