package fr.bnia.messagerie.domain;

/** Le sujet d'une conversation (spec 15 §3.1) : un objet métier, ou un sujet libre. */
public sealed interface Sujet permits ObjetMetier, SujetLibre {

    /** Le libellé de l'objet ou le titre du sujet libre. */
    String intitule();
}
