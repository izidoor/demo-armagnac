package fr.bnia.referentiels.domain;

import java.util.Objects;

/**
 * Pays (C.10.5.1) : sa zone commerciale, son appartenance à l'UE, la langue et le niveau de détail de
 * son certificat export, l'âge minimum à l'export.
 */
public final class Pays extends ValeurSimple {

    private DescriptionPays description;

    private Pays(Long id, Code code, Libelle libelle, DescriptionPays description, boolean actif) {
        super(id, code, libelle, actif);
        this.description = Objects.requireNonNull(description);
    }

    public static Pays creer(Code code, Libelle libelle, DescriptionPays description) {
        return new Pays(null, code, libelle, description, true);
    }

    public static Pays reconstituer(Long id, Code code, Libelle libelle, DescriptionPays description,
            boolean actif) {
        return new Pays(id, code, libelle, description, actif);
    }

    public DescriptionPays description() {
        return description;
    }

    /** Tout sauf l'identifiant (RG-REF-02). */
    public void decrire(Code code, Libelle libelle, DescriptionPays description) {
        recoder(code);
        renommer(libelle);
        this.description = Objects.requireNonNull(description);
    }
}
