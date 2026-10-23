package fr.bnia.contratsarmagnac.domain;

import java.util.Objects;

/**
 * L'approbation d'une version par une partie (RG-CA-07, 08), en ajout seul : elle ne vaut que pour cette version.
 *
 * @param version numéro de la version approuvée
 */
public record Approbation(int version, Auteur auteur) {

    public Approbation {
        Objects.requireNonNull(auteur, "auteur");
    }

    public Partie partie() {
        return auteur.partie();
    }
}
