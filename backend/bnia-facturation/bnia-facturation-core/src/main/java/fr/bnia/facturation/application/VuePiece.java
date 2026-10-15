package fr.bnia.facturation.application;

import fr.bnia.facturation.domain.EtatPiece;
import fr.bnia.facturation.domain.Facture;
import java.util.List;

/**
 * Une pièce et ce qui empêche son émission (spec 08 §6.1 : « un brouillon affiche ce qui l'empêche d'être
 * émis »).
 *
 * @param empechements RG-FAC-09 ; vide pour une pièce émise, ou un brouillon prêt à l'être
 * @param gerer        l'acteur gère la facturation (RG-FAC-26) : il modifie, émet et abandonne un brouillon, et
 *                     annote toute pièce
 */
public record VuePiece(Facture piece, List<String> empechements, boolean gerer) {

    public VuePiece {
        empechements = List.copyOf(empechements);
    }

    /** L'acteur peut modifier, émettre ou abandonner ce brouillon. */
    public boolean emettre() {
        return gerer && piece.etat() == EtatPiece.BROUILLON;
    }
}
