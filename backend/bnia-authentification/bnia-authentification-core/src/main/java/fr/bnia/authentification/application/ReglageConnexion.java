package fr.bnia.authentification.application;

/** Réglage de la connexion, modifiable à l'écran par un ADMIN du BNIA (RG-AUT-16) : une seule valeur. */
public interface ReglageConnexion {

    /** RG-AUT-16 : faux au départ. */
    boolean doubleAuthentification();

    void definirDoubleAuthentification(boolean active);
}
