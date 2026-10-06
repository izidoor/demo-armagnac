package fr.bnia.operateurs.domain.partage;

import java.util.Optional;

/**
 * Adresse d'un opérateur, vue par les autres contextes (facturation, certificat d'export), qui la
 * figent sur leurs documents.
 *
 * @param pays identifiant du pays dans {@code referentiels} (ADR-011)
 */
public record AdresseVue(
        Optional<String> ligne1,
        Optional<String> ligne2,
        Optional<String> ligne3,
        Optional<String> codePostal,
        Optional<String> ville,
        Optional<Long> pays) {
}
