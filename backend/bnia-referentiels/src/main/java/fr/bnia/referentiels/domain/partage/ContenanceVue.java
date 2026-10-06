package fr.bnia.referentiels.domain.partage;

import java.math.BigDecimal;

/**
 * Contenance vue par les autres contextes : {@code exportation} calcule le volume d'alcool pur à partir
 * du volume (§4.7.5). Une contenance est référencée par son volume (A-REF-1).
 *
 * @param volume  en litres, trois décimales
 * @param libelle libellé saisi, sinon dérivé du volume (« 0,70 L »)
 */
public record ContenanceVue(BigDecimal volume, String libelle, boolean actif) {
}
