package fr.bnia.facturation.application;

import static fr.bnia.facturation.application.DemanderFacturesTest.demandeDeLExemple;
import static fr.bnia.facturation.application.Monde.CLIENT;
import static fr.bnia.facturation.application.Monde.ETRANGER;
import static fr.bnia.facturation.application.Monde.GESTIONNAIRE;
import static fr.bnia.facturation.application.Monde.OPERATEUR_CLIENT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import fr.bnia.commun.domain.AccesRefuse;
import fr.bnia.commun.domain.ElementIntrouvable;
import fr.bnia.commun.domain.RegleEnfreinte;
import fr.bnia.facturation.api.LigneVendue;
import fr.bnia.facturation.domain.CoteJournal;
import fr.bnia.facturation.domain.EntreeJournal;
import fr.bnia.facturation.domain.EtatPiece;
import fr.bnia.facturation.domain.EvenementJournal;
import fr.bnia.facturation.domain.Exemples;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ConsulterMesFacturesTest {

    private final Monde monde = new Monde();
    private final List<FiltrePieces> filtres = new ArrayList<>();
    private final ConsulterMesFactures cas = monde.mesFactures(filtre -> {
        filtres.add(filtre);
        return List.of();
    });
    private long emise;
    private long brouillon;

    @BeforeEach
    void pieces() {
        monde.vendeurs.vendeur = Exemples.vendeur();
        List<Long> ids = monde.demander().demander(GESTIONNAIRE,
                List.of(demandeDeLExemple("7"), demandeDeLExemple("8")));
        emise = ids.getFirst();
        brouillon = ids.getLast();
        monde.emettre().emettre(GESTIONNAIRE, emise);
    }

    @Test
    @DisplayName("RG-FAC-28 : le listing ne rend que les pièces émises dont l'opérateur est le client")
    void rg_fac_28_listing() {
        cas.lister(CLIENT, OPERATEUR_CLIENT, new FiltrePieces(Optional.of("2026"), Optional.empty(),
                Optional.of(EtatPiece.BROUILLON), Optional.of("DSM"), Optional.of("999"), Optional.of("autre"),
                Optional.empty(), Optional.empty(), Optional.empty()));

        assertThat(filtres).singleElement().satisfies(f -> {
            assertThat(f.etat()).contains(EtatPiece.EMISE);
            assertThat(f.client()).contains("42");
            assertThat(f.acheteur()).isEmpty();
            assertThat(f.numero()).contains("2026");
            assertThat(f.nature()).contains("DSM");
        });
    }

    @Test
    @DisplayName("RG-FAC-28 : l'ouverture et le téléchargement sont journalisés « consultée », côté client")
    void rg_fac_28_consultation_journalisee() {
        assertThat(cas.consulter(CLIENT, OPERATEUR_CLIENT, emise).piece().numero()).contains("2026000001");
        cas.pdf(CLIENT, OPERATEUR_CLIENT, emise);

        List<EntreeJournal> consultations = monde.journal.deLaPiece(emise).stream()
                .filter(e -> e.evenement() == EvenementJournal.CONSULTEE).toList();
        assertThat(consultations).extracting(EntreeJournal::detail).containsExactly("PDF", null);
        assertThat(consultations).allSatisfy(e -> {
            assertThat(e.cote()).isEqualTo(CoteJournal.CLIENT);
            assertThat(e.nomActeur()).isEqualTo("Utilisateur 4");
        });
    }

    @Test
    @DisplayName("RG-FAC-26 : un brouillon, ou la pièce d'un autre client, est introuvable au portail")
    void rg_fac_26_brouillon_et_autre_client() {
        assertThatThrownBy(() -> cas.consulter(CLIENT, OPERATEUR_CLIENT, brouillon))
                .isInstanceOf(ElementIntrouvable.class);
        assertThatThrownBy(() -> cas.consulter(GESTIONNAIRE, 43, emise)).isInstanceOf(ElementIntrouvable.class);
    }

    @Test
    @DisplayName("RG-FAC-34 : les lignes article des seules pièces émises de l'opérateur, dans la période et de la nature demandée")
    void rg_fac_34_lignes() {
        LocalDate debut = LocalDate.of(2026, 10, 1);
        LocalDate fin = LocalDate.of(2026, 10, 31);

        assertThat(cas.lignes(CLIENT, OPERATEUR_CLIENT, debut, fin, Optional.of("DSM")))
                .extracting(LigneVendue::piece).containsExactly(emise, emise, emise, emise);
        assertThat(cas.lignes(CLIENT, OPERATEUR_CLIENT, debut, fin, Optional.empty())).hasSize(4);
        assertThat(cas.lignes(CLIENT, OPERATEUR_CLIENT, debut, fin, Optional.of("FIN_DE_TRAVAUX"))).isEmpty();
        assertThat(cas.lignes(CLIENT, OPERATEUR_CLIENT, debut.minusMonths(1), debut.minusDays(1), Optional.empty()))
                .isEmpty();
        assertThat(cas.lignes(GESTIONNAIRE, 43, debut, fin, Optional.empty())).isEmpty();
        assertThatThrownBy(() -> cas.lignes(CLIENT, OPERATEUR_CLIENT, fin, debut, Optional.empty()))
                .isInstanceOfSatisfying(RegleEnfreinte.class, e -> assertThat(e.regle()).contains("RG-FAC-30"));
    }

    @Test
    @DisplayName("RG-FAC-26, 34 : les factures d'un opérateur et leurs lignes sont refusées à qui ne lui est pas rattaché")
    void rg_fac_26_refus() {
        assertThatThrownBy(() -> cas.lister(ETRANGER, OPERATEUR_CLIENT, FiltrePieces.aucun()))
                .isInstanceOfSatisfying(AccesRefuse.class, e -> assertThat(e.regle()).contains("RG-FAC-26"));
        assertThatThrownBy(() -> cas.consulter(CLIENT, 43, emise)).isInstanceOf(AccesRefuse.class);
        assertThatThrownBy(() -> cas.pdf(ETRANGER, OPERATEUR_CLIENT, emise)).isInstanceOf(AccesRefuse.class);
        assertThatThrownBy(() -> cas.lignes(ETRANGER, OPERATEUR_CLIENT, LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 10, 31), Optional.empty())).isInstanceOf(AccesRefuse.class);
    }
}
