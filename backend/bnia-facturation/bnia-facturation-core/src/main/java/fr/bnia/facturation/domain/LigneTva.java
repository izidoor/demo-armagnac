package fr.bnia.facturation.domain;

import fr.bnia.facturation.api.CategorieTva;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Une ligne du récapitulatif de TVA (RG-FAC-05, EN 16931 BR-CO-17) : les lignes article d'une même catégorie et
 * d'un même taux.
 *
 * @param base somme des HT des lignes
 * @param tva  base × taux, au centime, demi supérieur
 */
public record LigneTva(CategorieTva categorie, BigDecimal base, BigDecimal tva) {

    /** Le HT d'une ligne article et sa catégorie de TVA. */
    public record Montant(CategorieTva categorie, BigDecimal ht) {
    }

    /**
     * RG-FAC-05 : le récapitulatif d'une pièce, les montants regroupés par catégorie et taux, dans l'ordre de leur
     * première apparition ; TVA = base × taux, au centime.
     */
    public static List<LigneTva> recapituler(List<Montant> montants) {
        Map<String, LigneTva> parCle = new LinkedHashMap<>();
        for (Montant m : montants) {
            parCle.merge(cle(m.categorie()), new LigneTva(m.categorie(), m.ht(), null),
                    (a, b) -> new LigneTva(a.categorie(), a.base().add(b.base()), null));
        }
        return parCle.values().stream().map(l -> new LigneTva(l.categorie(), Montants.centimes(l.base()),
                Montants.pourcentage(l.base(), l.categorie().tauxApplique()))).toList();
    }

    /**
     * RG-FAC-30 : le récapitulatif de plusieurs pièces, somme de leurs récapitulatifs par catégorie et taux. La TVA
     * de chaque pièce est arrondie sur sa base à elle, comme sur la pièce : la somme retrouve leurs totaux de TVA.
     */
    public static List<LigneTva> cumuler(List<List<LigneTva>> recapitulatifs) {
        Map<String, LigneTva> parCle = new LinkedHashMap<>();
        for (List<LigneTva> recapitulatif : recapitulatifs) {
            for (LigneTva l : recapitulatif) {
                parCle.merge(cle(l.categorie()), l, (a, b) -> new LigneTva(a.categorie(), a.base().add(b.base()),
                        a.tva().add(b.tva())));
            }
        }
        return List.copyOf(parCle.values());
    }

    private static String cle(CategorieTva categorie) {
        return categorie.code() + "/" + categorie.taux();
    }
}
