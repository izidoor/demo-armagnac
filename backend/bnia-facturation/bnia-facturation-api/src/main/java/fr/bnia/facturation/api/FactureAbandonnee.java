package fr.bnia.facturation.api;

/**
 * Le brouillon d'une facture a été abandonné (spec 08 §3.11, RG-FAC-18), publié dans la transaction de
 * l'abandon (RG-FAC-25, ADR-013) : son origine est libérée, la source peut la faire facturer de nouveau.
 *
 * @param piece          identifiant du brouillon, qui n'existe plus dans {@code facturation}
 * @param client         {@code null} pour un client libre
 * @param abandonneePar  l'utilisateur qui l'a abandonné, pour le journal de la source
 */
public record FactureAbandonnee(long piece, Origine origine, ClientRef client, Acteur abandonneePar) {
}
