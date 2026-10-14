package fr.bnia.declarations.domain;

import fr.bnia.commun.domain.VolumeAlcoolPur;
import fr.bnia.declarations.domain.GrilleCotisation.TauxEnVigueur;
import fr.bnia.declarations.domain.partage.LigneCotisation;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * Calcul de la cotisation CVO d'une DSM (spec 05 §3.6, RG-DEC-31 à 33), sans état : l'assiette par ligne de
 * cotisation, le taux en vigueur à la fin de la période, deux parts arrondies chacune au centime.
 */
public final class PolitiqueCotisation {

    private static final BigDecimal CENT = BigDecimal.valueOf(100);

    /**
     * Une ligne du détail (RG-DEC-33).
     *
     * @param hectolitres   volume en hlAP, litres ÷ 100, sans arrondi
     * @param nonAssujettie volume × montant × pourcentage exonéré, au centime
     * @param assujettie    volume × montant × (1 − pourcentage exonéré), au centime
     */
    public record LigneDetail(LigneCotisation ligne, VolumeAlcoolPur volume, BigDecimal hectolitres,
            BigDecimal nonAssujettie, BigDecimal assujettie) {

        public BigDecimal total() {
            return nonAssujettie.add(assujettie);
        }
    }

    /** Le détail de la cotisation et le taux appliqué (RG-DEC-32, 33). */
    public record Detail(TauxEnVigueur taux, List<LigneDetail> lignes) {

        public Detail {
            lignes = List.copyOf(lignes);
        }

        public BigDecimal totalNonAssujettie() {
            return lignes.stream().map(LigneDetail::nonAssujettie).reduce(zero(), BigDecimal::add);
        }

        public BigDecimal totalAssujettie() {
            return lignes.stream().map(LigneDetail::assujettie).reduce(zero(), BigDecimal::add);
        }

        /** RG-DEC-33 : somme des parts arrondies. */
        public BigDecimal totalHt() {
            return totalNonAssujettie().add(totalAssujettie());
        }
    }

    private PolitiqueCotisation() {
    }

    /** RG-DEC-31 : par ligne de cotisation, la somme des colonnes de l'assiette, colonnes grisées exclues. */
    public static Map<LigneCotisation, VolumeAlcoolPur> assiette(TypeDeclarant type,
            Function<ColonneDsm, VolumeAlcoolPur> totaux) {
        Map<LigneCotisation, VolumeAlcoolPur> assiette = new EnumMap<>(LigneCotisation.class);
        for (ColonneDsm colonne : ColonneDsm.values()) {
            if (colonne.assiettePour(type)) {
                assiette.merge(colonne.ligneCotisation().orElseThrow(), totaux.apply(colonne), VolumeAlcoolPur::plus);
            }
        }
        return assiette;
    }

    /**
     * RG-DEC-31 à 33 : le détail, une ligne par ligne de cotisation non nulle, au taux en vigueur à la fin de la
     * période ; vide si aucun taux n'est en vigueur à cette date (« non calculable », RG-DEC-32).
     *
     * @param totaux le total de la DSM pour chaque colonne
     */
    public static Optional<Detail> calculer(TypeDeclarant type, LocalDate finPeriode,
            Function<ColonneDsm, VolumeAlcoolPur> totaux, GrilleCotisation grille) {
        return grille.tauxAu(finPeriode).map(taux -> {
            BigDecimal exonere = taux.taux().pourcentageExonere().divide(CENT);
            List<LigneDetail> lignes = new ArrayList<>();
            assiette(type, totaux).forEach((ligne, volume) -> {
                if (!volume.estNul()) {
                    BigDecimal hl = volume.enHectolitres();
                    BigDecimal brut = hl.multiply(taux.taux().montant());
                    lignes.add(new LigneDetail(ligne, volume, hl, centimes(brut.multiply(exonere)),
                            centimes(brut.multiply(BigDecimal.ONE.subtract(exonere)))));
                }
            });
            return new Detail(taux, lignes);
        });
    }

    /** RG-DEC-33 : au centime, demi supérieur. */
    public static BigDecimal centimes(BigDecimal montant) {
        return montant.setScale(2, RoundingMode.HALF_UP);
    }

    private static BigDecimal zero() {
        return BigDecimal.ZERO.setScale(2);
    }
}
