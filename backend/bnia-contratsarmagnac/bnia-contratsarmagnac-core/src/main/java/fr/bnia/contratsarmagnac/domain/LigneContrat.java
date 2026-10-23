package fr.bnia.contratsarmagnac.domain;

import fr.bnia.commun.domain.Appellation;
import fr.bnia.commun.domain.Millesime;
import fr.bnia.commun.domain.RegleEnfreinte;
import fr.bnia.commun.domain.VolumeAlcoolPur;
import fr.bnia.declarations.domain.partage.CompteDAgeDsm;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

/**
 * Une ligne du contrat (spec 06 §3.3) : de l'Armagnac en fût, millésimé ou en assemblage, son volume, son degré et son
 * prix ; volume d'alcool pur, prix au litre de volume et montant s'en déduisent (RG-CA-04).
 *
 * @param millesime    présent si et seulement si la dénomination est absente (RG-CA-03)
 * @param denomination dénomination d'un assemblage, sans espaces superflus
 * @param compte       calculé pour un millésime, à la date de la version ; saisi pour une dénomination
 * @param volume       en hectolitres, 2 décimales
 * @param degre        en % vol., 2 décimales
 * @param prix         en € HT par hectolitre d'alcool pur, 2 décimales
 */
public record LigneContrat(Appellation appellation, Millesime millesime, String denomination, CompteDAgeDsm compte,
        BigDecimal volume, BigDecimal degre, BigDecimal prix) {

    public static final int LONGUEUR_DENOMINATION = 100;
    private static final BigDecimal CENT = BigDecimal.valueOf(100);
    private static final BigDecimal DIX_MILLE = BigDecimal.valueOf(10_000);

    /** RG-CA-03 : contrôle une ligne, déjà normalisée (voir {@link #saisie}). */
    public LigneContrat {
        if (appellation == null) {
            throw new RegleEnfreinte("RG-CA-03", "Chaque ligne porte une appellation.");
        }
        if ((millesime == null) == (denomination == null)) {
            throw new RegleEnfreinte("RG-CA-03", "Une ligne porte un millésime ou une dénomination, pas les deux.");
        }
        if (compte == null) {
            throw new RegleEnfreinte("RG-CA-03", "Le compte d'âge d'un assemblage est à saisir.");
        }
        volume = positif(volume, "volume", null);
        degre = positif(degre, "degré", CENT);
        prix = positif(prix, "prix", null);
    }

    /**
     * RG-CA-03, 04 : une ligne saisie ; le compte d'un millésime est calculé à {@code date} (annexe B : 2009 donne 16
     * du 1er avril 2026 au 31 mars 2027), celui d'une dénomination est saisi.
     */
    public static LigneContrat saisie(Appellation appellation, Integer millesime, String denomination, String compte,
            BigDecimal volume, BigDecimal degre, BigDecimal prix, LocalDate date) {
        String texte = denomination == null ? "" : denomination.strip().replaceAll("\\s+", " ");
        if (millesime != null && !texte.isEmpty()) {
            throw new RegleEnfreinte("RG-CA-03", "Une ligne porte un millésime ou une dénomination, pas les deux.");
        }
        if (millesime == null && texte.isEmpty()) {
            throw new RegleEnfreinte("RG-CA-03", "Chaque ligne porte un millésime ou une dénomination.");
        }
        if (texte.length() > LONGUEUR_DENOMINATION) {
            throw new RegleEnfreinte("RG-CA-03", "La dénomination « " + texte.substring(0, 20) + "… » dépasse "
                    + LONGUEUR_DENOMINATION + " caractères.");
        }
        if (millesime != null) {
            Millesime m = millesime(millesime, date);
            return new LigneContrat(appellation, m, null, CompteDAgeDsm.calcule(m, date), volume, degre, prix);
        }
        if (compte == null || compte.isBlank()) {
            throw new RegleEnfreinte("RG-CA-03", "Le compte d'âge de l'assemblage « " + texte + " » est à saisir.");
        }
        CompteDAgeDsm c;
        try {
            c = CompteDAgeDsm.de(compte);
        } catch (RegleEnfreinte invalide) {
            throw new RegleEnfreinte("RG-CA-03", invalide.getMessage());
        }
        return new LigneContrat(appellation, null, texte, c, volume, degre, prix);
    }

    private static Millesime millesime(int annee, LocalDate date) {
        if (annee > date.getYear()) {
            throw new RegleEnfreinte("RG-CA-03", "Le millésime " + annee + " est dans le futur.");
        }
        try {
            return new Millesime(annee);
        } catch (RegleEnfreinte invalide) {
            throw new RegleEnfreinte("RG-CA-03", invalide.getMessage());
        }
    }

    private static BigDecimal positif(BigDecimal valeur, String champ, BigDecimal maximum) {
        if (valeur == null || valeur.signum() <= 0 || maximum != null && valeur.compareTo(maximum) > 0) {
            throw new RegleEnfreinte("RG-CA-03", "Le " + champ + " d'une ligne est strictement positif"
                    + (maximum != null ? ", au plus " + maximum : "") + ".");
        }
        if (valeur.stripTrailingZeros().scale() > 2) {
            throw new RegleEnfreinte("RG-CA-03", "Le " + champ + " d'une ligne a 2 décimales au plus.");
        }
        return valeur.setScale(2, RoundingMode.UNNECESSARY);
    }

    /** RG-CA-04 : volume (hl) × degré, en litres d'alcool pur ; 30 hl à 47 % donnent 1 410 L AP. */
    public VolumeAlcoolPur volumeAp() {
        return VolumeAlcoolPur.litres(volume.multiply(degre).setScale(2, RoundingMode.HALF_UP));
    }

    /** RG-CA-04 : prix × degré / 10 000, en € HT par litre de volume ; 1 750 €/hl AP à 47 % donnent 8,23 €. */
    public BigDecimal prixLitreVolume() {
        return prix.multiply(degre).divide(DIX_MILLE, 2, RoundingMode.HALF_UP);
    }

    /** RG-CA-04 : volume AP (hl) × prix, en € HT ; 14,10 hl AP à 1 750 € donnent 24 675 €. */
    public BigDecimal montantHt() {
        return volume.multiply(degre).multiply(prix).divide(CENT, 2, RoundingMode.HALF_UP);
    }

    /** RG-CA-05 : {@code MB} et le millésime, ou {@code NB}, la dénomination et le compte. */
    public Grain grain() {
        return millesime != null ? Grain.millesime(millesime.annee()) : Grain.assemblage(denomination, compte);
    }

    /**
     * RG-CA-05, H-CA-11 : le grain à une date, pour une ligne d'un contrat conclu à {@code dateContrat} : le compte d'un
     * assemblage prend +1 à chaque 1er avril franchi depuis, {@code 20+} au plus ; un millésime ne change pas.
     */
    public Grain grainAu(LocalDate dateContrat, LocalDate date) {
        if (millesime != null) {
            return grain();
        }
        CompteDAgeDsm vieilli = compte;
        for (int annee = dateContrat.getYear(); annee <= date.getYear(); annee++) {
            LocalDate avril = LocalDate.of(annee, 4, 1);
            if (avril.isAfter(dateContrat) && !avril.isAfter(date)) {
                vieilli = vieilli.suivant();
            }
        }
        return Grain.assemblage(denomination, vieilli);
    }

    /** RG-CA-04 : le volume d'alcool pur d'un volume retiré de cette ligne, à son degré. */
    public VolumeAlcoolPur volumeAp(BigDecimal volumeRetire) {
        return VolumeAlcoolPur.litres(volumeRetire.multiply(degre).setScale(2, RoundingMode.HALF_UP));
    }

    /** Pour les messages : « millésime 2015 », « Hors d'âge, compte 20+ ». */
    public String libelle() {
        return millesime != null ? "millésime " + millesime : denomination + ", compte " + compte;
    }
}
