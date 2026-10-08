package fr.bnia.authentification.application;

/**
 * Envoi des e-mails (H-AUT-3 : SMTP), après la validation de la transaction en cours ; un échec d'envoi
 * n'annule rien (renvoyer le code, refaire la demande).
 */
public interface EnvoiCourriels {

    void envoyer(Courriel courriel);
}
