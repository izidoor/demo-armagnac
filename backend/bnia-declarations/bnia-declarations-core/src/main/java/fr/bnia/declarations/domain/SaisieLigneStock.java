package fr.bnia.declarations.domain;

import fr.bnia.commun.domain.VolumeAlcoolPur;

/**
 * Une ligne de stock initial telle que saisie (RG-DEC-07) : contrôlée par le point de départ.
 *
 * @param compte ignoré pour une ligne {@code MB}, dont le compte est calculé (RG-DEC-12)
 */
public record SaisieLigneStock(CleConditionnement cle, Integer millesime, String denomination, String compte,
        VolumeAlcoolPur volume) {
}
