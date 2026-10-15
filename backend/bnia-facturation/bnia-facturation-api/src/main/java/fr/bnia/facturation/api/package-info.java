/**
 * API de {@code facturation} (spec 08 §3.11, ADR-021) : ce que les sources appellent ({@link
 * fr.bnia.facturation.api.DemandeFactures}), implémentent ({@link fr.bnia.facturation.api.CatalogueArticles}) et
 * écoutent ({@link fr.bnia.facturation.api.FactureEmise}). Seule partie de {@code facturation} visible des autres
 * contextes, et de leur infra seulement : le core d'une source a son propre port, implémenté par un adapter sur
 * cette API. Java pur ; dépend du seul noyau commun.
 */
package fr.bnia.facturation.api;
