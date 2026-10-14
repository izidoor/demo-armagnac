package fr.bnia.declarations.application;

import static fr.bnia.declarations.application.Monde.AGENT_BNIA;
import static fr.bnia.declarations.application.Monde.AGENT_NEGOCIANT;
import static fr.bnia.declarations.application.Monde.NEGOCIANT;
import static fr.bnia.declarations.application.Monde.OBSERVATEUR_BNIA;
import static fr.bnia.declarations.application.Monde.OBSERVATEUR_NEGOCIANT;
import static fr.bnia.declarations.application.Monde.PRODUCTEUR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import fr.bnia.commun.domain.AccesRefuse;
import fr.bnia.commun.domain.ConflitUnicite;
import fr.bnia.commun.domain.RegleEnfreinte;
import fr.bnia.commun.domain.VolumeAlcoolPur;
import fr.bnia.declarations.domain.ActionJournalDeclarations;
import fr.bnia.declarations.domain.ColonneDsm;
import fr.bnia.declarations.domain.DeclarationStocksMouvements;
import fr.bnia.declarations.domain.SaisieLigneStock;
import fr.bnia.declarations.domain.partage.CleConditionnement;
import fr.bnia.declarations.domain.partage.EtatDsm;
import fr.bnia.operateurs.domain.partage.Acteur;
import fr.bnia.operateurs.domain.partage.StatutDeclarationStock;
import fr.bnia.operateurs.domain.partage.TypeOperateur;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class GererPointDeDepartTest {

    static final LocalDate JUIN = LocalDate.of(2026, 6, 1);
    static final List<SaisieLigneStock> STOCK = List.of(
            new SaisieLigneStock(CleConditionnement.NB, null, "XO", "12", VolumeAlcoolPur.litres("1500")),
            new SaisieLigneStock(CleConditionnement.MB, 2010, null, null, VolumeAlcoolPur.litres("640.25")));

    private final Monde monde = new Monde();
    private final GererPointDeDepart cas = monde.gererPointDeDepart();

    @Test
    @DisplayName("RG-DEC-01 : seul le BNIA qui gère le module fixe ou modifie le point de départ ; tout membre le lit")
    void rg_dec_01_refus() {
        for (Acteur refuse : List.of(AGENT_NEGOCIANT, OBSERVATEUR_BNIA, Monde.INCONNU)) {
            assertThatThrownBy(() -> cas.fixer(refuse, NEGOCIANT, JUIN, STOCK)).isInstanceOf(AccesRefuse.class)
                    .hasMessageContaining("BNIA");
        }
        long id = cas.fixer(AGENT_BNIA, NEGOCIANT, JUIN, STOCK).point().id();
        assertThatThrownBy(() -> cas.modifier(AGENT_NEGOCIANT, NEGOCIANT, id, JUIN, STOCK, null))
                .isInstanceOf(AccesRefuse.class);
        assertThat(cas.consulter(OBSERVATEUR_NEGOCIANT, NEGOCIANT)).isPresent();
        assertThat(cas.consulter(OBSERVATEUR_BNIA, NEGOCIANT)).isPresent();
        assertThatThrownBy(() -> cas.consulter(Monde.INCONNU, NEGOCIANT)).isInstanceOf(AccesRefuse.class)
                .extracting(e -> ((RegleEnfreinte) e).regle().orElseThrow()).isEqualTo("RG-DEC-01");
    }

    @Test
    @DisplayName("RG-DEC-06 : un négociant part un premier du mois, un producteur un 1er avril")
    void rg_dec_06_debut_selon_le_type() {
        assertThatThrownBy(() -> cas.fixer(AGENT_BNIA, PRODUCTEUR, JUIN, STOCK)).isInstanceOf(RegleEnfreinte.class)
                .hasMessageContaining("1er avril");
        assertThat(cas.fixer(AGENT_BNIA, PRODUCTEUR, LocalDate.of(2026, 4, 1), List.of()).point().lignes()).isEmpty();
    }

    @Test
    @DisplayName("RG-DEC-07 : le point de départ porte le stock initial ; un second se modifie plutôt")
    void rg_dec_07_fixer() {
        GererPointDeDepart.PointDeDepart point = cas.fixer(AGENT_BNIA, NEGOCIANT, JUIN, STOCK);
        assertThat(point.point().lignes()).hasSize(2);
        assertThat(point.point().fixePar().nom()).isEqualTo("Utilisateur 20");
        assertThat(point.fige()).isFalse();
        assertThatThrownBy(() -> cas.fixer(AGENT_BNIA, NEGOCIANT, JUIN, STOCK)).isInstanceOf(ConflitUnicite.class);
        monde.operateur(99, TypeOperateur.AUTRE, StatutDeclarationStock.NON);
        assertThatThrownBy(() -> cas.fixer(AGENT_BNIA, 99, JUIN, STOCK)).isInstanceOf(RegleEnfreinte.class)
                .extracting(e -> ((RegleEnfreinte) e).regle().orElseThrow()).isEqualTo("RG-DEC-02");
    }

    @Test
    @DisplayName("RG-DEC-08 : la DSM en cours suit le stock initial modifié ; le début ne change pas pendant ce temps")
    void rg_dec_08_la_dsm_en_cours_suit() {
        long id = cas.fixer(AGENT_BNIA, NEGOCIANT, JUIN, STOCK).point().id();
        long dsm = monde.gererDsm().initier(AGENT_NEGOCIANT, NEGOCIANT).dsm().id();
        List<SaisieLigneStock> corrige = List.of(
                new SaisieLigneStock(CleConditionnement.NB, null, "XO", "12", VolumeAlcoolPur.litres("1600")));
        cas.modifier(AGENT_BNIA, NEGOCIANT, id, JUIN, corrige, 0L);
        DeclarationStocksMouvements suivie = monde.dsms.parId(dsm).orElseThrow();
        assertThat(suivie.lignes().getFirst().volume(ColonneDsm.C01)).isEqualTo(VolumeAlcoolPur.litres("1600"));
        assertThat(suivie.total(ColonneDsm.C01)).isEqualTo(VolumeAlcoolPur.litres("1600"));
        assertThatThrownBy(() -> cas.modifier(AGENT_BNIA, NEGOCIANT, id, JUIN.plusMonths(1), corrige, null))
                .isInstanceOf(RegleEnfreinte.class).hasMessageContaining("juin 2026");
    }

    @Test
    @DisplayName("RG-DEC-08 : figé dès que la DSM qui commence à son début est validée")
    void rg_dec_08_fige() {
        long id = cas.fixer(AGENT_BNIA, NEGOCIANT, JUIN, STOCK).point().id();
        DeclarationStocksMouvements dsm = monde.dsms.parId(monde.gererDsm().initier(AGENT_NEGOCIANT, NEGOCIANT).dsm().id())
                .orElseThrow();
        monde.dsms.forcer(dsm, EtatDsm.VALIDEE);
        assertThat(cas.consulter(AGENT_BNIA, NEGOCIANT).orElseThrow().fige()).isTrue();
        assertThatThrownBy(() -> cas.modifier(AGENT_BNIA, NEGOCIANT, id, JUIN, STOCK, null))
                .isInstanceOf(RegleEnfreinte.class).hasMessageContaining("figé");
    }

    @Test
    @DisplayName("RG-DEC-23 : point de départ fixé, modifié, au journal de l'opérateur")
    void rg_dec_23_journal() {
        long id = cas.fixer(AGENT_BNIA, NEGOCIANT, JUIN, STOCK).point().id();
        cas.modifier(AGENT_BNIA, NEGOCIANT, id, JUIN.plusMonths(1), STOCK, null);
        assertThat(monde.journal.actions()).containsExactly(ActionJournalDeclarations.POINT_DE_DEPART_FIXE,
                ActionJournalDeclarations.POINT_DE_DEPART_MODIFIE);
        EntreeJournal derniere = monde.journal.deLOperateur(NEGOCIANT).getFirst();
        assertThat(derniere.dsm()).isNull();
        assertThat(derniere.nomActeur()).isEqualTo("Utilisateur 20");
        assertThat(derniere.detail()).isEqualTo("Début au 01/07/2026, 2 lignes de stock initial");
    }

    /** La DSM de juin du négociant, initiée, envoyée, prise en instruction et validée, sans mouvement. */
    private long juinValidee() {
        GererDsm dsms = monde.gererDsm();
        long dsm = dsms.initier(AGENT_NEGOCIANT, NEGOCIANT).dsm().id();
        dsms.envoyer(AGENT_NEGOCIANT, NEGOCIANT, dsm, null);
        dsms.prendreEnInstruction(AGENT_BNIA, NEGOCIANT, dsm, null);
        dsms.valider(AGENT_BNIA, NEGOCIANT, dsm, null);
        return dsm;
    }

    @Test
    @DisplayName("RG-DEC-09 : nouveau point de départ quand le précédent est figé et qu'aucune DSM n'est en cours")
    void rg_dec_09_nouveau_point_de_depart() {
        cas.fixer(AGENT_BNIA, NEGOCIANT, JUIN, STOCK);
        assertThat(cas.consulter(AGENT_BNIA, NEGOCIANT).orElseThrow().renouvelable()).isFalse();
        juinValidee();
        assertThat(cas.consulter(AGENT_BNIA, NEGOCIANT).orElseThrow().renouvelable()).isTrue();

        long juillet = monde.gererDsm().initier(AGENT_NEGOCIANT, NEGOCIANT).dsm().id();
        assertThat(cas.consulter(AGENT_BNIA, NEGOCIANT).orElseThrow().renouvelable()).isFalse();
        assertThatThrownBy(() -> cas.fixer(AGENT_BNIA, NEGOCIANT, LocalDate.of(2026, 9, 1), STOCK))
                .isInstanceOf(RegleEnfreinte.class).hasMessageContaining("juillet 2026 est en cours");
        monde.gererDsm().supprimer(AGENT_BNIA, NEGOCIANT, juillet);

        assertThatThrownBy(() -> cas.fixer(AGENT_BNIA, NEGOCIANT, JUIN, STOCK))
                .isInstanceOf(RegleEnfreinte.class).hasMessageContaining("après le 30/06/2026");
        GererPointDeDepart.PointDeDepart nouveau = cas.fixer(AGENT_BNIA, NEGOCIANT, LocalDate.of(2027, 5, 1), STOCK);

        assertThat(cas.consulter(AGENT_BNIA, NEGOCIANT).orElseThrow().point().id()).isEqualTo(nouveau.point().id());
        assertThat(cas.historique(OBSERVATEUR_NEGOCIANT, NEGOCIANT)).extracting(p -> p.point().debut(),
                GererPointDeDepart.PointDeDepart::fige).containsExactly(
                        org.assertj.core.groups.Tuple.tuple(LocalDate.of(2027, 5, 1), false),
                        org.assertj.core.groups.Tuple.tuple(JUIN, true));
        assertThat(monde.journal.deLOperateur(NEGOCIANT).getFirst().detail()).isEqualTo("Nouveau point de départ : "
                + "Début au 01/05/2027, 2 lignes de stock initial, après une interruption du 01/07/2026 au 30/04/2027");
        assertThat(monde.gererDsm().situation(AGENT_NEGOCIANT, NEGOCIANT).periode().orElseThrow().libelle())
                .as("la période suivante commence au nouveau point de départ").isEqualTo("mai 2027");
    }

    @Test
    @DisplayName("RG-DEC-09 : le stock proposé est le stock de fin de la dernière DSM validée, vieilli à la date retenue")
    void rg_dec_09_proposition() {
        cas.fixer(AGENT_BNIA, NEGOCIANT, JUIN, STOCK);
        assertThat(cas.proposer(AGENT_BNIA, NEGOCIANT, java.util.Optional.empty()))
                .as("sans DSM validée, le point de départ se modifie").isEmpty();
        juinValidee();

        GererPointDeDepart.Proposition lendemain = cas.proposer(AGENT_BNIA, NEGOCIANT, java.util.Optional.empty())
                .orElseThrow();
        assertThat(lendemain.debut()).isEqualTo(LocalDate.of(2026, 7, 1));
        assertThat(lendemain.derniere().libelle()).isEqualTo("juin 2026");
        assertThat(lendemain.lignes()).extracting(l -> l.identite().compte().toString()).containsExactly("12", "15");

        GererPointDeDepart.Proposition apres = cas.proposer(AGENT_BNIA, NEGOCIANT,
                java.util.Optional.of(LocalDate.of(2028, 5, 1))).orElseThrow();
        assertThat(apres.lignes()).extracting(l -> l.identite().compte().toString())
                .as("XO +2 (avril 2027 et 2028), MB 2010 recalculé").containsExactly("14", "17");
        assertThat(apres.lignes()).extracting(l -> l.volume().toString()).containsExactly("1500.00", "640.25");

        for (Acteur refuse : List.of(AGENT_NEGOCIANT, OBSERVATEUR_BNIA)) {
            assertThatThrownBy(() -> cas.proposer(refuse, NEGOCIANT, java.util.Optional.empty()))
                    .isInstanceOf(AccesRefuse.class);
        }
        assertThatThrownBy(() -> cas.historique(Monde.INCONNU, NEGOCIANT)).isInstanceOf(AccesRefuse.class);
    }
}
