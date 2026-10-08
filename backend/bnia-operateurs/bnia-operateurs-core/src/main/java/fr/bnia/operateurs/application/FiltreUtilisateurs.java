package fr.bnia.operateurs.application;

import fr.bnia.operateurs.domain.StatutUtilisateur;

/**
 * Filtres de la liste des utilisateurs (spec 02 §6.1, menu Accès). Un filtre {@code null} ne filtre pas.
 *
 * @param recherche contenue dans le nom, le prénom ou l'e-mail, sans distinction de casse
 * @param operateur un rattachement à cet opérateur
 */
public record FiltreUtilisateurs(String recherche, StatutUtilisateur statut, Long operateur) {
}
