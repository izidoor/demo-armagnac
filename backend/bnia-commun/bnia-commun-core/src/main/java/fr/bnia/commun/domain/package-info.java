/**
 * Noyau partagé du domaine (ADR-009) : concepts transverses du glossaire, qui sont des règles et non
 * des données administrées ({@link fr.bnia.commun.domain.CompteDAge}, {@link fr.bnia.commun.domain.Millesime},
 * {@link fr.bnia.commun.domain.Appellation}), et exceptions métier communes
 * ({@link fr.bnia.commun.domain.RegleEnfreinte}, {@link fr.bnia.commun.domain.ConflitUnicite}, {@link fr.bnia.commun.domain.ElementUtilise},
 * {@link fr.bnia.commun.domain.ElementIntrouvable}, {@link fr.bnia.commun.domain.ModificationConcurrente},
 * {@link fr.bnia.commun.domain.AccesRefuse}).
 *
 * <p>Java pur : aucune dépendance à un framework ni annotation de persistance (ADR-002).
 * Tous les contextes peuvent l'importer ; il ne dépend d'aucun contexte.
 */
package fr.bnia.commun.domain;
