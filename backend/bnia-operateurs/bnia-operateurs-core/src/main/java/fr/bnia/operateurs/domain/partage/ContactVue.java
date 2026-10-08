package fr.bnia.operateurs.domain.partage;

import java.util.Optional;
import java.util.Set;

/**
 * Contact vu par les autres contextes : {@code acces} (droits au portail), {@code notifications}
 * (destinataires par centre d'intérêt, C.14 p.136).
 *
 * @param operateur      identifiant de l'opérateur
 * @param centresInteret identifiants des centres d'intérêt dans {@code referentiels}
 * @param modules        vides sans accès (RG-OPE-31) ; tous pour un administrateur (RG-OPE-30)
 */
public record ContactVue(
        long id,
        long operateur,
        String nomComplet,
        Optional<String> fonction,
        Optional<String> email,
        Set<Long> centresInteret,
        boolean acces,
        boolean administrateur,
        Set<ModulePortail> modules,
        boolean actif) {
}
