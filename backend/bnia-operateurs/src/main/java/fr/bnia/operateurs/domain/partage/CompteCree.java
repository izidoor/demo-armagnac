package fr.bnia.operateurs.domain.partage;

/**
 * Émis par {@code acces} : un compte portail existe désormais pour cet e-mail. {@code operateurs}
 * verrouille l'e-mail des contacts qui le portent (RG-OPE-32, ADR-013).
 */
public record CompteCree(String email) {
}
