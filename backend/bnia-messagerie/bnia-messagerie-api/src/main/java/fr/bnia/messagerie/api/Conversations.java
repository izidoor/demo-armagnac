package fr.bnia.messagerie.api;

/**
 * Appelé par les contextes métier, dans leur transaction (A-MSG-4), sur la conversation de leurs objets. Ces appels
 * ne vérifient pas de droits : le contexte métier l'a fait.
 */
public interface Conversations {

    /**
     * RG-MSG-08 : dépose dans la conversation de l'objet, ouverte au besoin, un message protégé côté BNIA, au nom de
     * l'auteur (le motif d'une demande de compléments). Lu pour le BNIA, non lu pour l'opérateur.
     */
    void deposerMotif(ObjetRef objet, Acteur auteur, String texte);

    /** RG-MSG-07 : supprime la conversation de l'objet et ses messages, s'il en a une. */
    void supprimer(ObjetRef objet);
}
