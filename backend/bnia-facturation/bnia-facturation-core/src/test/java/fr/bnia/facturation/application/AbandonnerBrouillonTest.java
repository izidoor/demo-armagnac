package fr.bnia.facturation.application;

import static fr.bnia.facturation.application.DemanderFacturesTest.demandeDeLExemple;
import static fr.bnia.facturation.application.Monde.GESTIONNAIRE;
import static fr.bnia.facturation.application.Monde.LECTEUR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import fr.bnia.commun.domain.AccesRefuse;
import fr.bnia.commun.domain.RegleEnfreinte;
import fr.bnia.facturation.api.FactureAbandonnee;
import fr.bnia.facturation.domain.Exemples;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AbandonnerBrouillonTest {

    private final Monde monde = new Monde();
    private final AbandonnerBrouillon cas = monde.abandonner();
    private long brouillon;

    @BeforeEach
    void brouillonDeLExemple() {
        brouillon = monde.demander().demander(GESTIONNAIRE, List.of(demandeDeLExemple("7"))).getFirst();
    }

    @Test
    @DisplayName("RG-FAC-18, 25 : le brouillon est supprimé avec son journal et ses notes internes ; FactureAbandonnee "
            + "publie son origine")
    void rg_fac_18_abandon() {
        monde.annoter().ecrire(GESTIONNAIRE, brouillon, "Attendre le SIRET");

        cas.abandonner(GESTIONNAIRE, brouillon);

        assertThat(monde.factures.parId(brouillon)).isEmpty();
        assertThat(monde.notes.deLaPiece(brouillon)).isEmpty();
        assertThat(monde.evenements).singleElement().isInstanceOfSatisfying(FactureAbandonnee.class, e -> {
            assertThat(e.piece()).isEqualTo(brouillon);
            assertThat(e.origine().identifiant()).isEqualTo("7");
            assertThat(e.abandonneePar()).isEqualTo(GESTIONNAIRE);
        });
        // RG-FAC-02 : l'origine est libérée, elle se refacture.
        assertThat(monde.demander().demander(GESTIONNAIRE, List.of(demandeDeLExemple("7")))).hasSize(1);
    }

    @Test
    @DisplayName("RG-FAC-14 : une pièce émise ne s'abandonne pas")
    void rg_fac_14_emise() {
        monde.vendeurs.vendeur = Exemples.vendeur();
        monde.emettre().emettre(GESTIONNAIRE, brouillon);

        assertThatThrownBy(() -> cas.abandonner(GESTIONNAIRE, brouillon))
                .isInstanceOfSatisfying(RegleEnfreinte.class, e -> assertThat(e.regle()).contains("RG-FAC-14"));
    }

    @Test
    @DisplayName("RG-FAC-26 : abandonner est réservé à qui gère la facturation")
    void rg_fac_26_refus() {
        assertThatThrownBy(() -> cas.abandonner(LECTEUR, brouillon))
                .isInstanceOfSatisfying(AccesRefuse.class, e -> assertThat(e.regle()).contains("RG-FAC-26"));
        assertThat(monde.factures.parId(brouillon)).isPresent();
    }
}
