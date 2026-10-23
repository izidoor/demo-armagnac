package fr.bnia.contratsarmagnac.domain;

/**
 * Un fait produit par le contrat, relevé par le cas d'usage après l'enregistrement : il est journalisé (RG-CA-19) et
 * peut donner lieu à un e-mail à l'autre partie (RG-CA-18).
 *
 * @param detail numéro de version ou de retiraison ; {@code null} sans objet
 */
public record Fait(ActionContrat action, Auteur auteur, Integer detail) {
}
