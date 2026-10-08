package fr.bnia.operateurs.application;

/**
 * Identité confirmée par une personne nouvelle à l'acceptation de son invitation (RG-OPE-42), préremplie
 * avec celle de l'invitation ; ignorée pour un utilisateur existant, dont l'identité prime.
 */
public record SaisieAcceptation(Long titre, String nom, String prenom) {
}
