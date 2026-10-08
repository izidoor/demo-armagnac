package fr.bnia.operateurs.domain.partage;

/**
 * Émis par {@code acces} : la personne a changé l'e-mail de son compte dans « Mon compte ».
 * {@code operateurs} reporte la nouvelle adresse sur tous les contacts qui portaient l'ancienne
 * (RG-OPE-32, ADR-013).
 */
public record IdentifiantCompteModifie(String ancien, String nouveau) {
}
