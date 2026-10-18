package fr.bnia.facturation.domain;

import fr.bnia.facturation.api.ArticleRef;
import fr.bnia.facturation.api.CategorieTva;
import fr.bnia.facturation.api.ClientRef;
import fr.bnia.facturation.api.ConditionPaiement;
import fr.bnia.facturation.api.NatureArticle;
import fr.bnia.facturation.api.Origine;
import fr.bnia.facturation.domain.LigneFacture.LigneArticle;
import fr.bnia.facturation.domain.LigneFacture.LigneGroupe;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/** La facture d'exemple ({@code FACTURE-CVO-Armagnac_2026000700.pdf}) et ses acteurs, pour les tests. */
public final class Exemples {

    public static final CategorieTva TVA_20 = CategorieTva.normale(new BigDecimal("20"));
    public static final CategorieTva EXONEREE = CategorieTva.exoneree("Part non assujettie de la cotisation");
    public static final ClientRef CLIENT = ClientRef.operateur(42, "N320001");

    private Exemples() {
    }

    public static Origine origine(String identifiant) {
        return new Origine("declarations", "DSM", "Cotisation interprofessionnelle Armagnac", identifiant,
                "DSM juillet 2026");
    }

    public static Acheteur acheteur() {
        return new Acheteur(CLIENT, CategorieAcheteur.PROFESSIONNEL, "Armagnacs de Gascogne", null, "77699995500029",
                "FR55776999955", new Adresse("12 rue des Chais", null, "32100", "Condom", "France", "FR"), null,
                "comptabilite@gascogne.example");
    }

    public static Vendeur vendeur() {
        return new Vendeur("Bureau National Interprofessionnel de l'Armagnac",
                new Adresse("11 place de la Liberté", null, "32800", "Eauze", "France", "FR"), null, null,
                "77699995500029", "FR55776999955", "FR7610278022000002025240143", "CMCIFR2A", "05 62 08 11 00",
                "compta@armagnac.fr", false, null, null);
    }

    public static LigneArticle article(String code, String quantite, String prix, CategorieTva tva) {
        return new LigneArticle(new ArticleRef("declarations", code), "Cotisation " + code, "hlAP",
                NatureArticle.SERVICE, null, new BigDecimal(quantite), new BigDecimal(prix), tva);
    }

    /** Sorties France 8,3911 hlAP et export 22,5415 hlAP, à 60 €/hlAP dont 20 % exonérés. */
    public static List<LigneFacture> lignesDeLExemple() {
        return List.of(new LigneGroupe("Commerce de l'Armagnac", List.of(
                article("CVO-SORTIES_FRANCE-S", "8.3911", "48", TVA_20),
                article("CVO-SORTIES_FRANCE-E", "8.3911", "12", EXONEREE),
                article("CVO-SORTIES_EXPORT-S", "22.5415", "48", TVA_20),
                article("CVO-SORTIES_EXPORT-E", "22.5415", "12", EXONEREE))));
    }

    public static Facture brouillon(List<LigneFacture> lignes) {
        return Facture.demandee(origine("7"), acheteur(), LocalDate.of(2026, 7, 31), "DSM de juillet 2026",
                ConditionPaiement.TRENTE_JOURS_FIN_DE_MOIS, null, null, null, lignes, new Auteur(1, "Anne Lamarque"),
                Instant.parse("2026-10-07T08:00:00Z"));
    }
}
