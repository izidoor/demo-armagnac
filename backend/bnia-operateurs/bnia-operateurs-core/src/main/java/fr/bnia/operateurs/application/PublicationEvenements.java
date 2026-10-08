package fr.bnia.operateurs.application;

/**
 * Publication des événements entre contextes (ADR-013) : synchrone, dans la transaction du cas d'usage.
 * Les types sont ceux de {@code domain.partage}.
 */
public interface PublicationEvenements {

    void publier(Object evenement);
}
