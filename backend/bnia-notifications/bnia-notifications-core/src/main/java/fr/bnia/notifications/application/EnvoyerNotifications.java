package fr.bnia.notifications.application;

import fr.bnia.commun.application.UseCase;
import fr.bnia.notifications.api.Notification;
import fr.bnia.notifications.api.Notifications;
import fr.bnia.notifications.domain.Abonnement;
import java.util.List;

/** Envoyer la notification d'un contexte métier aux appareils de ses destinataires (spec 11 §5.1, RG-NOT-03). */
@UseCase
public class EnvoyerNotifications implements Notifications {

    private final AbonnementRepository abonnements;
    private final ServicePush service;

    public EnvoyerNotifications(AbonnementRepository abonnements, ServicePush service) {
        this.abonnements = abonnements;
        this.service = service;
    }

    /** RG-NOT-03 : tous les appareils de tous les destinataires ; aucun appareil abonné, rien ne part. */
    @Override
    public void envoyer(Notification notification) {
        if (notification.utilisateurs().isEmpty()) {
            return;
        }
        List<Abonnement> appareils = abonnements.de(notification.utilisateurs());
        if (!appareils.isEmpty()) {
            service.envoyer(appareils, new MessagePush(notification.titre(), notification.texte(), notification.lien()));
        }
    }
}
