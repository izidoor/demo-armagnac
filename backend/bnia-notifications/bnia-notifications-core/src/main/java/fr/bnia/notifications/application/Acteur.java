package fr.bnia.notifications.application;

/** L'utilisateur connecté, auteur d'une demande : on ne gère que ses propres appareils (RG-NOT-05). */
public record Acteur(long utilisateur) {
}
