package fr.bnia.contratsarmagnac.application;

/**
 * Publication des événements entre contextes (ADR-013) : synchrone, dans la transaction du cas d'usage. Le seul
 * événement du contexte, {@code RetiraisonValidee}, est un type de la partie publiée de {@code declarations} (A-CA-1).
 */
public interface PublicationEvenements {

    void publier(Object evenement);
}
