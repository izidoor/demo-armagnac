package fr.bnia.facturation.domain;

/**
 * L'auteur d'une action, copié à l'instant de l'action (ADR-011 amendé) : identifiant de l'utilisateur,
 * sans clé étrangère, et son nom, qui survit à sa suppression.
 */
public record Auteur(long utilisateur, String nom) {
}
