package fr.bnia.facturation.application;

import fr.bnia.facturation.api.LigneVendue;
import fr.bnia.facturation.domain.LigneTva;
import fr.bnia.facturation.domain.Totaux;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Les ventes consolidées d'une période (RG-FAC-30) : les lignes vendues, le récapitulatif de TVA (somme des
 * récapitulatifs des pièces) et les totaux.
 */
public record Ventes(LocalDate du, LocalDate au, List<LigneVendue> lignes, List<LigneTva> recapitulatifTva,
        Totaux totaux) {

    /** RG-FAC-05, 30 : le récapitulatif pièce par pièce, puis sa somme par catégorie et taux. */
    static Ventes de(LocalDate du, LocalDate au, List<LigneVendue> lignes) {
        Map<Long, List<LigneTva.Montant>> parPiece = new LinkedHashMap<>();
        for (LigneVendue l : lignes) {
            parPiece.computeIfAbsent(l.piece(), p -> new ArrayList<>())
                    .add(new LigneTva.Montant(l.tva(), l.montantHt()));
        }
        List<LigneTva> recapitulatif = LigneTva.cumuler(parPiece.values().stream().map(LigneTva::recapituler).toList());
        BigDecimal ht = recapitulatif.stream().map(LigneTva::base).reduce(BigDecimal.ZERO.setScale(2), BigDecimal::add);
        BigDecimal tva = recapitulatif.stream().map(LigneTva::tva).reduce(BigDecimal.ZERO.setScale(2), BigDecimal::add);
        return new Ventes(du, au, lignes, recapitulatif, new Totaux(ht, tva, ht.add(tva)));
    }

    /** Une ligne de la synthèse par article : quantité (dans son unité) et HT. */
    public record ParArticle(String source, String code, String designation, String unite, BigDecimal quantite,
            BigDecimal montantHt) {
    }

    /** Synthèse par article, dans l'ordre de source puis de code ; les quantités ne se somment que par article. */
    public List<ParArticle> parArticle() {
        Map<String, ParArticle> parCle = new TreeMap<>();
        for (LigneVendue l : lignes) {
            parCle.merge(l.article().source() + "\u001f" + l.article().code(),
                    new ParArticle(l.article().source(), l.article().code(), l.designation(), l.unite(), l.quantite(),
                            l.montantHt()),
                    (a, b) -> new ParArticle(a.source(), a.code(), a.designation(), a.unite(),
                            a.quantite().add(b.quantite()), a.montantHt().add(b.montantHt())));
        }
        return List.copyOf(parCle.values());
    }
}
