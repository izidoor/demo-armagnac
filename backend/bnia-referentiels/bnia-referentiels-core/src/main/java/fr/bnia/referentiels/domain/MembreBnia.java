package fr.bnia.referentiels.domain;

/**
 * Membre BNIA, valeur du champ de même nom de la fiche opérateur (C.1.1.1 p.101). Le CDC le type
 * « Référentiel » sans qu'aucune liste n'existe au paramétrage : liste ajoutée par la spec 02. Son
 * sens reste à préciser par le BNIA (Q-OPE-4).
 */
public final class MembreBnia extends ValeurSimple {

    private MembreBnia(Long id, Code code, Libelle libelle, boolean actif) {
        super(id, code, libelle, actif);
    }

    public static MembreBnia creer(Code code, Libelle libelle) {
        return new MembreBnia(null, code, libelle, true);
    }

    public static MembreBnia reconstituer(Long id, Code code, Libelle libelle, boolean actif) {
        return new MembreBnia(id, code, libelle, actif);
    }
}
