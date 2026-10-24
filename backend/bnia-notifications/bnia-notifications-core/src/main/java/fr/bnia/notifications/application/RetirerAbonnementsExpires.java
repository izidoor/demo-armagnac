package fr.bnia.notifications.application;

import fr.bnia.commun.application.UseCase;

/**
 * Retirer l'abonnement d'un appareil que son service push dit expiré (spec 11 §5.1, RG-NOT-04) : notifications
 * désactivées dans les réglages, application désinstallée, navigateur réinitialisé.
 */
@UseCase
public class RetirerAbonnementsExpires {

    private final AbonnementRepository abonnements;

    public RetirerAbonnementsExpires(AbonnementRepository abonnements) {
        this.abonnements = abonnements;
    }

    /** RG-NOT-04 : rien si l'adresse est déjà inconnue. */
    public void retirer(String adresse) {
        abonnements.supprimer(adresse);
    }
}
