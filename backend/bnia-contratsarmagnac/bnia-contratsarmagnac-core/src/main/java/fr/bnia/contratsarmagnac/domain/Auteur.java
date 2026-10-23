package fr.bnia.contratsarmagnac.domain;

import java.time.Instant;
import java.util.Objects;

/**
 * L'auteur d'une action sur un contrat (spec 06 §3.2, ADR-011) : sa partie, l'identifiant de l'utilisateur, sans clé
 * étrangère, la copie de son nom, et l'instant.
 */
public record Auteur(Partie partie, long utilisateur, String nom, Instant le) {

    public Auteur {
        Objects.requireNonNull(partie, "partie");
        Objects.requireNonNull(nom, "nom");
        Objects.requireNonNull(le, "le");
    }
}
