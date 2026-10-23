package fr.bnia.contratsarmagnac.application;

/**
 * Envoi des e-mails (RG-CA-18, A-CA-5) : après la validation de la transaction en cours ; un échec d'envoi n'annule
 * rien.
 */
public interface EnvoiCourriels {

    void envoyer(Courriel courriel);
}
