package fr.bnia.contratsarmagnac.domain;

import fr.bnia.commun.domain.RegleEnfreinte;
import fr.bnia.commun.domain.VolumeAlcoolPur;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Le contenu d'une version du contrat (spec 06 §3.2) : ses lignes, les textes imprimés, la date limite de retiraison et
 * le paiement.
 *
 * @param caracteristiques     caractéristiques complémentaires, tiennent lieu du commentaire (H-CA-10)
 * @param conditionsRetiraison conditions et calendrier de retiraison
 * @param aRetirerAvantLe      affichée dans les listes, sans effet sur le stock
 */
public record ContenuContrat(List<LigneContrat> lignes, String caracteristiques, String conditionsRetiraison,
        LocalDate aRetirerAvantLe, Paiement paiement) {

    public static final int LONGUEUR_LONGUE = 4000;

    /** RG-CA-03 : au moins une ligne ; textes vides ramenés à {@code null}. */
    public ContenuContrat {
        if (lignes == null || lignes.isEmpty()) {
            throw new RegleEnfreinte("RG-CA-03", "Un contrat a au moins une ligne.");
        }
        lignes = List.copyOf(lignes);
        caracteristiques = texte(caracteristiques, "Les caractéristiques complémentaires", LONGUEUR_LONGUE);
        conditionsRetiraison = texte(conditionsRetiraison, "Les conditions de retiraison", LONGUEUR_LONGUE);
        Objects.requireNonNull(paiement, "paiement");
    }

    private static String texte(String valeur, String champ, int maximum) {
        if (valeur == null || valeur.isBlank()) {
            return null;
        }
        String texte = valeur.strip();
        if (texte.length() > maximum) {
            throw new RegleEnfreinte("RG-CA-03", champ + " dépasse " + maximum + " caractères.");
        }
        return texte;
    }

    /** RG-CA-04 : le total du contrat, en € HT. */
    public BigDecimal montantHt() {
        return lignes.stream().map(LigneContrat::montantHt).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** RG-CA-04 : le volume d'alcool pur du contrat. */
    public VolumeAlcoolPur volumeAp() {
        return lignes.stream().map(LigneContrat::volumeAp).reduce(VolumeAlcoolPur.ZERO, VolumeAlcoolPur::plus);
    }

    /** RG-CA-12 : le volume d'alcool pur demandé par grain, lignes du même grain cumulées, dans l'ordre des lignes. */
    public Map<Grain, VolumeAlcoolPur> volumeApParGrain() {
        Map<Grain, VolumeAlcoolPur> parGrain = new LinkedHashMap<>();
        lignes.forEach(l -> parGrain.merge(l.grain(), l.volumeAp(), VolumeAlcoolPur::plus));
        return parGrain;
    }
}
