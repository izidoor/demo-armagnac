package fr.bnia.facturation.application;

import static fr.bnia.facturation.application.Monde.ETRANGER;
import static fr.bnia.facturation.application.Monde.GESTIONNAIRE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import fr.bnia.commun.domain.AccesRefuse;
import fr.bnia.commun.domain.ConflitUnicite;
import fr.bnia.commun.domain.RegleEnfreinte;
import fr.bnia.facturation.api.ArticleRef;
import fr.bnia.facturation.api.ClientRef;
import fr.bnia.facturation.api.ConditionPaiement;
import fr.bnia.facturation.api.DemandeFacture;
import fr.bnia.facturation.api.LigneDemandee;
import fr.bnia.facturation.api.LigneDemandee.ArticleDemande;
import fr.bnia.facturation.api.LigneDemandee.GroupeDemande;
import fr.bnia.facturation.api.LigneDemandee.TexteDemande;
import fr.bnia.facturation.api.Origine;
import fr.bnia.facturation.domain.EtatPiece;
import fr.bnia.facturation.domain.EvenementJournal;
import fr.bnia.facturation.domain.Exemples;
import fr.bnia.facturation.domain.Facture;
import fr.bnia.facturation.domain.LigneFacture.LigneArticle;
import fr.bnia.facturation.domain.LigneFacture.LigneGroupe;
import fr.bnia.facturation.domain.SensPiece;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DemanderFacturesTest {

    private final Monde monde = new Monde();
    private final DemanderFactures cas = monde.demander();

    static ArticleDemande article(String code, String quantite) {
        return new ArticleDemande(new ArticleRef("declarations", code), new BigDecimal(quantite), null, null);
    }

    static DemandeFacture demande(String dsm, LigneDemandee... lignes) {
        return new DemandeFacture(Exemples.origine(dsm), Exemples.CLIENT, LocalDate.of(2026, 7, 31),
                "DSM de juillet 2026", ConditionPaiement.TRENTE_JOURS_FIN_DE_MOIS, null, null, "Note de pied",
                List.of(lignes));
    }

    static DemandeFacture demandeDeLExemple(String dsm) {
        return demande(dsm, new GroupeDemande("Commerce de l'Armagnac", List.of(
                article("CVO-SORTIES_FRANCE-S", "8.3911"), article("CVO-SORTIES_FRANCE-E", "8.3911"),
                article("CVO-SORTIES_EXPORT-S", "22.5415"), article("CVO-SORTIES_EXPORT-E", "22.5415"))));
    }

    @Test
    @DisplayName("RG-FAC-01, 20 : un brouillon de facture par demande, journalisé « demandée » côté vendeur")
    void rg_fac_01_brouillons() {
        List<Long> ids = cas.demander(GESTIONNAIRE, List.of(demandeDeLExemple("7"), demandeDeLExemple("8")));

        assertThat(ids).hasSize(2);
        Facture brouillon = monde.factures.parId(ids.getFirst()).orElseThrow();
        assertThat(brouillon.sens()).isEqualTo(SensPiece.FACTURE);
        assertThat(brouillon.etat()).isEqualTo(EtatPiece.BROUILLON);
        assertThat(brouillon.notePied()).isEqualTo("Note de pied");
        assertThat(brouillon.totaux().ttc()).isEqualByComparingTo("2152.90");
        assertThat(monde.journal.deLaPiece(ids.getFirst())).singleElement().satisfies(e -> {
            assertThat(e.evenement()).isEqualTo(EvenementJournal.DEMANDEE);
            assertThat(e.nomActeur()).isEqualTo("Utilisateur 1");
        });
    }

    @Test
    @DisplayName("RG-FAC-01 : un lot réussit ou échoue tout entier ; l'erreur nomme la demande en cause")
    void rg_fac_01_tout_ou_rien() {
        DemandeFacture fautive = new DemandeFacture(
                new Origine("declarations", "DSM", "Cotisation", "9", "DSM août 2026"),
                Exemples.CLIENT, LocalDate.of(2026, 8, 31), null, ConditionPaiement.TRENTE_JOURS_FIN_DE_MOIS, null,
                null, null, List.of(article("CVO-INCONNU-S", "1")));

        assertThatThrownBy(() -> cas.demander(GESTIONNAIRE, List.of(demandeDeLExemple("7"), fautive)))
                .isInstanceOfSatisfying(RegleEnfreinte.class, e -> {
                    assertThat(e.regle()).contains("RG-FAC-03");
                    assertThat(e).hasMessageStartingWith("DSM août 2026 : Article inconnu");
                });
    }

    @Test
    @DisplayName("RG-FAC-02 : une origine n'a qu'une facture vivante, y compris dans un même lot")
    void rg_fac_02_une_facture_vivante() {
        cas.demander(GESTIONNAIRE, List.of(demandeDeLExemple("7")));

        assertThatThrownBy(() -> cas.demander(GESTIONNAIRE, List.of(demandeDeLExemple("7"))))
                .isInstanceOfSatisfying(ConflitUnicite.class, e -> {
                    assertThat(e.regle()).contains("RG-FAC-02");
                    assertThat(e).hasMessage("DSM juillet 2026 a déjà une facture.");
                });
        assertThatThrownBy(() -> cas.demander(GESTIONNAIRE, List.of(demandeDeLExemple("8"), demandeDeLExemple("8"))))
                .isInstanceOf(ConflitUnicite.class);
    }

    @Test
    @DisplayName("RG-FAC-03 : l'article recopié de son catalogue, le prix de la demande s'il est donné ; inconnu ou inactif refusé")
    void rg_fac_03_articles() {
        long id = cas.demander(GESTIONNAIRE, List.of(demande("7", new ArticleDemande(
                new ArticleRef("declarations", "CVO-SORTIES_FRANCE-S"), BigDecimal.ONE, new BigDecimal("50"), null),
                new TexteDemande("Volumes de juillet")))).getFirst();

        LigneArticle ligne = monde.factures.parId(id).orElseThrow().lignesArticle().getFirst();
        assertThat(ligne.designation()).isEqualTo("Cotisation CVO-SORTIES_FRANCE-S");
        assertThat(ligne.unite()).isEqualTo("hlAP");
        assertThat(ligne.tva()).isEqualTo(Exemples.TVA_20);
        assertThat(ligne.prixUnitaire()).isEqualByComparingTo("50");

        monde.ajouterArticle("CVO-ANCIEN-S", "40", false);
        assertThatThrownBy(() -> cas.demander(GESTIONNAIRE, List.of(demande("8", article("CVO-ANCIEN-S", "1")))))
                .isInstanceOfSatisfying(RegleEnfreinte.class, e -> {
                    assertThat(e.regle()).contains("RG-FAC-03");
                    assertThat(e).hasMessageContaining("inactif");
                });
        assertThatThrownBy(() -> cas.demander(GESTIONNAIRE, List.of(demande("8", new ArticleDemande(
                new ArticleRef("boutique", "PLV"), BigDecimal.ONE, null, null)))))
                .hasMessage("Aucun catalogue d'articles pour la source « boutique ».");
    }

    @Test
    @DisplayName("RG-FAC-04 : un groupe ne contient pas d'autre groupe")
    void rg_fac_04_groupe_imbrique() {
        GroupeDemande interne = new GroupeDemande("Interne", List.of(article("CVO-SORTIES_FRANCE-S", "1")));

        assertThatThrownBy(() -> cas.demander(GESTIONNAIRE, List.of(demande("7",
                new GroupeDemande("Externe", List.of(interne))))))
                .isInstanceOfSatisfying(RegleEnfreinte.class, e -> assertThat(e.regle()).contains("RG-FAC-04"));
        long id = cas.demander(GESTIONNAIRE, List.of(demandeDeLExemple("7"))).getFirst();
        assertThat(monde.factures.parId(id).orElseThrow().lignes().getFirst()).isInstanceOf(LigneGroupe.class);
    }

    @Test
    @DisplayName("RG-FAC-11 : l'acheteur d'un client est lu chez l'hôte ; un client inconnu est refusé")
    void rg_fac_11_acheteur_du_client() {
        long id = cas.demander(GESTIONNAIRE, List.of(demandeDeLExemple("7"))).getFirst();
        assertThat(monde.factures.parId(id).orElseThrow().acheteur()).isEqualTo(Exemples.acheteur());

        DemandeFacture inconnu = new DemandeFacture(Exemples.origine("8"), ClientRef.operateur(99, "X"),
                LocalDate.of(2026, 7, 31), null, ConditionPaiement.TRENTE_JOURS_FIN_DE_MOIS, null, null, null, List.of());
        assertThatThrownBy(() -> cas.demander(GESTIONNAIRE, List.of(inconnu)))
                .isInstanceOfSatisfying(RegleEnfreinte.class, e -> assertThat(e.regle()).contains("RG-FAC-11"));
    }

    @Test
    @DisplayName("RG-FAC-26 : demander une facture est réservé à qui gère la facturation")
    void rg_fac_26_refus() {
        assertThatThrownBy(() -> cas.demander(ETRANGER, List.of(demandeDeLExemple("7"))))
                .isInstanceOfSatisfying(AccesRefuse.class, e -> assertThat(e.regle()).contains("RG-FAC-26"));
        assertThatThrownBy(() -> cas.demander(Monde.LECTEUR, List.of(demandeDeLExemple("7"))))
                .isInstanceOf(AccesRefuse.class);
        assertThat(monde.factures.parId).isEmpty();
    }
}
