package fr.bnia.notifications.application;

/**
 * Ce qu'un appareil affiche (spec 11 §3.2) : le titre, le texte, et le chemin ouvert par un appui, qui sert aussi
 * d'étiquette (une notification remplace la précédente du même objet).
 */
public record MessagePush(String titre, String texte, String lien) {
}
