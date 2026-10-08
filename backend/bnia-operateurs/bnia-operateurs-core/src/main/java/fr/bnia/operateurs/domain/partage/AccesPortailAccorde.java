package fr.bnia.operateurs.domain.partage;

/**
 * Un contact reçoit l'accès au portail pour son opérateur (RG-OPE-29, ADR-013). {@code acces} crée le
 * compte de cet e-mail s'il n'existe pas et envoie l'e-mail d'activation.
 *
 * @param operateur identifiant de l'opérateur
 * @param contact   identifiant du contact
 */
public record AccesPortailAccorde(long operateur, long contact, String email) {
}
