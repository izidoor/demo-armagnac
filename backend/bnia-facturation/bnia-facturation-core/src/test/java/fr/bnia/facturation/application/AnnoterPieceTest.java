package fr.bnia.facturation.application;

import static fr.bnia.facturation.application.DemanderFacturesTest.demandeDeLExemple;
import static fr.bnia.facturation.application.Monde.GESTIONNAIRE;
import static fr.bnia.facturation.application.Monde.LECTEUR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import fr.bnia.commun.domain.AccesRefuse;
import fr.bnia.commun.domain.RegleEnfreinte;
import fr.bnia.facturation.domain.CoteJournal;
import fr.bnia.facturation.domain.EntreeJournal;
import fr.bnia.facturation.domain.EvenementJournal;
import fr.bnia.facturation.domain.Exemples;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AnnoterPieceTest {

    private final Monde monde = new Monde();
    private final AnnoterPiece cas = monde.annoter();
    private long brouillon;

    @BeforeEach
    void brouillonDeLExemple() {
        brouillon = monde.demander().demander(GESTIONNAIRE, List.of(demandeDeLExemple("7"))).getFirst();
    }

    @Test
    @DisplayName("RG-FAC-20, 21 : une note interne, côté vendeur, sur un brouillon puis sur la pièce émise")
    void rg_fac_21_note() {
        cas.annoter(GESTIONNAIRE, brouillon, "  Relancer le comptable  ");
        monde.vendeurs.vendeur = Exemples.vendeur();
        monde.emettre().emettre(GESTIONNAIRE, brouillon);
        cas.annoter(GESTIONNAIRE, brouillon, "Envoyée par courrier");

        List<EntreeJournal> notes = monde.journal.deLaPiece(brouillon).stream()
                .filter(e -> e.evenement() == EvenementJournal.NOTE).toList();
        assertThat(notes).extracting(EntreeJournal::detail)
                .containsExactly("Envoyée par courrier", "Relancer le comptable");
        assertThat(notes).allMatch(e -> e.cote() == CoteJournal.VENDEUR);
    }

    @Test
    @DisplayName("RG-FAC-21 : une note a un texte")
    void rg_fac_21_texte() {
        assertThatThrownBy(() -> cas.annoter(GESTIONNAIRE, brouillon, " "))
                .isInstanceOfSatisfying(RegleEnfreinte.class, e -> assertThat(e.regle()).contains("RG-FAC-21"));
    }

    @Test
    @DisplayName("RG-FAC-26 : annoter est réservé à qui gère la facturation")
    void rg_fac_26_refus() {
        assertThatThrownBy(() -> cas.annoter(LECTEUR, brouillon, "Note"))
                .isInstanceOfSatisfying(AccesRefuse.class, e -> assertThat(e.regle()).contains("RG-FAC-26"));
    }
}
