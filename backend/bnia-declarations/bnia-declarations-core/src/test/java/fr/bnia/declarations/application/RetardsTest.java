package fr.bnia.declarations.application;

import static fr.bnia.declarations.application.Monde.AGENT_BNIA;
import static fr.bnia.declarations.application.Monde.AGENT_NEGOCIANT;
import static fr.bnia.declarations.application.Monde.HORLOGE;
import static fr.bnia.declarations.application.Monde.NEGOCIANT;
import static fr.bnia.declarations.application.Monde.OBSERVATEUR_BNIA;
import static fr.bnia.declarations.application.Monde.PRODUCTEUR;
import static fr.bnia.declarations.application.Monde.SOMMEIL;
import static org.assertj.core.api.Assertions.assertThat;

import fr.bnia.declarations.application.ListerDsm.DsmListee;
import fr.bnia.declarations.domain.TypeDeclarant;
import fr.bnia.declarations.domain.partage.DeclarationEnRetard;
import fr.bnia.declarations.domain.partage.EtatDsm;
import java.time.Clock;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Échéances, périodes absentes, retards et leur détection (spec 05 lot 7). */
class RetardsTest {

    private static final FiltreDsm TOUT = new FiltreDsm(Optional.empty(), Optional.empty(), Optional.empty(),
            Set.of());

    private final Monde monde = new Monde();
    private final GererDsm dsms = monde.gererDsm();
    private final ListerDsm lister = monde.listerDsm();

    /** Le négociant part du 1er juin ; échéance de juin : le 5 juillet. Le producteur au statut SOMMEIL aussi. */
    @BeforeEach
    void pointsDeDepart() {
        monde.gererPointDeDepart().fixer(AGENT_BNIA, NEGOCIANT, GererPointDeDepartTest.JUIN,
                GererPointDeDepartTest.STOCK);
        monde.gererPointDeDepart().fixer(AGENT_BNIA, SOMMEIL, LocalDate.of(2026, 4, 1), List.of());
    }

    private static Clock le(int annee, int mois, int jour) {
        return Clock.fixed(LocalDate.of(annee, mois, jour).atTime(6, 0).atZone(ZoneId.of("Europe/Paris")).toInstant(),
                ZoneId.of("Europe/Paris"));
    }

    @Test
    @DisplayName("RG-DEC-40 : la période attendue sans DSM est une ligne « absente » du listing, en retard après l'échéance")
    void rg_dec_40_absente_au_listing() {
        assertThat(lister.lister(OBSERVATEUR_BNIA, TOUT)).singleElement().satisfies(l -> {
            assertThat(l.absente()).isTrue();
            assertThat(l.operateur()).isEqualTo(NEGOCIANT);
            assertThat(l.code()).isEqualTo("N7");
            assertThat(l.periode().libelle()).isEqualTo("juin 2026");
            assertThat(l.echeance()).isEqualTo(LocalDate.of(2026, 7, 5));
            assertThat(l.enRetard()).isTrue();
            assertThat(l.totalHt()).isEmpty();
        });
        assertThat(lister.lister(AGENT_BNIA, new FiltreDsm(Optional.empty(), Optional.of(TypeDeclarant.PRODUCTEUR),
                Optional.empty(), Set.of()))).as("filtre de type").isEmpty();
        assertThat(lister.lister(AGENT_BNIA, new FiltreDsm(Optional.empty(), Optional.empty(),
                Optional.of(YearMonth.of(2026, 7)), Set.of()))).as("filtre de mois").isEmpty();
        assertThat(lister.lister(AGENT_BNIA, new FiltreDsm(Optional.empty(), Optional.empty(), Optional.empty(),
                Set.of(EtatDsm.BROUILLON)))).as("un état demandé exclut les absentes").isEmpty();
    }

    @Test
    @DisplayName("RG-DEC-40 : une période absente n'est listée qu'en retard, le lendemain de son échéance")
    void rg_dec_40_absente_listee_en_retard() {
        assertThat(monde.listerDsm(le(2026, 7, 5)).lister(AGENT_BNIA, TOUT)).as("le jour de l'échéance").isEmpty();
        assertThat(monde.listerDsm(le(2026, 7, 6)).lister(AGENT_BNIA, TOUT)).singleElement()
                .satisfies(l -> assertThat(l.absente() && l.enRetard()).isTrue());
    }

    @Test
    @DisplayName("RG-DEC-40 : une DSM en brouillon après l'échéance est en retard ; envoyée, elle attend le BNIA")
    void rg_dec_40_dsm_en_retard() {
        long id = dsms.initier(AGENT_NEGOCIANT, NEGOCIANT).dsm().id();

        DsmListee brouillon = lister.lister(AGENT_BNIA, TOUT).getFirst();
        assertThat(brouillon.absente()).isFalse();
        assertThat(brouillon.enRetard()).isTrue();
        assertThat(brouillon.echeance()).isEqualTo(LocalDate.of(2026, 7, 5));
        VueDsm vue = dsms.consulter(AGENT_NEGOCIANT, NEGOCIANT, id);
        assertThat(vue.echeance()).isEqualTo(LocalDate.of(2026, 7, 5));
        assertThat(vue.enRetard()).isTrue();
        assertThat(dsms.situation(AGENT_NEGOCIANT, NEGOCIANT).enRetard()).isTrue();

        dsms.envoyer(AGENT_NEGOCIANT, NEGOCIANT, id, null);
        assertThat(lister.lister(AGENT_BNIA, TOUT)).singleElement().satisfies(l -> assertThat(l.enRetard()).isFalse());
        assertThat(dsms.consulter(AGENT_BNIA, NEGOCIANT, id).enRetard()).isFalse();
        assertThat(dsms.situation(AGENT_NEGOCIANT, NEGOCIANT).enRetard()).isFalse();
    }

    @Test
    @DisplayName("RG-DEC-41 : DeclarationEnRetard publié une seule fois, le lendemain de l'échéance")
    void rg_dec_41_detection() {
        assertThat(monde.detecterRetards(le(2026, 7, 5)).detecter()).as("le jour de l'échéance").isZero();
        assertThat(monde.evenements.publies).isEmpty();

        assertThat(monde.detecterRetards(le(2026, 7, 6)).detecter()).isEqualTo(1);
        assertThat(monde.detecterRetards(le(2026, 7, 7)).detecter()).as("déjà signalé").isZero();
        assertThat(monde.evenements.publies).singleElement().isEqualTo(new DeclarationEnRetard(NEGOCIANT,
                LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 30), LocalDate.of(2026, 7, 5), null));
    }

    @Test
    @DisplayName("RG-DEC-41 : la DSM en brouillon est citée ; un opérateur hors statut OUI ou sans point de départ n'est pas signalé")
    void rg_dec_41_dsm_citee() {
        long id = dsms.initier(AGENT_NEGOCIANT, NEGOCIANT).dsm().id();
        assertThat(monde.detecterRetards(HORLOGE).detecter()).isEqualTo(1);
        assertThat(monde.evenements.publies).singleElement()
                .satisfies(e -> assertThat(((DeclarationEnRetard) e).dsm()).isEqualTo(id));
        assertThat(monde.evenements.publies).noneMatch(e -> e instanceof DeclarationEnRetard r
                && (r.operateur() == SOMMEIL || r.operateur() == PRODUCTEUR));
    }
}
