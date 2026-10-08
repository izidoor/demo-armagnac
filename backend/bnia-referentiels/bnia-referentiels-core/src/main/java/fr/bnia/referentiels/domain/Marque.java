package fr.bnia.referentiels.domain;

/**
 * Marque commerciale, utilisée dans les fiches opérateurs (C.10.9.1).
 *
 * <p>Créée au paramétrage, ou depuis la fiche opérateur au back-office comme au portail (RG-REF-14).
 * Son libellé est unique sous sa forme normalisée (RG-REF-15).
 */
public final class Marque extends ValeurSimple {

    private Marque(Long id, Code code, Libelle libelle, boolean actif) {
        super(id, code, libelle, actif);
    }

    public static Marque creer(Code code, Libelle libelle) {
        return new Marque(null, code, libelle, true);
    }

    public static Marque reconstituer(Long id, Code code, Libelle libelle, boolean actif) {
        return new Marque(id, code, libelle, actif);
    }

    public LibelleNormalise libelleNormalise() {
        return LibelleNormalise.de(libelle());
    }
}
