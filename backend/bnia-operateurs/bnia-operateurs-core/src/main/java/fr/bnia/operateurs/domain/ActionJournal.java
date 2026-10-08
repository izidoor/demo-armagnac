package fr.bnia.operateurs.domain;

/** Ce qui est journalisé sur les personnes et les accès (RG-OPE-34). */
public enum ActionJournal {

    CONTACT_CREE("Contact déclaré"),
    CONTACT_MODIFIE("Contact modifié"),
    CONTACT_SUPPRIME("Contact supprimé"),
    FICHE_MODIFIEE("Fiche professionnelle modifiée"),
    INVITATION_ENVOYEE("Invitation envoyée"),
    INVITATION_RELANCEE("Invitation relancée"),
    INVITATION_ANNULEE("Invitation annulée"),
    INVITATION_EXPIREE("Invitation expirée"),
    INVITATION_ACCEPTEE("Invitation acceptée"),
    ROLE_CHANGE("Rôle changé"),
    ACCES_SUSPENDU("Accès suspendu"),
    ACCES_REACTIVE("Accès réactivé"),
    ACCES_RETIRE("Accès retiré"),
    PROFIL_MODIFIE("Profil modifié"),
    EMAIL_MODIFIE("E-mail modifié"),
    UTILISATEUR_BLOQUE("Utilisateur bloqué par le BNIA"),
    UTILISATEUR_DEBLOQUE("Utilisateur débloqué par le BNIA"),
    UTILISATEUR_SUPPRIME("Utilisateur supprimé");

    private final String libelle;

    ActionJournal(String libelle) {
        this.libelle = libelle;
    }

    public String libelle() {
        return libelle;
    }
}
