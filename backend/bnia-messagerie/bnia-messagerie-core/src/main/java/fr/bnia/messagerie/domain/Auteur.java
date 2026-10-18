package fr.bnia.messagerie.domain;

import java.util.Objects;

/**
 * L'auteur d'un message ou d'une conversation libre : identifiant de l'utilisateur, sans clé étrangère, et copie de
 * son nom à l'instant de l'action (ADR-011, auteur d'une action).
 */
public record Auteur(long utilisateur, String nom) {

    public Auteur {
        Objects.requireNonNull(nom, "nom");
    }
}
