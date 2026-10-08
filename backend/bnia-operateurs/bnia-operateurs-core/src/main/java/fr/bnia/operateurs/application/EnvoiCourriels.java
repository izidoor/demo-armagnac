package fr.bnia.operateurs.application;

/**
 * Envoi des e-mails (RG-OPE-40) : après la validation de la transaction en cours ; un échec d'envoi
 * n'annule rien et se reprend par une relance.
 */
public interface EnvoiCourriels {

    void envoyer(Courriel courriel);
}
