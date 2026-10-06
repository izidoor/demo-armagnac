package fr.bnia.referentiels.domain.partage;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

/**
 * Taux applicable d'une classe de TVA (RG-REF-12). Un contexte qui calcule un montant fige ce taux sur
 * son propre document (spec 01, §5.1).
 *
 * @param classeTva code de la classe
 * @param taux      pourcentage, deux décimales
 */
public record TauxTvaVue(String classeTva, BigDecimal taux, LocalDate debut, Optional<LocalDate> fin) {
}
