/**
 * Noyau partagé du domaine (ADR-009) : concepts transverses du glossaire, qui sont des règles et non
 * des données administrées ({@link fr.bnia.commun.domain.CompteDAge}), et exceptions métier communes
 * ({@link fr.bnia.commun.domain.RegleEnfreinte}, {@link fr.bnia.commun.domain.ConflitUnicite},
 * {@link fr.bnia.commun.domain.ElementIntrouvable}).
 *
 * <p>Java pur : aucune dépendance à un framework ni annotation de persistance (ADR-002).
 * Tous les contextes peuvent l'importer ; il ne dépend d'aucun contexte.
 */
package fr.bnia.commun.domain;
