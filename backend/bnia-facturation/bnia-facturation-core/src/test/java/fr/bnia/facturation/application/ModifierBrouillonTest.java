package fr.bnia.facturation.application;

import static fr.bnia.facturation.application.DemanderFacturesTest.demandeDeLExemple;
import static fr.bnia.facturation.application.Monde.GESTIONNAIRE;
import static fr.bnia.facturation.application.Monde.LECTEUR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import fr.bnia.commun.domain.AccesRefuse;
import fr.bnia.commun.domain.ModificationConcurrente;
import fr.bnia.commun.domain.RegleEnfreinte;
import fr.bnia.facturation.api.Article;
import fr.bnia.facturation.api.ArticleRef;
import fr.bnia.facturation.api.ConditionPaiement;
import fr.bnia.facturation.application.ModifierBrouillon.LigneSaisie;
import fr.bnia.facturation.application.ModifierBrouillon.LigneSaisie.ArticleSaisi;
import fr.bnia.facturation.application.ModifierBrouillon.LigneSaisie.GroupeSaisi;
import fr.bnia.facturation.application.ModifierBrouillon.LigneSaisie.TexteSaisi;
import fr.bnia.facturation.application.ModifierBrouillon.Saisie;
import fr.bnia.facturation.domain.EvenementJournal;
import fr.bnia.facturation.domain.Exemples;
import fr.bnia.facturation.domain.Facture;
import fr.bnia.facturation.domain.LigneFacture;
import fr.bnia.facturation.domain.LigneFacture.LigneArticle;
import fr.bnia.facturation.domain.LigneFacture.LigneGroupe;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ModifierBrouillonTest {

    private final Monde monde = new Monde();
    private final ModifierBrouillon cas = monde.modifier();
    private long brouillon;

    @BeforeEach
    void brouillonDeLExemple() {
        brouillon = monde.demander().demander(GESTIONNAIRE, List.of(demandeDeLExemple("7"))).getFirst();
    }

    /** Les lignes du brouillon telles qu'enregistrées, en saisie : rien de changé. */
    private List<LigneSaisie> telles() {
        return monde.factures.parId(brouillon).orElseThrow().lignes().stream().map(ModifierBrouillonTest::saisie)
                .toList();
    }

    private static LigneSaisie saisie(LigneFacture ligne) {
        return switch (ligne) {
            case LigneArticle a -> new ArticleSaisi(a.numero(), a.article(), a.designation(), a.unite(), a.quantite(),
                    a.prixUnitaire());
            case LigneFacture.LigneInformative i -> new TexteSaisi(i.numero(), i.texte());
            case LigneGroupe g -> new GroupeSaisi(g.numero(), g.titre(),
                    g.lignes().stream().map(ModifierBrouillonTest::saisie).toList());
        };
    }

    private Saisie saisie(String periode, List<LigneSaisie> lignes) {
        return new Saisie(periode, ConditionPaiement.TRENTE_JOURS_FIN_DE_MOIS, null, null, "Note de pied", lignes,
                monde.factures.parId(brouillon).orElseThrow().version());
    }

    @Test
    @DisplayName("RG-FAC-17 : période corrigée, texte ajouté en tête, article d'un catalogue ajouté ; journal « modifiée »")
    void rg_fac_17_modifier() {
        List<LigneSaisie> lignes = new ArrayList<>(telles());
        lignes.addFirst(new TexteSaisi(null, "Régularisation suite au contrôle du 02/10"));
        lignes.add(new ArticleSaisi(null, new ArticleRef("declarations", "CVO-SORTIES_FRANCE-S"), null, null,
                new BigDecimal("1"), null));

        VuePiece vue = cas.modifier(GESTIONNAIRE, brouillon, saisie("DSM juillet 2026, rectifiée", lignes));

        Facture piece = vue.piece();
        assertThat(piece.libellePeriode()).isEqualTo("DSM juillet 2026, rectifiée");
        assertThat(piece.lignes().getFirst().designation()).isEqualTo("Régularisation suite au contrôle du 02/10");
        LigneArticle ajoutee = (LigneArticle) piece.lignes().getLast();
        // RG-FAC-03 : l'article ajouté est résolu dans son catalogue, prix et TVA compris.
        assertThat(ajoutee.prixUnitaire()).isEqualByComparingTo("48");
        assertThat(ajoutee.tva()).isEqualTo(Exemples.TVA_20);
        assertThat(ajoutee.recue()).isFalse();
        assertThat(piece.totaux().ht()).isEqualByComparingTo("1903.95");
        assertThat(monde.journal.deLaPiece(brouillon).getFirst().evenement()).isEqualTo(EvenementJournal.MODIFIEE);
    }

    @Test
    @DisplayName("RG-FAC-17 : une ligne reçue de la source ne se retire pas")
    void rg_fac_17_ligne_recue() {
        GroupeSaisi groupe = (GroupeSaisi) telles().getFirst();
        GroupeSaisi ampute = new GroupeSaisi(groupe.numero(), groupe.titre(), groupe.lignes().subList(1, 4));

        assertThatThrownBy(() -> cas.modifier(GESTIONNAIRE, brouillon, saisie(null, List.of(ampute))))
                .isInstanceOfSatisfying(RegleEnfreinte.class, e -> assertThat(e.regle()).contains("RG-FAC-17"));
        assertThat(monde.journal.deLaPiece(brouillon)).extracting(e -> e.evenement())
                .doesNotContain(EvenementJournal.MODIFIEE);
    }

    @Test
    @DisplayName("RG-FAC-17 : une ligne inconnue du brouillon est refusée ; une saisie périmée aussi")
    void rg_fac_17_ligne_inconnue_et_version() {
        assertThatThrownBy(() -> cas.modifier(GESTIONNAIRE, brouillon, saisie(null,
                List.of(telles().getFirst(), new TexteSaisi(99, "Inconnue")))))
                .isInstanceOfSatisfying(RegleEnfreinte.class, e -> assertThat(e.getMessage()).contains("n° 99"));

        cas.modifier(GESTIONNAIRE, brouillon, saisie("Première", telles()));
        Saisie perimee = new Saisie("Seconde", ConditionPaiement.TRENTE_JOURS_FIN_DE_MOIS, null, null, null, telles(),
                0);
        assertThatThrownBy(() -> cas.modifier(GESTIONNAIRE, brouillon, perimee))
                .isInstanceOf(ModificationConcurrente.class);
    }

    @Test
    @DisplayName("RG-FAC-03 : un article inactif ne s'ajoute pas ; seuls les actifs sont proposés")
    void rg_fac_03_article_inactif() {
        monde.ajouterArticle("CVO-ANCIEN-S", "40", false);
        List<LigneSaisie> lignes = new ArrayList<>(telles());
        lignes.add(new ArticleSaisi(null, new ArticleRef("declarations", "CVO-ANCIEN-S"), null, null,
                BigDecimal.ONE, null));

        assertThatThrownBy(() -> cas.modifier(GESTIONNAIRE, brouillon, saisie(null, lignes)))
                .isInstanceOfSatisfying(RegleEnfreinte.class, e -> assertThat(e.regle()).contains("RG-FAC-03"));
        assertThat(cas.articlesProposes(GESTIONNAIRE, brouillon)).extracting(Article::code)
                .containsExactly("CVO-SORTIES_EXPORT-E", "CVO-SORTIES_EXPORT-S", "CVO-SORTIES_FRANCE-E",
                        "CVO-SORTIES_FRANCE-S");
    }

    @Test
    @DisplayName("RG-FAC-26 : modifier un brouillon est réservé à qui gère la facturation")
    void rg_fac_26_refus() {
        assertThatThrownBy(() -> cas.modifier(LECTEUR, brouillon, saisie("Rectifiée", telles())))
                .isInstanceOfSatisfying(AccesRefuse.class, e -> assertThat(e.regle()).contains("RG-FAC-26"));
        assertThatThrownBy(() -> cas.articlesProposes(LECTEUR, brouillon)).isInstanceOf(AccesRefuse.class);
    }
}
