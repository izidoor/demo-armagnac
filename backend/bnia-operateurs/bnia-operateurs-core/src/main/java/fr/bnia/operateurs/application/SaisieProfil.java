package fr.bnia.operateurs.application;

/**
 * Profil d'un utilisateur, modifié par lui seul (vision UC-23, RG-OPE-47).
 *
 * @param version version lue par l'écran (RG-OPE-51)
 */
public record SaisieProfil(Long titre, String nom, String prenom, Long version) {
}
