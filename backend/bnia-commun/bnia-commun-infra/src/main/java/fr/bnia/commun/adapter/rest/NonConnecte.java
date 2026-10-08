package fr.bnia.commun.adapter.rest;

/** Un endpoint qui exige un utilisateur connecté est appelé sans session : 401. */
public class NonConnecte extends RuntimeException {

    public NonConnecte() {
        super("Vous n'êtes pas connecté, ou votre session a expiré : connectez-vous.");
    }
}
