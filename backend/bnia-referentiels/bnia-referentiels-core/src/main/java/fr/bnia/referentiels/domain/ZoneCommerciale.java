package fr.bnia.referentiels.domain;

/**
 * Zone commerciale d'un pays : son continent géographique (C.10.4, H-REF-5).
 */
public final class ZoneCommerciale extends ValeurSimple {

    private ZoneCommerciale(Long id, Code code, Libelle libelle, boolean actif) {
        super(id, code, libelle, actif);
    }

    public static ZoneCommerciale creer(Code code, Libelle libelle) {
        return new ZoneCommerciale(null, code, libelle, true);
    }

    public static ZoneCommerciale reconstituer(Long id, Code code, Libelle libelle, boolean actif) {
        return new ZoneCommerciale(id, code, libelle, actif);
    }
}
