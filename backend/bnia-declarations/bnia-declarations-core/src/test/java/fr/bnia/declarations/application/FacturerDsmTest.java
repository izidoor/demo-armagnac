package fr.bnia.declarations.application;

import static fr.bnia.declarations.application.Monde.AGENT_BNIA;
import static fr.bnia.declarations.application.Monde.AGENT_NEGOCIANT;
import static fr.bnia.declarations.application.Monde.NEGOCIANT;
import static fr.bnia.declarations.application.Monde.OBSERVATEUR_BNIA;
import static fr.bnia.declarations.application.Monde.PRODUCTEUR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import fr.bnia.commun.domain.AccesRefuse;
import fr.bnia.commun.domain.RegleEnfreinte;
import fr.bnia.commun.domain.VolumeAlcoolPur;
import fr.bnia.declarations.application.Facturation.DemandeFactureDsm;
import fr.bnia.declarations.application.Facturation.LigneArticle;
import fr.bnia.declarations.application.GererGrilleCotisation.SaisieTauxCvo;
import fr.bnia.declarations.domain.ActionJournalDeclarations;
import fr.bnia.declarations.domain.ColonneDsm;
import fr.bnia.declarations.domain.DeclarationStocksMouvements;
import fr.bnia.declarations.domain.LigneDsm;
import fr.bnia.declarations.domain.SaisieDsm;
import fr.bnia.declarations.domain.SaisieLigneDsm;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Facturer une DSM, révision de la spec 05 par la spec 08 (§3.13) : RG-DEC-43. */
class FacturerDsmTest {

    private final Monde monde = new Monde();
    private final GererDsm dsms = monde.gererDsm();
    private final FacturerDsm cas = monde.facturerDsm();
    private long juin;

    /** La DSM de juin du négociant, validée : 839,11 L vendus en France (colonne 17, sans justificatif). */
    @BeforeEach
    void juinValidee() {
        monde.gererGrille().ajouter(AGENT_BNIA, new SaisieTauxCvo(new BigDecimal("60"), new BigDecimal("20"),
                LocalDate.of(2024, 1, 1)));
        monde.gererPointDeDepart().fixer(AGENT_BNIA, NEGOCIANT, GererPointDeDepartTest.JUIN, GererPointDeDepartTest.STOCK);
        DeclarationStocksMouvements dsm = dsms.initier(AGENT_NEGOCIANT, NEGOCIANT).dsm();
        juin = dsm.id();
        List<SaisieLigneDsm> lignes = new ArrayList<>();
        for (int i = 0; i < dsm.lignes().size(); i++) {
            lignes.add(telle(dsm.lignes().get(i), i == 0 ? Map.of(ColonneDsm.C17, VolumeAlcoolPur.litres("839.11"))
                    : Map.of()));
        }
        dsms.enregistrer(AGENT_NEGOCIANT, NEGOCIANT, juin, new SaisieDsm(null, lignes), null);
        dsms.envoyer(AGENT_NEGOCIANT, NEGOCIANT, juin, null);
        dsms.prendreEnInstruction(AGENT_BNIA, NEGOCIANT, juin, null);
        assertThat(dsms.consulter(AGENT_BNIA, NEGOCIANT, juin).actions()).doesNotContain(ActionDsm.FACTURER);
        dsms.valider(AGENT_BNIA, NEGOCIANT, juin, null);
    }

    private static SaisieLigneDsm telle(LigneDsm l, Map<ColonneDsm, VolumeAlcoolPur> volumes) {
        return new SaisieLigneDsm(l.id(), l.identite().cle(),
                l.identite().millesime() == null ? null : l.identite().millesime().annee(),
                l.identite().denomination() == null ? null : l.identite().denomination().texte(),
                l.identite().compte().toString(), volumes);
    }

    @Test
    @DisplayName("RG-DEC-43 : une demande par DSM validée, un groupe par famille, deux articles par ligne de cotisation, 30 jours fin de mois")
    void rg_dec_43_demande() {
        VueDsm avant = dsms.consulter(AGENT_BNIA, NEGOCIANT, juin);
        assertThat(avant.actions()).contains(ActionDsm.FACTURER);
        assertThat(avant.manquePourFacturer()).isEmpty();

        FacturerDsm.DsmFacturee facturee = cas.facturer(AGENT_BNIA, NEGOCIANT, juin);

        DemandeFactureDsm demande = monde.facturation.demandes.getFirst();
        assertThat(demande.reference()).isEqualTo("DSM juin 2026");
        assertThat(demande.libellePeriode()).isEqualTo("DSM de juin 2026");
        assertThat(demande.dateOperation()).isEqualTo(LocalDate.of(2026, 6, 30));
        assertThat(demande.joursPaiement()).isEqualTo(30);
        assertThat(demande.finDeMois()).isTrue();
        assertThat(demande.groupes()).singleElement().satisfies(g -> {
            assertThat(g.titre()).isEqualTo("Commerce de l'Armagnac");
            assertThat(g.lignes()).extracting(LigneArticle::code, LigneArticle::quantite).containsExactly(
                    org.assertj.core.groups.Tuple.tuple("CVO-SORTIES_FRANCE-S", new BigDecimal("8.3911")),
                    org.assertj.core.groups.Tuple.tuple("CVO-SORTIES_FRANCE-E", new BigDecimal("8.3911")));
        });
        assertThat(demande.notePied()).contains("60,00 €/hlAP").contains("80,00 %");

        VueDsm vue = dsms.consulter(AGENT_BNIA, NEGOCIANT, juin);
        assertThat(vue.brouillonFacture()).contains(facturee.brouillon());
        assertThat(vue.actions()).doesNotContain(ActionDsm.FACTURER);
        assertThat(monde.journal.deLaDsm(juin).getFirst().action()).isEqualTo(ActionJournalDeclarations.FACTURATION_DEMANDEE);
        ListerDsm.DsmListee listee = monde.listerDsm().lister(AGENT_BNIA, new FiltreDsm(Optional.empty(),
                Optional.empty(), Optional.empty(), Set.of())).stream()
                .filter(l -> l.dsm().map(d -> d.id() == juin).orElse(false)).findFirst().orElseThrow();
        assertThat(listee.dsm().orElseThrow().facturationDemandee()).isTrue();
        assertThat(listee.operateurFacturable()).isTrue();
        assertThat(listee.totalHt()).as("total calculé tant que la facture n'est pas émise")
                .contains(new BigDecimal("503.46"));
    }

    @Test
    @DisplayName("RG-DEC-43 : une DSM non validée, déjà demandée, ou d'un opérateur non facturable est refusée, et le refus la nomme")
    void rg_dec_43_refus() {
        cas.facturer(AGENT_BNIA, List.of(juin));
        assertThatThrownBy(() -> cas.facturer(AGENT_BNIA, List.of(juin)))
                .isInstanceOfSatisfying(RegleEnfreinte.class, e -> {
                    assertThat(e.regle()).contains("RG-DEC-43");
                    assertThat(e).hasMessage("Opérateur 7, DSM juin 2026 : sa facturation est déjà demandée.");
                });

        monde.operateur(NEGOCIANT, fr.bnia.operateurs.domain.partage.TypeOperateur.NEGOCIANT,
                fr.bnia.operateurs.domain.partage.StatutDeclarationStock.OUI);
        long juillet = dsms.initier(AGENT_NEGOCIANT, NEGOCIANT).dsm().id();
        assertThatThrownBy(() -> cas.facturer(AGENT_BNIA, List.of(juillet)))
                .hasMessage("Opérateur 7, DSM juillet 2026 : elle n'est pas validée.");
        assertThat(monde.facturation.demandes).hasSize(1);
    }

    @Test
    @DisplayName("RG-DEC-43 : un opérateur non facturable est refusé, avec ce qui lui manque")
    void rg_dec_43_non_facturable() {
        DeclarationStocksMouvements dsm = monde.dsms.parId(juin).orElseThrow();
        // Le négociant perd son SIRET : l'acheteur de la fiche décide (RG-OPE-02).
        var sansSiret = monde.operateurs.acheteur(PRODUCTEUR)
                .map(a -> new fr.bnia.operateurs.domain.partage.AcheteurVue(NEGOCIANT, a.code(), "Domaine de Laubade",
                        Optional.empty(), Optional.empty(), a.adresse(), Optional.empty(), List.of("SIRET"), true));
        org.mockito.Mockito.when(monde.operateurs.acheteur(NEGOCIANT)).thenReturn(sansSiret);

        assertThatThrownBy(() -> cas.facturer(AGENT_BNIA, List.of(dsm.id())))
                .hasMessage("Domaine de Laubade, DSM juin 2026 : Domaine de Laubade n'est pas facturable (SIRET manquant).");
    }

    @Test
    @DisplayName("RG-DEC-43 : la vue d'une DSM à facturer dit ce qui manque à l'opérateur, le listing qu'il n'est pas facturable")
    void rg_dec_43_non_facturable_annonce() {
        var sansSiret = monde.operateurs.acheteur(PRODUCTEUR)
                .map(a -> new fr.bnia.operateurs.domain.partage.AcheteurVue(NEGOCIANT, a.code(), "Domaine de Laubade",
                        Optional.empty(), Optional.empty(), a.adresse(), Optional.empty(), List.of("SIRET"), true));
        org.mockito.Mockito.when(monde.operateurs.acheteur(NEGOCIANT)).thenReturn(sansSiret);
        org.mockito.Mockito.when(monde.operateurs.facturables(org.mockito.ArgumentMatchers.anyCollection()))
                .thenReturn(Set.of());

        VueDsm vue = dsms.consulter(AGENT_BNIA, NEGOCIANT, juin);
        assertThat(vue.actions()).as("l'écran montre l'action, désactivée").contains(ActionDsm.FACTURER);
        assertThat(vue.manquePourFacturer()).containsExactly("SIRET");
        assertThat(dsms.consulter(OBSERVATEUR_BNIA, NEGOCIANT, juin).manquePourFacturer())
                .as("sans l'action, rien à annoncer").isEmpty();
        ListerDsm.DsmListee listee = monde.listerDsm().lister(AGENT_BNIA, new FiltreDsm(Optional.of(NEGOCIANT),
                Optional.empty(), Optional.empty(), Set.of())).stream()
                .filter(l -> l.dsm().map(d -> d.id() == juin).orElse(false)).findFirst().orElseThrow();
        assertThat(listee.operateurFacturable()).isFalse();
    }

    @Test
    @DisplayName("RG-DEC-43 : facturer est réservé à qui gère la facturation au BNIA")
    void rg_dec_43_droits() {
        assertThatThrownBy(() -> cas.facturer(OBSERVATEUR_BNIA, List.of(juin)))
                .isInstanceOfSatisfying(AccesRefuse.class, e -> assertThat(e.regle()).contains("RG-DEC-43"));
        assertThatThrownBy(() -> cas.facturer(AGENT_NEGOCIANT, NEGOCIANT, juin)).isInstanceOf(AccesRefuse.class);
        assertThat(dsms.consulter(OBSERVATEUR_BNIA, NEGOCIANT, juin).actions()).doesNotContain(ActionDsm.FACTURER);
    }
}
