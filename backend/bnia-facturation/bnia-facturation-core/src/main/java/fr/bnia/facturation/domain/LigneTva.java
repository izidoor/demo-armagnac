package fr.bnia.facturation.domain;

import fr.bnia.facturation.api.CategorieTva;
import java.math.BigDecimal;

/**
 * Une ligne du récapitulatif de TVA (RG-FAC-05, EN 16931 BR-CO-17) : les lignes article d'une même catégorie et
 * d'un même taux.
 *
 * @param base somme des HT des lignes
 * @param tva  base × taux, au centime, demi supérieur
 */
public record LigneTva(CategorieTva categorie, BigDecimal base, BigDecimal tva) {
}
