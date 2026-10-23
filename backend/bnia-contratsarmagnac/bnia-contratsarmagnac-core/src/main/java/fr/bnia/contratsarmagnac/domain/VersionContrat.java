package fr.bnia.contratsarmagnac.domain;

import java.util.Objects;

/** Une version du contrat (spec 06 §3.2), en ajout seul : 1 à l'ouverture, +1 à chaque modification. */
public record VersionContrat(int numero, Auteur auteur, ContenuContrat contenu) {

    public VersionContrat {
        Objects.requireNonNull(auteur, "auteur");
        Objects.requireNonNull(contenu, "contenu");
    }
}
