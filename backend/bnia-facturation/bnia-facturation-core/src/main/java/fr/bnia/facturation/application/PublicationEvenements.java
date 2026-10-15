package fr.bnia.facturation.application;

/**
 * Publication des événements entre contextes (ADR-013) : synchrone, dans la transaction du cas d'usage
 * (RG-FAC-25). Les types sont ceux de l'API ({@code fr.bnia.facturation.api}).
 */
public interface PublicationEvenements {

    void publier(Object evenement);
}
