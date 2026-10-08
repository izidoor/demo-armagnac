package fr.bnia.operateurs.domain.partage;

import fr.bnia.commun.domain.Appellation;
import fr.bnia.commun.domain.Millesime;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;

/**
 * Opérateur vu par les autres contextes (ADR-012, spec 02 §5.2). Un opérateur inactif reste lisible
 * là où il est déjà référencé (RG-OPE-26).
 *
 * @param id                    identifiant technique, celui que les autres contextes stockent (ADR-011)
 * @param periodicite           absente pour un opérateur {@code AUTRE} (RG-OPE-01)
 * @param statutDeclarationStock seul {@code OUI} rend une DSM attendue (RG-OPE-11)
 * @param region                identifiant de la région ; « en région délimitée » se lit dans {@code referentiels}
 * @param millesimesDiffusables en accord partiel seulement (RG-OPE-14)
 * @param emailFacturation      adresse du destinataire des factures (RG-OPE-23)
 */
public record OperateurVue(
        long id,
        String code,
        TypeOperateur type,
        String raisonSociale,
        Optional<String> nomCommercial,
        AdresseVue adresse,
        Optional<PeriodiciteDsm> periodicite,
        StatutDeclarationStock statutDeclarationStock,
        Optional<Long> region,
        Set<Appellation> appellations,
        NiveauDiffusion niveauDiffusion,
        SortedSet<Millesime> millesimesDiffusables,
        boolean accesStatistiques,
        Optional<String> emailFacturation,
        boolean actif) {
}
