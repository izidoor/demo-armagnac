package fr.bnia.notifications.api;

/**
 * Confier une notification aux appareils de ses destinataires (spec 11, RG-NOT-03), appelé par l'infra d'un contexte
 * métier, dans sa transaction : l'envoi part après la validation de celle-ci, et un échec n'annule rien.
 */
public interface Notifications {

    void envoyer(Notification notification);
}
