package fr.bnia.facturation.application;

import fr.bnia.facturation.domain.Facture;
import fr.bnia.facturation.domain.Vendeur;
import java.util.Optional;

/**
 * L'aperçu PDF d'un brouillon (RG-FAC-15) : la mise en page de la pièce émise, marquée « projet » (bandeau et
 * filigrane), sans numéro, sans date d'émission ni échéance, avec le vendeur paramétré s'il l'est.
 */
public interface ApercuPiece {

    byte[] apercu(Facture brouillon, Optional<Vendeur> vendeur);
}
