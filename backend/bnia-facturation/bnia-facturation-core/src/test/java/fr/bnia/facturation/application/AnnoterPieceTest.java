package fr.bnia.facturation.application;

import static fr.bnia.facturation.application.DemanderFacturesTest.demandeDeLExemple;
import static fr.bnia.facturation.application.Monde.ETRANGER;
import static fr.bnia.facturation.application.Monde.GESTIONNAIRE;
import static fr.bnia.facturation.application.Monde.LECTEUR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import fr.bnia.commun.domain.AccesRefuse;
import fr.bnia.commun.domain.ElementIntrouvable;
import fr.bnia.commun.domain.RegleEnfreinte;
import fr.bnia.facturation.domain.EvenementJournal;
import fr.bnia.facturation.domain.Exemples;
import fr.bnia.facturation.domain.NoteInterne;
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
    @DisplayName("RG-FAC-21 : une note interne sur un brouillon puis sur la pièce émise, au carnet, hors du journal")
    void rg_fac_21_note() {
        cas.ecrire(GESTIONNAIRE, brouillon, "  Relancer le comptable  ");
        monde.vendeurs.vendeur = Exemples.vendeur();
        monde.emettre().emettre(GESTIONNAIRE, brouillon);
        cas.ecrire(GESTIONNAIRE, brouillon, "Envoyée par courrier");

        assertThat(cas.lister(LECTEUR, brouillon)).extracting(NoteInterne::texte)
                .containsExactly("Relancer le comptable", "Envoyée par courrier");
        assertThat(monde.journal.deLaPiece(brouillon)).extracting(e -> e.evenement())
                .containsExactly(EvenementJournal.EMISE, EvenementJournal.DEMANDEE);
    }

    @Test
    @DisplayName("RG-FAC-21 : une note a un texte ; son auteur la modifie ou la supprime, sur cette pièce seulement")
    void rg_fac_21_auteur() {
        assertThatThrownBy(() -> cas.ecrire(GESTIONNAIRE, brouillon, " "))
                .isInstanceOfSatisfying(RegleEnfreinte.class, e -> assertThat(e.regle()).contains("RG-FAC-21"));
        NoteInterne note = cas.ecrire(GESTIONNAIRE, brouillon, "Attendre le SIRET");

        NoteInterne modifiee = cas.modifier(GESTIONNAIRE, brouillon, note.id(), "SIRET reçu");
        assertThat(modifiee.texte()).isEqualTo("SIRET reçu");
        assertThat(modifiee.modifieeLe()).isNotNull();

        cas.supprimer(GESTIONNAIRE, brouillon, note.id());
        assertThat(cas.lister(GESTIONNAIRE, brouillon)).isEmpty();
        assertThatThrownBy(() -> cas.supprimer(GESTIONNAIRE, brouillon + 1, note.id()))
                .isInstanceOf(ElementIntrouvable.class);
    }

    @Test
    @DisplayName("RG-FAC-26 : écrire une note est réservé à qui gère la facturation, la lire au personnel du BNIA")
    void rg_fac_26_refus() {
        assertThatThrownBy(() -> cas.ecrire(LECTEUR, brouillon, "Note"))
                .isInstanceOfSatisfying(AccesRefuse.class, e -> assertThat(e.regle()).contains("RG-FAC-26"));
        assertThatThrownBy(() -> cas.lister(ETRANGER, brouillon))
                .isInstanceOfSatisfying(AccesRefuse.class, e -> assertThat(e.regle()).contains("RG-FAC-26"));
        assertThat(monde.notes.deLaPiece(brouillon)).isEmpty();
    }
}
