package fr.bnia.contratsarmagnac.application;

import java.util.ArrayList;
import java.util.List;

final class NotificationsCaptures implements EnvoiNotifications {

    final List<NotificationContrat> envoyees = new ArrayList<>();

    @Override
    public void envoyer(NotificationContrat notification) {
        envoyees.add(notification);
    }

    List<NotificationContrat> a(long utilisateur) {
        return envoyees.stream().filter(n -> n.utilisateurs().contains(utilisateur)).toList();
    }
}
