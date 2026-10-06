package fr.bnia.operateurs.application;

import fr.bnia.commun.domain.Appellation;
import fr.bnia.operateurs.domain.TypeAlambic;
import fr.bnia.operateurs.domain.partage.PeriodiciteDsm;
import fr.bnia.operateurs.domain.partage.StatutDeclarationStock;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

/**
 * Saisie de l'onglet Production : déclaratif, région, appellations, alambics.
 *
 * @param region identifiant de la région, ou {@code null}
 */
public record SaisieProduction(
        PeriodiciteDsm periodicite,
        StatutDeclarationStock statutDeclarationStock,
        Long region,
        Set<Appellation> appellations,
        List<Alambic> alambics) {

    public record Alambic(String numeroAgrement, TypeAlambic type, BigDecimal capacite) {
    }
}
