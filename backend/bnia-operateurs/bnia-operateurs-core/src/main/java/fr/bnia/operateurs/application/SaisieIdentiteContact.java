package fr.bnia.operateurs.application;

/**
 * Modification de l'identité d'un contact (vision UC-11) ; sa fiche se modifie à part (UC-18).
 *
 * @param version version lue par l'écran (RG-OPE-51)
 */
public record SaisieIdentiteContact(Long titre, String nom, String prenom, String email, Long version) {
}
