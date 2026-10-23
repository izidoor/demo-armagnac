package fr.bnia.contratsarmagnac.application;

/** Un e-mail à envoyer, en texte. */
public record Courriel(String destinataire, String sujet, String texte) {
}
