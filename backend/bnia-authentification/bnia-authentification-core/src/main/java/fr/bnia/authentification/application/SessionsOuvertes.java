package fr.bnia.authentification.application;

/** Les sessions serveur d'un utilisateur (A-AUT-3), fermées à la réinitialisation de son mot de passe (RG-AUT-08). */
public interface SessionsOuvertes {

    void fermer(long utilisateur);
}
