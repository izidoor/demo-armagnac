package fr.bnia.contratsarmagnac.application;

import static fr.bnia.contratsarmagnac.application.Monde.AGENT_BNIA;
import static fr.bnia.contratsarmagnac.application.Monde.HENRI;
import static fr.bnia.contratsarmagnac.application.Monde.JEANNE;
import static fr.bnia.contratsarmagnac.application.Monde.NEGOCIANT;
import static fr.bnia.contratsarmagnac.application.Monde.PRODUCTEUR;
import static fr.bnia.contratsarmagnac.application.Monde.TIERS;
import static fr.bnia.contratsarmagnac.application.Monde.millesime2015;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import fr.bnia.commun.domain.AccesRefuse;
import fr.bnia.commun.domain.ElementIntrouvable;
import fr.bnia.commun.domain.RegleEnfreinte;
import fr.bnia.contratsarmagnac.application.ConsulterContrats.Mercuriales;
import fr.bnia.contratsarmagnac.application.NegocierContrats.SaisieContrat;
import fr.bnia.contratsarmagnac.domain.ActionContrat;
import fr.bnia.contratsarmagnac.domain.Partie;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Édition PDF (RG-CA-20) et mercuriales (RG-CA-26), au portail et au back-office. */
class EditerEtMercurialesTest {

    private final Monde monde = new Monde();
    private long conclu;
    private long projet;

    @BeforeEach
    void contrats() {
        NegocierContrats negocier = monde.negocier();
        conclu = negocier.ouvrir(JEANNE, PRODUCTEUR, new SaisieContrat(Partie.VENDEUR, NEGOCIANT,
                millesime2015("15", "1750"))).id();
        negocier.approuver(HENRI, NEGOCIANT, conclu, 1);
        projet = negocier.ouvrir(JEANNE, PRODUCTEUR, new SaisieContrat(Partie.VENDEUR, NEGOCIANT,
                millesime2015("1", "1600"))).id();
    }

    @Test
    @DisplayName("RG-CA-20, 19 : le PDF reprend les parties (SIRET, CVI du vendeur seul) ; l'édition au portail est notée au journal")
    void rg_ca_20_pdf() {
        EditerContrats.DocumentEdite pdf = monde.editer().auPortail(HENRI, NEGOCIANT, conclu);

        assertThat(pdf.nom()).isEqualTo("Contrat-CA-2026-0001.pdf");
        assertThat(monde.editions).singleElement().satisfies(e -> {
            assertThat(e.campagne()).isEqualTo("2026-2027");
            assertThat(e.vendeur().raisonSociale()).isEqualTo("Maison Durand");
            assertThat(e.vendeur().cvi()).contains("3205600001");
            assertThat(e.acheteur().cvi()).isEmpty();
            assertThat(e.acheteur().siret()).contains("77699995500029");
            assertThat(e.acheteur().adresse()).containsExactly("Condom");
        });
        assertThat(monde.journal.duContrat(conclu)).last()
                .satisfies(j -> assertThat(j.action()).isEqualTo(ActionContrat.EDITION_PDF));
        monde.editer().auBackOffice(AGENT_BNIA, projet);
        assertThat(monde.editions).hasSize(2);
    }

    @Test
    @DisplayName("RG-CA-01 : le PDF d'un contrat dont on n'est pas partie est introuvable ; le back-office est au BNIA")
    void rg_ca_01_refus_pdf() {
        assertThatThrownBy(() -> monde.editer().auPortail(HENRI, TIERS, conclu)).isInstanceOf(AccesRefuse.class);
        assertThatThrownBy(() -> monde.editer().auBackOffice(HENRI, conclu)).isInstanceOf(AccesRefuse.class);
        assertThatThrownBy(() -> monde.editer().auBackOffice(AGENT_BNIA, 999)).isInstanceOf(ElementIntrouvable.class);
    }

    @Test
    @DisplayName("RG-CA-26 : les mercuriales des 12 derniers mois ne comptent que les contrats conclus ou exécutés ; réservées au BNIA")
    void rg_ca_26_mercuriales() {
        Mercuriales mercuriales = monde.consulter().mercuriales(AGENT_BNIA, Optional.empty(), Optional.empty());

        assertThat(mercuriales.du()).isEqualTo(LocalDate.of(2025, 10, 24));
        assertThat(mercuriales.au()).isEqualTo(LocalDate.of(2026, 10, 23));
        assertThat(mercuriales.produits()).singleElement().satisfies(m -> {
            assertThat(m.libelle()).isEqualTo("Millésime 2015");
            assertThat(m.contrats()).isEqualTo(1);
            assertThat(m.prixMoyen()).isEqualByComparingTo(new BigDecimal("1750"));
        });
        assertThat(monde.consulter().mercuriales(AGENT_BNIA, Optional.of(LocalDate.of(2026, 1, 1)),
                Optional.of(LocalDate.of(2026, 10, 22))).produits()).isEmpty();
        assertThatThrownBy(() -> monde.consulter().mercuriales(AGENT_BNIA, Optional.of(LocalDate.of(2026, 10, 24)),
                Optional.empty())).isInstanceOf(RegleEnfreinte.class).hasMessageContaining("commence après");
        assertThatThrownBy(() -> monde.consulter().mercuriales(HENRI, Optional.empty(), Optional.empty()))
                .isInstanceOf(AccesRefuse.class);
    }
}
