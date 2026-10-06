package fr.bnia.operateurs.application;

import fr.bnia.operateurs.domain.SerieCode;
import java.util.OptionalInt;

/** A-OPE-5 : le plus grand numéro existant d'une série de codes, pour proposer le suivant (RG-OPE-03). */
public interface NumerotationLecture {

    /** Rien si aucun code n'appartient à la série. */
    OptionalInt plusGrandNumero(SerieCode serie);
}
