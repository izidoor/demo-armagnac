package fr.bnia.facturation.application;

import fr.bnia.facturation.domain.Facture;
import java.util.List;

/**
 * Une pièce et ce qui empêche son émission (spec 08 §6.1 : « un brouillon affiche ce qui l'empêche d'être
 * émis »).
 *
 * @param empechements RG-FAC-09 ; vide pour une pièce émise, ou un brouillon prêt à l'être
 * @param emettre      l'acteur peut l'émettre : brouillon, et gestion de la facturation (RG-FAC-26)
 */
public record VuePiece(Facture piece, List<String> empechements, boolean emettre) {

    public VuePiece {
        empechements = List.copyOf(empechements);
    }
}
