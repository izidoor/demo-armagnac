package fr.bnia.authentification.application;

/** Hachage lent des mots de passe (RG-AUT-07) : jamais stockés ni journalisés en clair. */
public interface HachageMotDePasse {

    String hacher(String clair);

    boolean correspond(String clair, String empreinte);
}
