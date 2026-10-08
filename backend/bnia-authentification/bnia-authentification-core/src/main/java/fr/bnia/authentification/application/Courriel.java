package fr.bnia.authentification.application;

/** Un e-mail à envoyer, en texte : code de connexion, lien de réinitialisation. */
public record Courriel(String destinataire, String sujet, String texte) {
}
