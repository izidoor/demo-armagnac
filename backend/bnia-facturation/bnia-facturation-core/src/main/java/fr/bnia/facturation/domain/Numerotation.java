package fr.bnia.facturation.domain;

import fr.bnia.commun.domain.RegleEnfreinte;

/**
 * Compteur des numéros d'une année (spec 08 §3.8, A-FAC-5), commun aux factures et aux avoirs (H-FAC-4).
 *
 * @param dernier dernier numéro attribué cette année, 0 avant le premier
 */
public record Numerotation(int annee, long dernier) {

    private static final long MAXIMUM = 999_999;

    /** RG-FAC-12 : le numéro suivant, sans trou. */
    public Numerotation suivante() {
        if (dernier >= MAXIMUM) {
            throw new RegleEnfreinte("RG-FAC-12", "Les numéros de pièce de " + annee + " sont épuisés.");
        }
        return new Numerotation(annee, dernier + 1);
    }

    /** RG-FAC-12 : l'année suivie de 6 chiffres, {@code 2026000001}. */
    public String numero() {
        return String.format("%04d%06d", annee, dernier);
    }
}
