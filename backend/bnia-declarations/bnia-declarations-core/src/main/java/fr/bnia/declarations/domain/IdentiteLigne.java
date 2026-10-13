package fr.bnia.declarations.domain;

import fr.bnia.commun.domain.Millesime;
import fr.bnia.commun.domain.RegleEnfreinte;
import fr.bnia.declarations.domain.partage.CompteDAgeDsm;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Ce qui rend une ligne de DSM ou de stock initial unique (RG-DEC-11) : clé de conditionnement,
 * millésime ou dénomination, compte d'âge. L'égalité compare la dénomination normalisée.
 *
 * @param millesime    présent si et seulement si la clé commence par {@code M} (RG-DEC-10)
 * @param denomination présente si et seulement si la clé commence par {@code N} (RG-DEC-10)
 */
public record IdentiteLigne(CleConditionnement cle, Millesime millesime, Denomination denomination,
        CompteDAgeDsm compte) {

    public IdentiteLigne {
        Objects.requireNonNull(cle, "cle");
        Objects.requireNonNull(compte, "compte");
    }

    /**
     * RG-DEC-10, 12 : contrôle une saisie ; le compte d'une ligne {@code MB} est calculé au début de la
     * période (le compte saisi est ignoré), les autres sont saisis.
     *
     * @param debutPeriode début de la période de la DSM, ou du point de départ
     */
    public static IdentiteLigne saisie(CleConditionnement cle, Integer millesime, String denomination,
            String compte, LocalDate debutPeriode) {
        if (cle == null) {
            throw new RegleEnfreinte("RG-DEC-10", "Chaque ligne porte une clé de conditionnement (MB, MV, NB ou NV).");
        }
        boolean denominationSaisie = denomination != null && !denomination.isBlank();
        Millesime m = null;
        Denomination d = null;
        if (cle.millesimee()) {
            if (millesime == null) {
                throw new RegleEnfreinte("RG-DEC-10", "Une ligne " + cle + " porte un millésime.");
            }
            if (denominationSaisie) {
                throw new RegleEnfreinte("RG-DEC-10", "Une ligne " + cle + " porte un millésime, pas de dénomination.");
            }
            m = new Millesime(millesime);
        } else {
            if (millesime != null) {
                throw new RegleEnfreinte("RG-DEC-10", "Une ligne " + cle + " porte une dénomination, pas de millésime.");
            }
            d = Denomination.de(denomination);
        }
        CompteDAgeDsm c;
        if (cle.compteCalcule()) {
            c = CompteDAgeDsm.calcule(m, debutPeriode);
        } else if (compte == null || compte.isBlank()) {
            throw new RegleEnfreinte("RG-DEC-12", "Le compte d'âge d'une ligne " + cle + " est à saisir.");
        } else {
            c = CompteDAgeDsm.de(compte);
        }
        return new IdentiteLigne(cle, m, d, c);
    }

    /** RG-DEC-12 : le compte d'une ligne {@code MV} ne dépasse pas celui de son millésime au début de la période. */
    public boolean compteTropVieux(LocalDate debutPeriode) {
        return cle == CleConditionnement.MV && compte.estPlusVieuxQue(CompteDAgeDsm.calcule(millesime, debutPeriode));
    }

    /**
     * Pour les messages, dans l'ordre des colonnes de la grille et avec leurs intitulés :
     * « millésime 2010, clé MB, compte d'âge 14 », « dénomination XO, clé NB, compte d'âge 12 ».
     */
    public String libelle() {
        return "« " + (millesime != null ? "millésime " + millesime : "dénomination " + denomination) + ", clé " + cle
                + ", compte d'âge " + compte + " »";
    }
}
