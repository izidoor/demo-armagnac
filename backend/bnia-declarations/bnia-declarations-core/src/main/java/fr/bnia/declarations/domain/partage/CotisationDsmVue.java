package fr.bnia.declarations.domain.partage;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * La cotisation CVO d'une DSM, publiée pour {@code facturation} (spec 05 §5.2, RG-DEC-31 à 33). Calculée à la
 * grille du jour, même si la DSM est déjà facturée.
 *
 * @param validee            la DSM est validée : seule une DSM validée se facture
 * @param facturee           la DSM a déjà une facture (RG-DEC-34)
 * @param montant            €/hlAP du taux en vigueur à la fin de la période ; absent si aucun (RG-DEC-32), et
 *                           alors pas de ligne
 * @param pourcentageExonere pourcentage exonéré de TVA de ce taux
 * @param totalHt            somme des parts arrondies (RG-DEC-33), nulle sans taux
 */
public record CotisationDsmVue(long dsm, long operateur, boolean validee, boolean facturee, LocalDate finPeriode,
        Optional<BigDecimal> montant, Optional<BigDecimal> pourcentageExonere, List<LigneCotisationVue> lignes,
        BigDecimal totalHt) {

    public CotisationDsmVue {
        lignes = List.copyOf(lignes);
    }

    /**
     * Une ligne du détail (RG-DEC-33).
     *
     * @param hectolitres volume en hlAP
     */
    public record LigneCotisationVue(LigneCotisation ligne, BigDecimal hectolitres, BigDecimal nonAssujettie,
            BigDecimal assujettie) {
    }
}
