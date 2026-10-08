package fr.bnia.operateurs.domain;

import fr.bnia.commun.domain.RegleEnfreinte;
import java.math.BigDecimal;

/**
 * Alambic d'un opérateur (C.1.2.1 p.102) : type obligatoire, capacité strictement positive si elle est
 * renseignée (RG-OPE-13).
 *
 * @param numeroAgrement facultatif
 * @param capacite       facultative ; unité propre au type, inconnue à ce jour (Q-OPE-8)
 */
public record Alambic(String numeroAgrement, TypeAlambic type, BigDecimal capacite) {

    public Alambic {
        if (type == null) {
            throw new RegleEnfreinte("RG-OPE-13", "Le type de l'alambic est obligatoire.");
        }
        if (capacite != null && capacite.signum() <= 0) {
            throw new RegleEnfreinte("RG-OPE-13", "La capacité d'un alambic doit être strictement positive : " + capacite + ".");
        }
        numeroAgrement = Textes.facultatif(numeroAgrement);
    }
}
