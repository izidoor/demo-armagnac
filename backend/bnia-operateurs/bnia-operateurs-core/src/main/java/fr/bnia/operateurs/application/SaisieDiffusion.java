package fr.bnia.operateurs.application;

import fr.bnia.operateurs.domain.partage.NiveauDiffusion;
import java.util.List;

/**
 * Saisie de l'onglet Diffusion (RG-OPE-14, 15).
 *
 * @param millesimes        années diffusables, en accord partiel seulement
 * @param accesStatistiques réservé au BNIA : un autre rôle doit laisser la valeur actuelle (RG-OPE-36)
 * @param version           version lue par l'écran (RG-OPE-51)
 */
public record SaisieDiffusion(NiveauDiffusion niveau, List<Integer> millesimes, boolean accesStatistiques,
        Long version) {
}
