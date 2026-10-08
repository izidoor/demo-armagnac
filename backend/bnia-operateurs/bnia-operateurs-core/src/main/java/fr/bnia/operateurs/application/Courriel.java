package fr.bnia.operateurs.application;

/** Un e-mail à envoyer, en texte (invitation, changement d'e-mail, blocage). */
public record Courriel(String destinataire, String sujet, String texte) {
}
