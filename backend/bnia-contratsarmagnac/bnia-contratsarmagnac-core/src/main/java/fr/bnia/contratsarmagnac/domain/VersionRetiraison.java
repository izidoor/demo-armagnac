package fr.bnia.contratsarmagnac.domain;

import java.util.Objects;

/** Une version d'une retiraison (spec 06 §3.4), en ajout seul : 1 à la déclaration, +1 à chaque modification. */
public record VersionRetiraison(int numero, Auteur auteur, ContenuRetiraison contenu) {

    public VersionRetiraison {
        Objects.requireNonNull(auteur, "auteur");
        Objects.requireNonNull(contenu, "contenu");
    }
}
