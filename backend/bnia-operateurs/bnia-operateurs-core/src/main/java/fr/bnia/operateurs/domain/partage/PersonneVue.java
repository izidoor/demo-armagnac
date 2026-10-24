package fr.bnia.operateurs.domain.partage;

import java.util.Optional;
import java.util.Set;

/**
 * Une personne d'un opérateur, contact ou membre, vue par les autres contextes ({@code notifications} :
 * destinataires par centre d'intérêt ou par rôle, C.14 p.136).
 *
 * @param fiche          identifiant de sa fiche professionnelle, stable d'un état à l'autre (RG-OPE-30)
 * @param centresInteret identifiants des centres d'intérêt dans {@code referentiels}
 * @param role           pour un membre seulement
 * @param utilisateur    pour un membre seulement : son identifiant d'utilisateur ({@code notifications} : ses
 *                       appareils abonnés, spec 11)
 */
public record PersonneVue(
        long operateur,
        long fiche,
        String nomComplet,
        Optional<String> fonction,
        Optional<String> email,
        Set<Long> centresInteret,
        Optional<Role> role,
        Optional<Long> utilisateur) {
}
