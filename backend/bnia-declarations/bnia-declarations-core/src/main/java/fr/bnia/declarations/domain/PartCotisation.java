package fr.bnia.declarations.domain;

import fr.bnia.declarations.domain.partage.LigneCotisation;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

/**
 * Les deux parts d'une ligne de cotisation, chacune un article du catalogue de la cotisation (spec 08 §3.13,
 * H-FAC-1) : la part assujettie à la TVA ({@code CVO-<ligne>-S}) et la part non assujettie, exonérée
 * ({@code CVO-<ligne>-E}).
 */
public enum PartCotisation {

    ASSUJETTIE("S", "part assujettie"),
    NON_ASSUJETTIE("E", "part non assujettie");

    private static final BigDecimal CENT = BigDecimal.valueOf(100);

    private final String suffixe;
    private final String libelle;

    PartCotisation(String suffixe, String libelle) {
        this.suffixe = suffixe;
        this.libelle = libelle;
    }

    /** {@code CVO-SORTIES_FRANCE-S}. */
    public String code(LigneCotisation ligne) {
        return "CVO-" + ligne.name() + "-" + suffixe;
    }

    /** « Cotisation Sorties France, part assujettie ». */
    public String designation(LigneCotisation ligne) {
        return "Cotisation " + ligne.libelle() + ", " + libelle;
    }

    /**
     * Le prix unitaire par hlAP, 4 décimales (spec 08 §3.2) : montant × (1 − pourcentage) pour la part assujettie,
     * montant × pourcentage pour l'autre.
     */
    public BigDecimal prix(TauxCotisation taux) {
        BigDecimal exonere = taux.pourcentageExonere().divide(CENT);
        BigDecimal part = this == ASSUJETTIE ? BigDecimal.ONE.subtract(exonere) : exonere;
        return taux.montant().multiply(part).setScale(4, RoundingMode.HALF_UP);
    }

    /** La ligne et la part d'un code d'article ; vide si le code n'en est pas un. */
    public static Optional<Article> deCode(String code) {
        for (LigneCotisation ligne : LigneCotisation.values()) {
            for (PartCotisation part : values()) {
                if (part.code(ligne).equals(code)) {
                    return Optional.of(new Article(ligne, part));
                }
            }
        }
        return Optional.empty();
    }

    /** Un article de cotisation : une ligne, une part. */
    public record Article(LigneCotisation ligne, PartCotisation part) {

        public String code() {
            return part.code(ligne);
        }
    }
}
