package fr.bnia.declarations.domain;

import fr.bnia.commun.domain.RegleEnfreinte;
import fr.bnia.commun.domain.VolumeAlcoolPur;
import java.util.Objects;

/** Une ligne du stock initial d'un point de départ (spec 05 §3.3) : mêmes règles qu'une ligne de DSM (RG-DEC-07). */
public record LigneStockInitial(IdentiteLigne identite, VolumeAlcoolPur volume) {

    /** @throws RegleEnfreinte si le volume manque ou est négatif (RG-DEC-15) */
    public LigneStockInitial {
        Objects.requireNonNull(identite, "identite");
        if (volume == null) {
            volume = VolumeAlcoolPur.ZERO;
        }
        if (volume.estNegatif()) {
            throw new RegleEnfreinte("RG-DEC-15", "Le volume de la ligne " + identite.libelle()
                    + " est négatif : un volume est positif ou nul.");
        }
    }
}
