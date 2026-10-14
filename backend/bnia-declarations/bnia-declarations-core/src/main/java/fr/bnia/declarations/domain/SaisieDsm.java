package fr.bnia.declarations.domain;

import java.time.LocalDate;
import java.util.List;

/**
 * La saisie d'une DSM, envoyée en entier par « Enregistrer » ou pour la vue provisoire (spec 05 §6.3).
 *
 * @param dateDia facultative (RG-DEC-19)
 * @param lignes  toutes les lignes, dans l'ordre de l'écran
 */
public record SaisieDsm(LocalDate dateDia, List<SaisieLigneDsm> lignes) {

    public SaisieDsm {
        lignes = lignes == null ? List.of() : List.copyOf(lignes);
    }
}
