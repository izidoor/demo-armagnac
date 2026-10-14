package fr.bnia.declarations.domain;

import fr.bnia.commun.domain.RegleEnfreinte;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** RG-DEC-11 : deux lignes d'une DSM ou d'un stock initial n'ont pas la même identité. */
final class Unicite {

    private Unicite() {
    }

    static void verifier(List<IdentiteLigne> identites) {
        Set<IdentiteLigne> vues = new HashSet<>();
        for (IdentiteLigne identite : identites) {
            if (!vues.add(identite)) {
                throw new RegleEnfreinte("RG-DEC-11", "La ligne " + identite.libelle()
                        + " figure deux fois : même clé, même millésime ou dénomination, même compte d'âge.");
            }
        }
    }
}
