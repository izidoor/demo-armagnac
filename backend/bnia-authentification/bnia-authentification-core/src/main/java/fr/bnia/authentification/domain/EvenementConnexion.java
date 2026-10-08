package fr.bnia.authentification.domain;

/** Ce que le journal des connexions retient (spec 03 §3.6, RG-AUT-12). */
public enum EvenementConnexion {

    CONNEXION_REUSSIE("Connexion réussie"),
    ECHEC_MOT_DE_PASSE("Échec : mot de passe"),
    ECHEC_CODE("Échec : code"),
    BLOCAGE_DECLENCHE("Blocage temporaire déclenché"),
    CONNEXION_REFUSEE("Connexion refusée"),
    DECONNEXION("Déconnexion"),
    MOT_DE_PASSE_DEFINI("Premier mot de passe"),
    MOT_DE_PASSE_CHANGE("Mot de passe changé"),
    REINITIALISATION_DEMANDEE("Réinitialisation demandée"),
    MOT_DE_PASSE_REINITIALISE("Mot de passe réinitialisé"),
    NAVIGATEUR_DE_CONFIANCE("Navigateur de confiance");

    private final String libelle;

    EvenementConnexion(String libelle) {
        this.libelle = libelle;
    }

    public String libelle() {
        return libelle;
    }
}
