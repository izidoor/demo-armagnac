package fr.bnia.contratsarmagnac.application;

/**
 * Envoi des notifications push (RG-CA-18, spec 11) aux appareils abonnés des destinataires : après la validation de
 * la transaction en cours ; un échec d'envoi n'annule rien.
 */
public interface EnvoiNotifications {

    void envoyer(NotificationContrat notification);
}
