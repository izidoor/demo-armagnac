package fr.bnia.facturation.application;

import fr.bnia.facturation.domain.Facture;

/** Le PDF d'une pièce émise (RG-FAC-15, A-FAC-10). */
public interface EditionPiece {

    byte[] pdf(Facture piece);
}
