package fr.bnia.operateurs.application;

import fr.bnia.operateurs.domain.NiveauDiffusion;
import java.util.List;

/**
 * Saisie de l'onglet Diffusion (RG-OPE-14, 15).
 *
 * @param millesimes années diffusables, en accord partiel seulement
 */
public record SaisieDiffusion(NiveauDiffusion niveau, List<Integer> millesimes, boolean accesStatistiques) {
}
