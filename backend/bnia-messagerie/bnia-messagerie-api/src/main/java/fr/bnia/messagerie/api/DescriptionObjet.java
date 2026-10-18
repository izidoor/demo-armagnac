package fr.bnia.messagerie.api;

import java.util.Objects;

/**
 * Ce que le contexte d'un objet en dit à {@code messagerie} (RG-MSG-04) : l'opérateur de sa conversation et son
 * libellé, copié à l'ouverture.
 *
 * @param operateur identifiant de l'opérateur (celui de la DSM, le client de la facture)
 * @param libelle   libellé affiché (« DSM octobre 2026 », « Facture 2026000003 »)
 */
public record DescriptionObjet(long operateur, String libelle) {

    public DescriptionObjet {
        Objects.requireNonNull(libelle, "libelle");
    }
}
