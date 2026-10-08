package fr.bnia.operateurs.application;

import java.util.Set;

/**
 * Modification d'une fiche professionnelle (vision UC-18, 25).
 *
 * @param version version lue par l'écran (RG-OPE-51)
 */
public record SaisieFiche(
        String fonction,
        String telephoneProfessionnel,
        String telephoneSecondaire,
        String adresse,
        String codePostal,
        String ville,
        Long pays,
        Set<Long> centresInteret,
        String observation,
        Long version) {
}
