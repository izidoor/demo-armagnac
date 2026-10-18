package fr.bnia.facturation.application;

import static fr.bnia.facturation.application.DemanderFacturesTest.demandeDeLExemple;
import static fr.bnia.facturation.application.Monde.CLIENT;
import static fr.bnia.facturation.application.Monde.ETRANGER;
import static fr.bnia.facturation.application.Monde.GESTIONNAIRE;
import static fr.bnia.facturation.application.Monde.LECTEUR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.groups.Tuple.tuple;

import fr.bnia.commun.domain.AccesRefuse;
import fr.bnia.commun.domain.RegleEnfreinte;
import fr.bnia.facturation.api.Article;
import fr.bnia.facturation.api.ArticleRef;
import fr.bnia.facturation.api.LigneVendue;
import fr.bnia.facturation.api.NatureArticle;
import fr.bnia.facturation.domain.Exemples;
import fr.bnia.facturation.domain.LigneTva;
import fr.bnia.facturation.domain.Totaux;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Ventes, articles et lecture publiée (lot 5) : RG-FAC-26, 29, 30, 31. */
class ConsulterVentesTest {

    private static final LocalDate DEBUT = LocalDate.of(2026, 10, 1);
    private static final LocalDate FIN = LocalDate.of(2026, 12, 31);

    private final Monde monde = new Monde();
    private long emise;

    /** Deux demandes de l'exemple : l'une émise le 07/10/2026 (horloge du monde), l'autre laissée en brouillon. */
    @BeforeEach
    void pieces() {
        monde.vendeurs.vendeur = Exemples.vendeur();
        List<Long> ids = monde.demander().demander(GESTIONNAIRE,
                List.of(demandeDeLExemple("7"), demandeDeLExemple("8")));
        emise = ids.getFirst();
        monde.emettre().emettre(GESTIONNAIRE, emise);
    }

    @Test
    @DisplayName("RG-FAC-30 : les lignes article des seules pièces émises de la période, récapitulatif et totaux de la pièce")
    void rg_fac_30_ventes_de_la_periode() {
        Ventes ventes = monde.consulterVentes().ventes(LECTEUR, DEBUT, FIN);

        assertThat(ventes.lignes()).hasSize(4).allSatisfy(l -> {
            assertThat(l.piece()).isEqualTo(emise);
            assertThat(l.numero()).isEqualTo("2026000001");
            assertThat(l.avoir()).isFalse();
            assertThat(l.origine().nature()).isEqualTo("DSM");
        });
        assertThat(ventes.recapitulatifTva()).extracting(LigneTva::base, LigneTva::tva).containsExactly(
                tuple(new BigDecimal("1484.76"), new BigDecimal("296.95")),
                tuple(new BigDecimal("371.19"), new BigDecimal("0.00")));
        assertThat(ventes.totaux()).isEqualTo(new Totaux(new BigDecimal("1855.95"), new BigDecimal("296.95"),
                new BigDecimal("2152.90")));
        assertThat(ventes.parArticle()).extracting(Ventes.ParArticle::code, Ventes.ParArticle::quantite)
                .containsExactly(tuple("CVO-SORTIES_EXPORT-E", new BigDecimal("22.5415")),
                        tuple("CVO-SORTIES_EXPORT-S", new BigDecimal("22.5415")),
                        tuple("CVO-SORTIES_FRANCE-E", new BigDecimal("8.3911")),
                        tuple("CVO-SORTIES_FRANCE-S", new BigDecimal("8.3911")));
        assertThat(monde.consulterVentes().ventes(LECTEUR, LocalDate.of(2026, 10, 8), FIN).lignes()).isEmpty();
    }

    @Test
    @DisplayName("RG-FAC-30 : une ligne d'avoir compte en négatif, en quantité, en HT et dans le récapitulatif")
    void rg_fac_30_avoir_en_negatif() {
        List<LigneVendue> facture = monde.consulterVentes().ventes(LECTEUR, DEBUT, FIN).lignes();
        LigneVendue l = facture.getFirst();
        LigneVendue avoir = new LigneVendue(99, "2026000002", true, l.dateEmission(), l.acheteur(), l.client(), null,
                l.article(), l.designation(), l.unite(), l.nature(), null, l.tva(), l.quantite().negate(),
                l.prixUnitaire(), l.montantHt().negate());

        Ventes ventes = Ventes.de(DEBUT, FIN, List.of(l, avoir));

        assertThat(ventes.parArticle()).singleElement().satisfies(a -> {
            assertThat(a.quantite()).isZero();
            assertThat(a.montantHt()).isZero();
        });
        assertThat(ventes.totaux().ttc()).isZero();
    }

    @Test
    @DisplayName("RG-FAC-30 : la période va d'une date à une date postérieure ou égale")
    void rg_fac_30_periode() {
        assertThatThrownBy(() -> monde.consulterVentes().ventes(LECTEUR, FIN, DEBUT))
                .isInstanceOfSatisfying(RegleEnfreinte.class, e -> assertThat(e.regle()).contains("RG-FAC-30"));
        assertThat(monde.consulterVentes().pdf(LECTEUR, DEBUT, FIN)).asString().isEqualTo("%PDF-1.7 ventes 4");
    }

    @Test
    @DisplayName("RG-FAC-26 : les ventes, leur PDF et les articles sont réservés au personnel du BNIA")
    void rg_fac_26_refus() {
        for (var acteur : List.of(CLIENT, ETRANGER)) {
            assertThatThrownBy(() -> monde.consulterVentes().ventes(acteur, DEBUT, FIN))
                    .isInstanceOf(AccesRefuse.class);
            assertThatThrownBy(() -> monde.consulterVentes().pdf(acteur, DEBUT, FIN)).isInstanceOf(AccesRefuse.class);
            assertThatThrownBy(() -> monde.consulterArticles().articles(acteur)).isInstanceOf(AccesRefuse.class);
        }
    }

    @Test
    @DisplayName("RG-FAC-29 : tous les articles de tous les catalogues, inactifs compris, par source et code")
    void rg_fac_29_articles() {
        monde.ajouterArticle("CVO-ANCIENNE-S", "40.0000", false);

        List<Article> articles = monde.consulterArticles().articles(LECTEUR);

        assertThat(articles).extracting(Article::code, Article::actif).containsExactly(
                tuple("CVO-ANCIENNE-S", false), tuple("CVO-SORTIES_EXPORT-E", true),
                tuple("CVO-SORTIES_EXPORT-S", true), tuple("CVO-SORTIES_FRANCE-E", true),
                tuple("CVO-SORTIES_FRANCE-S", true));
        assertThat(articles).allSatisfy(a -> {
            assertThat(a.lienAdministration()).isEqualTo("/bo/parametrage/taux-cvo");
            assertThat(a.nature()).isEqualTo(NatureArticle.SERVICE);
        });
    }

    @Test
    @DisplayName("RG-FAC-31 : la lecture publiée rend les lignes vendues de la période, filtrées par nature d'origine")
    void rg_fac_31_lecture_publiee() {
        assertThat(monde.lecture().ventes(DEBUT, FIN, Optional.of("DSM"))).hasSize(4)
                .extracting(LigneVendue::article).contains(new ArticleRef("declarations", "CVO-SORTIES_FRANCE-S"));
        assertThat(monde.lecture().ventes(DEBUT, FIN, Optional.of(" "))).hasSize(4);
        assertThat(monde.lecture().ventes(DEBUT, FIN, Optional.of("FIN_DE_TRAVAUX"))).isEmpty();
    }
}
