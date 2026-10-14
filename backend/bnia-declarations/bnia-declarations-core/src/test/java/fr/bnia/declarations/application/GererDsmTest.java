package fr.bnia.declarations.application;

import static fr.bnia.declarations.application.Monde.AGENT_BNIA;
import static fr.bnia.declarations.application.Monde.AGENT_NEGOCIANT;
import static fr.bnia.declarations.application.Monde.INCONNU;
import static fr.bnia.declarations.application.Monde.NEGOCIANT;
import static fr.bnia.declarations.application.Monde.OBSERVATEUR_BNIA;
import static fr.bnia.declarations.application.Monde.OBSERVATEUR_NEGOCIANT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import fr.bnia.commun.domain.AccesRefuse;
import fr.bnia.commun.domain.ElementIntrouvable;
import fr.bnia.commun.domain.RegleEnfreinte;
import fr.bnia.commun.domain.VolumeAlcoolPur;
import fr.bnia.declarations.domain.ActionJournalDeclarations;
import fr.bnia.declarations.domain.ColonneDsm;
import fr.bnia.declarations.domain.DeclarationStocksMouvements;
import fr.bnia.declarations.domain.EtatDsm;
import fr.bnia.declarations.domain.LigneDsm;
import fr.bnia.declarations.domain.OrigineLigne;
import fr.bnia.declarations.domain.SaisieDsm;
import fr.bnia.declarations.domain.SaisieLigneDsm;
import fr.bnia.operateurs.domain.partage.Acteur;
import fr.bnia.operateurs.domain.partage.StatutDeclarationStock;
import fr.bnia.operateurs.domain.partage.TypeOperateur;
import java.util.List;
import java.util.Map;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class GererDsmTest {

    private final Monde monde = new Monde();
    private final GererDsm cas = monde.gererDsm();

    @BeforeEach
    void pointDeDepart() {
        monde.gererPointDeDepart().fixer(AGENT_BNIA, NEGOCIANT, GererPointDeDepartTest.JUIN, GererPointDeDepartTest.STOCK);
    }

    private static SaisieDsm saisie(DeclarationStocksMouvements dsm, Map<ColonneDsm, VolumeAlcoolPur> premiereLigne) {
        return new SaisieDsm(null, dsm.lignes().stream().map(l -> ligne(l, l == dsm.lignes().getFirst()
                ? premiereLigne : Map.of())).toList());
    }

    private static SaisieLigneDsm ligne(LigneDsm l, Map<ColonneDsm, VolumeAlcoolPur> volumes) {
        return new SaisieLigneDsm(l.id(), l.identite().cle(),
                l.identite().millesime() == null ? null : l.identite().millesime().annee(),
                l.identite().denomination() == null ? null : l.identite().denomination().texte(),
                l.identite().compte().toString(), volumes);
    }

    private static void refuse(ThrowingCallable appel) {
        assertThatThrownBy(appel).isInstanceOf(AccesRefuse.class)
                .extracting(e -> ((RegleEnfreinte) e).regle().orElseThrow()).isEqualTo("RG-DEC-01");
    }

    @Test
    @DisplayName("RG-DEC-01 : l'opérateur initie et saisit, par son propre rôle ; le BNIA ne saisit pas")
    void rg_dec_01_initier_et_saisir() {
        refuse(() -> cas.initier(AGENT_BNIA, NEGOCIANT));
        refuse(() -> cas.initier(OBSERVATEUR_NEGOCIANT, NEGOCIANT));
        refuse(() -> cas.initier(INCONNU, NEGOCIANT));
        DeclarationStocksMouvements dsm = cas.initier(AGENT_NEGOCIANT, NEGOCIANT).dsm();
        SaisieDsm saisie = saisie(dsm, Map.of());
        for (Acteur acteur : List.of(AGENT_BNIA, OBSERVATEUR_NEGOCIANT, INCONNU)) {
            refuse(() -> cas.enregistrer(acteur, NEGOCIANT, dsm.id(), saisie, null));
            refuse(() -> cas.apercevoir(acteur, NEGOCIANT, dsm.id(), saisie));
        }
        assertThatThrownBy(() -> cas.enregistrer(AGENT_BNIA, NEGOCIANT, dsm.id(), saisie, null))
                .hasMessageContaining("le BNIA l'instruit");
    }

    @Test
    @DisplayName("RG-DEC-01 : tout membre et le personnel du BNIA lisent ; un inconnu non")
    void rg_dec_01_lecture() {
        long id = cas.initier(AGENT_NEGOCIANT, NEGOCIANT).dsm().id();
        for (Acteur lecteur : List.of(OBSERVATEUR_NEGOCIANT, OBSERVATEUR_BNIA, AGENT_BNIA)) {
            assertThat(cas.consulter(lecteur, NEGOCIANT, id).dsm().id()).isEqualTo(id);
            assertThat(cas.lister(lecteur, NEGOCIANT)).hasSize(1);
            assertThat(cas.journal(lecteur, NEGOCIANT, id)).hasSize(1);
            assertThat(cas.situation(lecteur, NEGOCIANT).empechement()).isPresent();
        }
        refuse(() -> cas.consulter(INCONNU, NEGOCIANT, id));
        refuse(() -> cas.lister(INCONNU, NEGOCIANT));
        refuse(() -> cas.journal(INCONNU, NEGOCIANT, id));
        refuse(() -> cas.journalDeLOperateur(INCONNU, NEGOCIANT));
        refuse(() -> cas.denominations(INCONNU, NEGOCIANT));
        refuse(() -> cas.situation(INCONNU, NEGOCIANT));
        assertThatThrownBy(() -> cas.consulter(AGENT_NEGOCIANT, Monde.PRODUCTEUR, id))
                .as("une DSM se lit par son opérateur").isInstanceOf(ElementIntrouvable.class);
    }

    @Test
    @DisplayName("RG-DEC-01 : l'opérateur ou le BNIA qui gère supprime un brouillon ; un observateur non")
    void rg_dec_01_supprimer() {
        long id = cas.initier(AGENT_NEGOCIANT, NEGOCIANT).dsm().id();
        refuse(() -> cas.supprimer(OBSERVATEUR_NEGOCIANT, NEGOCIANT, id));
        refuse(() -> cas.supprimer(OBSERVATEUR_BNIA, NEGOCIANT, id));
        refuse(() -> cas.supprimer(INCONNU, NEGOCIANT, id));
        cas.supprimer(AGENT_BNIA, NEGOCIANT, id);
        assertThat(monde.dsms.parId(id)).isEmpty();
    }

    @Test
    @DisplayName("RG-DEC-02 : un opérateur au statut autre que OUI n'initie pas")
    void rg_dec_02_statut() {
        monde.operateur(NEGOCIANT, TypeOperateur.NEGOCIANT, StatutDeclarationStock.SOMMEIL);
        assertThatThrownBy(() -> cas.initier(AGENT_NEGOCIANT, NEGOCIANT)).isInstanceOf(RegleEnfreinte.class)
                .extracting(e -> ((RegleEnfreinte) e).regle().orElseThrow()).isEqualTo("RG-DEC-02");
    }

    @Test
    @DisplayName("RG-DEC-05 : une seule DSM non validée ; pas sans point de départ")
    void rg_dec_05_une_seule_dsm_non_validee() {
        cas.initier(AGENT_NEGOCIANT, NEGOCIANT);
        assertThatThrownBy(() -> cas.initier(AGENT_NEGOCIANT, NEGOCIANT)).isInstanceOf(RegleEnfreinte.class)
                .extracting(e -> ((RegleEnfreinte) e).regle().orElseThrow()).isEqualTo("RG-DEC-05");
        assertThatThrownBy(() -> cas.initier(AGENT_NEGOCIANT, Monde.PRODUCTEUR)).isInstanceOf(RegleEnfreinte.class)
                .hasMessageContaining("point de départ");
    }

    @Test
    @DisplayName("RG-DEC-13 : la DSM initiée reprend le stock initial du point de départ")
    void rg_dec_13_initiation() {
        VueDsm vue = cas.initier(AGENT_NEGOCIANT, NEGOCIANT);
        assertThat(vue.dsm().periode().libelle()).isEqualTo("juin 2026");
        assertThat(vue.dsm().etat()).isEqualTo(EtatDsm.BROUILLON);
        assertThat(vue.dsm().lignes()).hasSize(2).allMatch(l -> l.origine() == OrigineLigne.REPRISE && l.id() != null);
        assertThat(vue.dsm().total(ColonneDsm.C01)).isEqualTo(VolumeAlcoolPur.litres("2140.25"));
        assertThat(vue.operateur().code()).isEqualTo("N7");
    }

    @Test
    @DisplayName("RG-DEC-18 : enregistrer avec des anomalies ; la vue provisoire n'enregistre rien")
    void rg_dec_18_enregistrer_et_apercevoir() {
        DeclarationStocksMouvements dsm = cas.initier(AGENT_NEGOCIANT, NEGOCIANT).dsm();
        SaisieDsm negatif = saisie(dsm, Map.of(ColonneDsm.C17, VolumeAlcoolPur.litres("2000")));
        VueDsm apercu = cas.apercevoir(AGENT_NEGOCIANT, NEGOCIANT, dsm.id(), negatif);
        assertThat(apercu.anomalies()).hasSize(1);
        assertThat(cas.consulter(AGENT_NEGOCIANT, NEGOCIANT, dsm.id()).anomalies()).isEmpty();
        VueDsm enregistree = cas.enregistrer(AGENT_NEGOCIANT, NEGOCIANT, dsm.id(), negatif, 0L);
        assertThat(enregistree.anomalies()).hasSize(1);
        assertThat(enregistree.dsm().version()).isEqualTo(1);
        assertThat(monde.journal.actions()).containsExactly(ActionJournalDeclarations.POINT_DE_DEPART_FIXE, ActionJournalDeclarations.DSM_INITIEE,
                ActionJournalDeclarations.DSM_ENREGISTREE);
    }

    @Test
    @DisplayName("RG-DEC-22 : supprimer un brouillon efface son journal ; « brouillon supprimé » reste à l'opérateur")
    void rg_dec_22_suppression_en_cascade() {
        long id = cas.initier(AGENT_NEGOCIANT, NEGOCIANT).dsm().id();
        cas.supprimer(AGENT_NEGOCIANT, NEGOCIANT, id);
        assertThat(monde.journal.deLaDsm(id)).extracting(EntreeJournal::action)
                .containsExactly(ActionJournalDeclarations.BROUILLON_SUPPRIME);
        assertThat(cas.journalDeLOperateur(AGENT_NEGOCIANT, NEGOCIANT)).extracting(EntreeJournal::action)
                .containsExactly(ActionJournalDeclarations.BROUILLON_SUPPRIME, ActionJournalDeclarations.POINT_DE_DEPART_FIXE);
        assertThat(cas.situation(AGENT_NEGOCIANT, NEGOCIANT).initiable()).as("la période se réinitie").isTrue();
        assertThatThrownBy(() -> cas.consulter(AGENT_NEGOCIANT, NEGOCIANT, id)).isInstanceOf(ElementIntrouvable.class);
    }

    @Test
    @DisplayName("RG-DEC-22 : une DSM qui n'est plus en brouillon ne se supprime pas")
    void rg_dec_22_pas_apres_le_brouillon() {
        DeclarationStocksMouvements dsm = monde.dsms.parId(cas.initier(AGENT_NEGOCIANT, NEGOCIANT).dsm().id()).orElseThrow();
        monde.dsms.forcer(dsm, EtatDsm.ENVOYEE);
        assertThatThrownBy(() -> cas.supprimer(AGENT_BNIA, NEGOCIANT, dsm.id())).isInstanceOf(RegleEnfreinte.class)
                .extracting(e -> ((RegleEnfreinte) e).regle().orElseThrow()).isEqualTo("RG-DEC-22");
    }

    @Test
    @DisplayName("RG-DEC-23 : chaque action de la DSM au journal, avec l'acteur et la période copiés")
    void rg_dec_23_journal() {
        DeclarationStocksMouvements dsm = cas.initier(AGENT_NEGOCIANT, NEGOCIANT).dsm();
        cas.enregistrer(AGENT_NEGOCIANT, NEGOCIANT, dsm.id(), saisie(dsm, Map.of()), null);
        List<EntreeJournal> journal = cas.journal(AGENT_NEGOCIANT, NEGOCIANT, dsm.id());
        assertThat(journal).extracting(EntreeJournal::action).containsExactly(ActionJournalDeclarations.DSM_ENREGISTREE,
                ActionJournalDeclarations.DSM_INITIEE);
        assertThat(journal).allMatch(e -> e.periode().equals("juin 2026") && e.nomActeur().equals("Utilisateur 10"));
    }
}
