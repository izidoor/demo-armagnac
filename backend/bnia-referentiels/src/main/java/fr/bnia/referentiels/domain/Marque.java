package fr.bnia.referentiels.domain;

/**
 * Marque commerciale, utilisée dans les fiches opérateurs (C.10.9.1).
 *
 * <p>Créée au paramétrage, ou depuis la fiche opérateur au back-office comme au portail (RG-REF-14).
 * Son libellé est unique sous sa forme normalisée (RG-REF-15).
 */
public final class Marque extends ValeurSimple {

    private Marque(Code code, Libelle libelle, boolean actif) {
        super(code, libelle, actif);
    }

    public static Marque creer(Code code, Libelle libelle) {
        return new Marque(code, libelle, true);
    }

    public static Marque reconstituer(Code code, Libelle libelle, boolean actif) {
        return new Marque(code, libelle, actif);
    }

    public LibelleNormalise libelleNormalise() {
        return LibelleNormalise.de(libelle());
    }
}
