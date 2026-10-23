package fr.bnia.contratsarmagnac.domain;

import fr.bnia.commun.domain.RegleEnfreinte;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Une ligne d'une retiraison (spec 06 §3.4) : la ligne du contrat retirée, désignée par son rang, et le volume
 * enlevé ; son volume d'alcool pur se calcule au degré de la ligne du contrat.
 *
 * @param rang   rang de la ligne dans la version courante du contrat, à partir de 0
 * @param volume en hectolitres, 2 décimales
 */
public record LigneRetiraison(int rang, BigDecimal volume) {

    /** RG-CA-13 : un volume strictement positif, à 2 décimales. */
    public LigneRetiraison {
        if (volume == null || volume.signum() <= 0) {
            throw new RegleEnfreinte("RG-CA-13", "Le volume retiré d'une ligne est strictement positif.");
        }
        if (volume.stripTrailingZeros().scale() > 2) {
            throw new RegleEnfreinte("RG-CA-13", "Le volume retiré d'une ligne a 2 décimales au plus.");
        }
        volume = volume.setScale(2, RoundingMode.UNNECESSARY);
    }
}
