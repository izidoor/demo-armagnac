package fr.bnia.referentiels.domain;

/**
 * Région de rattachement d'un opérateur (C.10.6.1). L'indicateur « en région délimitée » distingue la
 * région Armagnac du reste (C.1.1.2 p.102, colonnes « en » et « hors région délimitée » de la DSM).
 */
public final class Region extends ValeurSimple {

    private boolean enRegionDelimitee;

    private Region(Long id, Code code, Libelle libelle, boolean enRegionDelimitee, boolean actif) {
        super(id, code, libelle, actif);
        this.enRegionDelimitee = enRegionDelimitee;
    }

    public static Region creer(Code code, Libelle libelle, boolean enRegionDelimitee) {
        return new Region(null, code, libelle, enRegionDelimitee, true);
    }

    public static Region reconstituer(Long id, Code code, Libelle libelle, boolean enRegionDelimitee,
            boolean actif) {
        return new Region(id, code, libelle, enRegionDelimitee, actif);
    }

    public boolean estEnRegionDelimitee() {
        return enRegionDelimitee;
    }

    public void decrire(Code code, Libelle libelle, boolean enRegionDelimitee) {
        recoder(code);
        renommer(libelle);
        this.enRegionDelimitee = enRegionDelimitee;
    }
}
