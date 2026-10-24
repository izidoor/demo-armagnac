package fr.bnia.contratsarmagnac.adapter.integration;

import fr.bnia.contratsarmagnac.application.EnvoiNotifications;
import fr.bnia.contratsarmagnac.application.NotificationContrat;
import fr.bnia.notifications.api.Notification;
import fr.bnia.notifications.api.Notifications;
import org.springframework.stereotype.Component;

/** Notifications push des contrats (RG-CA-18), confiées à {@code notifications}, lib en aval (ADR-024). */
@Component
class NotificationsContrats implements EnvoiNotifications {

    private final Notifications notifications;

    NotificationsContrats(Notifications notifications) {
        this.notifications = notifications;
    }

    @Override
    public void envoyer(NotificationContrat n) {
        notifications.envoyer(new Notification(n.utilisateurs(), n.titre(), n.texte(), n.lien()));
    }
}
