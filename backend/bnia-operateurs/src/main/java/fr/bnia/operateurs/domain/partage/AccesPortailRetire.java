package fr.bnia.operateurs.domain.partage;

/**
 * Un contact perd l'accès au portail pour son opérateur : accès retiré, contact désactivé ou supprimé,
 * opérateur désactivé (RG-OPE-26, 29, 33 ; ADR-013).
 *
 * @param operateur identifiant de l'opérateur
 * @param contact   identifiant du contact
 */
public record AccesPortailRetire(long operateur, long contact, String email) {
}
