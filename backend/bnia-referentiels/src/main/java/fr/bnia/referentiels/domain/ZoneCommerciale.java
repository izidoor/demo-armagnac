package fr.bnia.referentiels.domain;

/**
 * Zone commerciale d'un pays : son continent géographique (C.10.4, H-REF-5).
 */
public final class ZoneCommerciale extends ValeurSimple {

    private ZoneCommerciale(Code code, Libelle libelle, boolean actif) {
        super(code, libelle, actif);
    }

    public static ZoneCommerciale creer(Code code, Libelle libelle) {
        return new ZoneCommerciale(code, libelle, true);
    }

    public static ZoneCommerciale reconstituer(Code code, Libelle libelle, boolean actif) {
        return new ZoneCommerciale(code, libelle, actif);
    }
}
