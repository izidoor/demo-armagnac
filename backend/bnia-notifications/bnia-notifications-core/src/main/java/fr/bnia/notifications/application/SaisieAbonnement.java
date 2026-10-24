package fr.bnia.notifications.application;

/** L'abonnement que le navigateur a reçu de son service push ({@code PushSubscription.toJSON()}). */
public record SaisieAbonnement(String adresse, String cleP256dh, String secretAuth) {
}
