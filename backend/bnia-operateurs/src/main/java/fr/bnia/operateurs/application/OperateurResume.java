package fr.bnia.operateurs.application;

/** Désignation courte d'une autre fiche : autres fiches du groupe (RG-OPE-09), doublons (RG-OPE-06, 08). */
public record OperateurResume(long id, String code, String raisonSociale, String ville, boolean actif) {
}
