package fr.bnia.contratsarmagnac.domain;

import fr.bnia.commun.domain.VolumeAlcoolPur;
import fr.bnia.declarations.domain.partage.CompteDAgeDsm;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * La mercuriale d'un produit (spec 06 RG-CA-26, H-CA-9) : sur les lignes des contrats conclus ou exécutés d'une
 * période, par millésime, ou par dénomination (comparée comme RG-DEC-11) et compte d'âge : nombre de contrats, volume
 * d'alcool pur, prix minimum, moyen pondéré par le volume d'alcool pur, et maximum, en € HT / hl AP.
 *
 * @param libelle « Millésime 2015 », « Hors d'âge, compte 20+ »
 */
public record Mercuriale(String libelle, Integer millesime, String denomination, CompteDAgeDsm compte,
        int contrats, VolumeAlcoolPur volumeAp, BigDecimal prixMinimum, BigDecimal prixMoyen,
        BigDecimal prixMaximum) {

    /**
     * Une ligne d'un contrat conclu ou exécuté, sur sa version courante.
     *
     * @param compte compte d'âge de la ligne, à sa saisie
     */
    public record LigneConclue(long contrat, Integer millesime, String denomination, CompteDAgeDsm compte,
            VolumeAlcoolPur volumeAp, BigDecimal prix) {

        public LigneConclue {
            Objects.requireNonNull(volumeAp, "volumeAp");
            Objects.requireNonNull(prix, "prix");
        }

        Grain grain() {
            return millesime != null ? Grain.millesime(millesime) : Grain.assemblage(denomination, compte);
        }
    }

    /**
     * RG-CA-26 : une mercuriale par produit, les millésimes du plus récent au plus ancien, puis les assemblages par
     * dénomination et compte d'âge ; prix moyen arrondi au centime, au demi supérieur.
     */
    public static List<Mercuriale> calculer(List<LigneConclue> lignes) {
        Map<Grain, List<LigneConclue>> parProduit = new LinkedHashMap<>();
        lignes.forEach(l -> parProduit.computeIfAbsent(l.grain(), g -> new ArrayList<>()).add(l));
        return parProduit.values().stream().map(Mercuriale::de)
                .sorted(Comparator.comparing((Mercuriale m) -> m.millesime() == null)
                        .thenComparing(m -> m.millesime() == null ? 0 : -m.millesime())
                        .thenComparing(m -> m.denomination() == null ? "" : m.denomination().toLowerCase())
                        .thenComparing(m -> m.compte() == null ? CompteDAgeDsm.DOUBLE_ZERO : m.compte()))
                .toList();
    }

    private static Mercuriale de(List<LigneConclue> lignes) {
        LigneConclue premiere = lignes.getFirst();
        VolumeAlcoolPur volume = lignes.stream().map(LigneConclue::volumeAp)
                .reduce(VolumeAlcoolPur.ZERO, VolumeAlcoolPur::plus);
        BigDecimal pondere = lignes.stream().map(l -> l.prix().multiply(l.volumeAp().litres()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal moyen = volume.estNul() ? BigDecimal.ZERO
                : pondere.divide(volume.litres(), 2, RoundingMode.HALF_UP);
        String libelle = premiere.millesime() != null ? "Millésime " + premiere.millesime()
                : premiere.denomination() + ", compte " + premiere.compte();
        return new Mercuriale(libelle, premiere.millesime(), premiere.denomination(),
                premiere.millesime() != null ? null : premiere.compte(),
                (int) lignes.stream().mapToLong(LigneConclue::contrat).distinct().count(), volume,
                lignes.stream().map(LigneConclue::prix).min(Comparator.naturalOrder()).orElseThrow(), moyen,
                lignes.stream().map(LigneConclue::prix).max(Comparator.naturalOrder()).orElseThrow());
    }
}
