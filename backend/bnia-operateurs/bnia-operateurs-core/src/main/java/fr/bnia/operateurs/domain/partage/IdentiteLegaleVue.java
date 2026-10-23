package fr.bnia.operateurs.domain.partage;

import java.util.Optional;

/**
 * Les identifiants et le téléphone d'un opérateur, que {@code contratsarmagnac} imprime sur le contrat (spec 06
 * RG-CA-20) : n° SIRET, n° CVI, premier téléphone.
 */
public record IdentiteLegaleVue(long id, Optional<String> siret, Optional<String> cvi, Optional<String> telephone) {
}
