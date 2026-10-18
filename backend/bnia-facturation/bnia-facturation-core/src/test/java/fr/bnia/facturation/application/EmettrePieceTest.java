package fr.bnia.facturation.application;

import static fr.bnia.facturation.application.DemanderFacturesTest.demandeDeLExemple;
import static fr.bnia.facturation.application.Monde.GESTIONNAIRE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import fr.bnia.commun.domain.AccesRefuse;
import fr.bnia.commun.domain.RegleEnfreinte;
import fr.bnia.facturation.api.FactureEmise;
import fr.bnia.facturation.domain.Acheteur;
import fr.bnia.facturation.domain.EvenementJournal;
import fr.bnia.facturation.domain.Exemples;
import fr.bnia.facturation.domain.Facture;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class EmettrePieceTest {

    private final Monde monde = new Monde();
    private final EmettrePiece cas = monde.emettre();
    private long brouillon;

    @BeforeEach
    void brouillonDeLExemple() {
        monde.vendeurs.vendeur = Exemples.vendeur();
        brouillon = monde.demander().demander(GESTIONNAIRE, List.of(demandeDeLExemple("7"))).getFirst();
    }

    @Test
    @DisplayName("RG-FAC-09, 12, 13, 15 : numéro, date, échéance, vendeur recopié, PDF déposé, journal « émise »")
    void rg_fac_09_emission() {
        Facture emise = cas.emettre(GESTIONNAIRE, brouillon);

        assertThat(emise.numero()).contains("2026000001");
        assertThat(emise.dateEmission()).contains(LocalDate.of(2026, 10, 7));
        assertThat(emise.echeance()).contains(LocalDate.of(2026, 11, 30));
        assertThat(emise.vendeur()).contains(Exemples.vendeur());
        assertThat(emise.pdf()).hasValueSatisfying(pdf -> assertThat(pdf.nom()).isEqualTo("FACTURE-2026000001.pdf"));
        assertThat(monde.journal.deLaPiece(brouillon).getFirst().evenement()).isEqualTo(EvenementJournal.EMISE);
    }

    @Test
    @DisplayName("RG-FAC-12 : factures numérotées à la suite, sans trou ; un refus ne consomme pas de numéro")
    void rg_fac_12_sequence() {
        long second = monde.demander().demander(GESTIONNAIRE, List.of(demandeDeLExemple("8"))).getFirst();
        monde.vendeurs.vendeur = null;
        assertThatThrownBy(() -> cas.emettre(GESTIONNAIRE, second)).isInstanceOf(RegleEnfreinte.class);
        monde.vendeurs.vendeur = Exemples.vendeur();

        assertThat(cas.emettre(GESTIONNAIRE, second).numero()).contains("2026000001");
        assertThat(cas.emettre(GESTIONNAIRE, brouillon).numero()).contains("2026000002");
    }

    @Test
    @DisplayName("RG-FAC-09 : sans vendeur, ou avec un acheteur incomplet, l'émission est refusée et nomme ce qui manque")
    void rg_fac_09_refus() {
        monde.vendeurs.vendeur = null;

        assertThatThrownBy(() -> cas.emettre(GESTIONNAIRE, brouillon))
                .isInstanceOfSatisfying(RegleEnfreinte.class, e -> {
                    assertThat(e.regle()).contains("RG-FAC-09");
                    assertThat(e).hasMessageContaining("vendeur non paramétré");
                });
        assertThat(monde.numeros.derniers).isEmpty();
        assertThat(monde.evenements).isEmpty();
    }

    @Test
    @DisplayName("RG-FAC-11 : l'acheteur d'un client est relu à l'émission")
    void rg_fac_11_relu() {
        Acheteur a = Exemples.acheteur();
        monde.acheteurs.put(Exemples.CLIENT, new Acheteur(a.client(), a.categorie(), a.nom(), null, null, null,
                a.adresseFacturation(), null, a.email()));

        assertThatThrownBy(() -> cas.emettre(GESTIONNAIRE, brouillon)).hasMessageContaining("SIREN");

        monde.acheteurs.put(Exemples.CLIENT, new Acheteur(a.client(), a.categorie(), "Gascogne SAS", a.siren(),
                a.siret(), a.numeroTva(), a.adresseFacturation(), null, a.email()));
        assertThat(cas.emettre(GESTIONNAIRE, brouillon).acheteur().nom()).isEqualTo("Gascogne SAS");
    }

    @Test
    @DisplayName("RG-FAC-14 : une pièce émise ne se réémet pas")
    void rg_fac_14_deja_emise() {
        cas.emettre(GESTIONNAIRE, brouillon);

        assertThatThrownBy(() -> cas.emettre(GESTIONNAIRE, brouillon))
                .isInstanceOfSatisfying(RegleEnfreinte.class, e -> assertThat(e.regle()).contains("RG-FAC-14"));
    }

    @Test
    @DisplayName("RG-FAC-25 : FactureEmise publiée avec l'origine opaque, le client, les montants et l'acteur")
    void rg_fac_25_evenement() {
        cas.emettre(GESTIONNAIRE, brouillon);

        assertThat(monde.evenements).singleElement().isInstanceOfSatisfying(FactureEmise.class, e -> {
            assertThat(e.piece()).isEqualTo(brouillon);
            assertThat(e.origine().identifiant()).isEqualTo("7");
            assertThat(e.client()).isEqualTo(Exemples.CLIENT);
            assertThat(e.numero()).isEqualTo("2026000001");
            assertThat(e.ttc()).isEqualByComparingTo("2152.90");
            assertThat(e.emisePar()).isEqualTo(GESTIONNAIRE);
        });
    }

    @Test
    @DisplayName("RG-FAC-26 : émettre est réservé à qui gère la facturation")
    void rg_fac_26_refus() {
        assertThatThrownBy(() -> cas.emettre(Monde.LECTEUR, brouillon))
                .isInstanceOfSatisfying(AccesRefuse.class, e -> assertThat(e.regle()).contains("RG-FAC-26"));
    }
}
