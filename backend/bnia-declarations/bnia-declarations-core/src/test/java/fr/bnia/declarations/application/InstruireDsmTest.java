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
import fr.bnia.commun.domain.ModificationConcurrente;
import fr.bnia.commun.domain.RegleEnfreinte;
import fr.bnia.commun.domain.VolumeAlcoolPur;
import fr.bnia.declarations.application.ListerDsm.DsmListee;
import fr.bnia.declarations.domain.ActionJournalDeclarations;
import fr.bnia.declarations.domain.ColonneDsm;
import fr.bnia.declarations.domain.NoteInterne;
import fr.bnia.declarations.domain.DeclarationStocksMouvements;
import fr.bnia.declarations.domain.LigneDsm;
import fr.bnia.declarations.domain.OrigineLigne;
import fr.bnia.declarations.domain.SaisieDsm;
import fr.bnia.declarations.domain.SaisieLigneDsm;
import fr.bnia.declarations.domain.TypeDeclarant;
import fr.bnia.declarations.domain.TypeMouvement;
import fr.bnia.declarations.domain.TypePiece;
import fr.bnia.declarations.domain.partage.CleConditionnement;
import fr.bnia.declarations.domain.partage.ComplementsDemandes;
import fr.bnia.declarations.domain.partage.Cote;
import fr.bnia.declarations.domain.partage.DsmEnvoyee;
import fr.bnia.declarations.domain.partage.DsmValidee;
import fr.bnia.declarations.domain.partage.EtatDsm;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Envoi, instruction, validation, notes internes, listing du BNIA, DSM suivante (spec 05, lot 2 ; spec 15, lot 1). */
class InstruireDsmTest {

    private static final FiltreDsm TOUT = new FiltreDsm(Optional.empty(), Optional.empty(), Optional.empty(),
            Set.of());

    private final Monde monde = new Monde();
    private final GererDsm cas = monde.gererDsm();
    private final AnnoterDsm annoter = monde.annoterDsm();
    private long id;

    @BeforeEach
    void dsmDeJuin() {
        monde.gererPointDeDepart().fixer(AGENT_BNIA, NEGOCIANT, GererPointDeDepartTest.JUIN, GererPointDeDepartTest.STOCK);
        id = cas.initier(AGENT_NEGOCIANT, NEGOCIANT).dsm().id();
    }

    private DeclarationStocksMouvements dsm() {
        return monde.dsms.parId(id).orElseThrow();
    }

    private static SaisieLigneDsm telle(LigneDsm l, Map<ColonneDsm, VolumeAlcoolPur> volumes) {
        return new SaisieLigneDsm(l.id(), l.identite().cle(),
                l.identite().millesime() == null ? null : l.identite().millesime().annee(),
                l.identite().denomination() == null ? null : l.identite().denomination().texte(),
                l.identite().compte().toString(), volumes);
    }

    private static void refuse(ThrowingCallable appel) {
        assertThatThrownBy(appel).isInstanceOf(AccesRefuse.class)
                .extracting(e -> ((RegleEnfreinte) e).regle().orElseThrow()).isEqualTo("RG-DEC-01");
    }

    private static void enfreint(String regle, ThrowingCallable appel) {
        assertThatThrownBy(appel).isInstanceOf(RegleEnfreinte.class)
                .extracting(e -> ((RegleEnfreinte) e).regle().orElseThrow()).isEqualTo(regle);
    }

    @Test
    @DisplayName("RG-DEC-01 : l'opérateur envoie ; le BNIA prend en instruction, demande des compléments, valide")
    void rg_dec_01_transitions_par_cote() {
        for (var acteur : List.of(AGENT_BNIA, OBSERVATEUR_NEGOCIANT, INCONNU)) {
            refuse(() -> cas.envoyer(acteur, NEGOCIANT, id, null));
        }
        cas.envoyer(AGENT_NEGOCIANT, NEGOCIANT, id, null);
        for (var acteur : List.of(AGENT_NEGOCIANT, OBSERVATEUR_BNIA, INCONNU)) {
            refuse(() -> cas.prendreEnInstruction(acteur, NEGOCIANT, id, null));
        }
        cas.prendreEnInstruction(AGENT_BNIA, NEGOCIANT, id, null);
        for (var acteur : List.of(AGENT_NEGOCIANT, OBSERVATEUR_BNIA, INCONNU)) {
            refuse(() -> cas.demanderComplements(acteur, NEGOCIANT, id, "Motif", null));
            refuse(() -> cas.valider(acteur, NEGOCIANT, id, null));
        }
    }

    @Test
    @DisplayName("RG-DEC-20, RG-DEC-25 : cycle complet, compléments reçus validés sans reprise, journal, événements, motif déposé")
    void rg_dec_20_cycle_complet() {
        assertThat(cas.envoyer(AGENT_NEGOCIANT, NEGOCIANT, id, 0L).dsm().etat()).isEqualTo(EtatDsm.ENVOYEE);
        cas.prendreEnInstruction(AGENT_BNIA, NEGOCIANT, id, 1L);
        enfreint("RG-DEC-20", () -> cas.demanderComplements(AGENT_BNIA, NEGOCIANT, id, "  ", 2L));
        assertThat(dsm().etat()).isEqualTo(EtatDsm.EN_INSTRUCTION);
        cas.demanderComplements(AGENT_BNIA, NEGOCIANT, id, "DAE manquants pour l'export", 2L);
        assertThat(cas.envoyer(AGENT_NEGOCIANT, NEGOCIANT, id, 3L).dsm().etat()).isEqualTo(EtatDsm.COMPLEMENTS_RECUS);
        VueDsm recus = cas.consulter(AGENT_BNIA, NEGOCIANT, id);
        assertThat(recus.actions()).contains(ActionDsm.DEMANDER_COMPLEMENTS, ActionDsm.VALIDER);
        assertThat(recus.cote()).isEqualTo(Cote.BNIA);
        assertThat(cas.consulter(AGENT_NEGOCIANT, NEGOCIANT, id).cote()).isEqualTo(Cote.OPERATEUR);
        VueDsm validee = cas.valider(AGENT_BNIA, NEGOCIANT, id, 4L);

        assertThat(validee.dsm().etat()).isEqualTo(EtatDsm.VALIDEE);
        assertThat(validee.dsm().valideePar().nom()).isEqualTo("Utilisateur 20");
        assertThat(monde.journal.deLaDsm(id)).extracting(EntreeJournal::action).containsExactly(
                ActionJournalDeclarations.DSM_VALIDEE,
                ActionJournalDeclarations.DSM_ENVOYEE, ActionJournalDeclarations.COMPLEMENTS_DEMANDES,
                ActionJournalDeclarations.DSM_PRISE_EN_INSTRUCTION, ActionJournalDeclarations.DSM_ENVOYEE,
                ActionJournalDeclarations.DSM_INITIEE);
        LocalDate juin = GererPointDeDepartTest.JUIN;
        LocalDate fin = LocalDate.of(2026, 6, 30);
        assertThat(monde.evenements.publies).containsExactly(
                new DsmEnvoyee(id, NEGOCIANT, juin, fin, false),
                new ComplementsDemandes(id, NEGOCIANT, juin, fin, "DAE manquants pour l'export"),
                new DsmEnvoyee(id, NEGOCIANT, juin, fin, true),
                new DsmValidee(id, NEGOCIANT, juin, fin));
        assertThat(monde.messagerie.motifs).containsExactly(
                new MessagerieCapturee.Motif(id, AGENT_BNIA.utilisateur(), "DAE manquants pour l'export"));
    }

    @Test
    @DisplayName("RG-DEC-20 : une transition sur une version périmée est refusée")
    void rg_dec_20_version() {
        assertThatThrownBy(() -> cas.envoyer(AGENT_NEGOCIANT, NEGOCIANT, id, 7L))
                .isInstanceOf(ModificationConcurrente.class);
    }

    @Test
    @DisplayName("RG-DEC-21 : après l'envoi, l'opérateur ne modifie plus sa saisie")
    void rg_dec_21_saisie_figee() {
        cas.envoyer(AGENT_NEGOCIANT, NEGOCIANT, id, null);
        SaisieDsm saisie = new SaisieDsm(null, dsm().lignes().stream().map(l -> telle(l, Map.of())).toList());
        enfreint("RG-DEC-21", () -> cas.enregistrer(AGENT_NEGOCIANT, NEGOCIANT, id, saisie, null));
    }

    @Test
    @DisplayName("RG-DEC-01 : les actions de la vue suivent l'état et le côté")
    void rg_dec_01_actions() {
        assertThat(cas.consulter(AGENT_NEGOCIANT, NEGOCIANT, id).actions()).containsExactlyInAnyOrder(
                ActionDsm.ENREGISTRER, ActionDsm.ENVOYER, ActionDsm.SUPPRIMER, ActionDsm.DEPOSER_PIECES);
        assertThat(cas.consulter(AGENT_BNIA, NEGOCIANT, id).actions()).containsExactlyInAnyOrder(
                ActionDsm.SUPPRIMER, ActionDsm.ANNOTER, ActionDsm.DEPOSER_PIECES);
        assertThat(cas.consulter(OBSERVATEUR_NEGOCIANT, NEGOCIANT, id).actions()).isEmpty();
        cas.envoyer(AGENT_NEGOCIANT, NEGOCIANT, id, null);
        assertThat(cas.consulter(AGENT_NEGOCIANT, NEGOCIANT, id).actions()).isEmpty();
        assertThat(cas.consulter(AGENT_BNIA, NEGOCIANT, id).actions()).containsExactlyInAnyOrder(
                ActionDsm.PRENDRE_EN_INSTRUCTION, ActionDsm.ANNOTER, ActionDsm.DEPOSER_PIECES);
        cas.prendreEnInstruction(AGENT_BNIA, NEGOCIANT, id, null);
        assertThat(cas.consulter(AGENT_BNIA, NEGOCIANT, id).actions()).contains(ActionDsm.DEMANDER_COMPLEMENTS,
                ActionDsm.VALIDER);
    }

    @Test
    @DisplayName("RG-DEC-14 : la DSM suivante reprend le stock de fin de la DSM validée")
    void rg_dec_14_dsm_suivante() {
        DeclarationStocksMouvements juin = dsm();
        // XO vendu en entier : la ligne n'est pas reprise.
        cas.enregistrer(AGENT_NEGOCIANT, NEGOCIANT, id, new SaisieDsm(null, List.of(
                telle(juin.lignes().get(0), Map.of(ColonneDsm.C17, VolumeAlcoolPur.litres("1500"))),
                telle(juin.lignes().get(1), Map.of(ColonneDsm.C22, VolumeAlcoolPur.litres("40.25"))))), null);
        cas.envoyer(AGENT_NEGOCIANT, NEGOCIANT, id, null);
        cas.prendreEnInstruction(AGENT_BNIA, NEGOCIANT, id, null);
        monde.gererPieces().deposerJustificatif(AGENT_BNIA, NEGOCIANT, id, null, TypeMouvement.VENTE_EXPORT,
                TypePiece.DAE, null, new FichierDepose("dae.pdf", new byte[] {1}));
        cas.valider(AGENT_BNIA, NEGOCIANT, id, null);

        VueDsm juillet = cas.initier(AGENT_NEGOCIANT, NEGOCIANT);

        assertThat(juillet.dsm().periode().libelle()).isEqualTo("juillet 2026");
        assertThat(juillet.dsm().lignes()).singleElement().satisfies(l -> {
            assertThat(l.identite().cle()).isEqualTo(CleConditionnement.MB);
            assertThat(l.origine()).isEqualTo(OrigineLigne.REPRISE);
            assertThat(l.stockDebut()).isEqualTo(VolumeAlcoolPur.litres("600"));
        });
    }

    @Test
    @DisplayName("RG-DEC-01, 24 : les notes internes se lisent par le personnel du BNIA, s'écrivent par qui gère au BNIA")
    void rg_dec_01_notes_internes() {
        annoter.ecrire(AGENT_BNIA, NEGOCIANT, id, null, "À vérifier avec les douanes");
        assertThat(annoter.lister(OBSERVATEUR_BNIA, NEGOCIANT, id)).extracting(NoteInterne::texte)
                .containsExactly("À vérifier avec les douanes");
        assertThatThrownBy(() -> annoter.lister(AGENT_NEGOCIANT, NEGOCIANT, id)).isInstanceOf(AccesRefuse.class)
                .hasMessage("Les notes internes sont réservées au personnel du BNIA.");
        refuse(() -> annoter.lister(INCONNU, NEGOCIANT, id));
        refuse(() -> annoter.ecrire(AGENT_NEGOCIANT, NEGOCIANT, id, null, "Note"));
        refuse(() -> annoter.ecrire(OBSERVATEUR_BNIA, NEGOCIANT, id, null, "Note"));
    }

    @Test
    @DisplayName("RG-DEC-24 : une note vise la DSM ou une de ses lignes, dans tout état ; son auteur seul la modifie ou la supprime")
    void rg_dec_24_annoter() {
        long ligne = dsm().lignes().getFirst().id();
        NoteInterne surLigne = annoter.ecrire(AGENT_BNIA, NEGOCIANT, id, ligne, "Sorties export à rapprocher des DAE");
        assertThat(surLigne.ligne()).isEqualTo(ligne);
        assertThatThrownBy(() -> annoter.ecrire(AGENT_BNIA, NEGOCIANT, id, 9999L, "Note"))
                .isInstanceOf(ElementIntrouvable.class);
        cas.envoyer(AGENT_NEGOCIANT, NEGOCIANT, id, null);
        cas.prendreEnInstruction(AGENT_BNIA, NEGOCIANT, id, null);
        cas.valider(AGENT_BNIA, NEGOCIANT, id, null);
        annoter.ecrire(AGENT_BNIA, NEGOCIANT, id, null, "Validée après rapprochement");
        assertThat(annoter.lister(AGENT_BNIA, NEGOCIANT, id)).hasSize(2);
        assertThat(annoter.modifier(AGENT_BNIA, NEGOCIANT, id, surLigne.id(), "DAE reçus, conforme").modifieeLe())
                .isNotNull();
        annoter.supprimer(AGENT_BNIA, NEGOCIANT, id, surLigne.id());
        assertThat(annoter.lister(AGENT_BNIA, NEGOCIANT, id)).singleElement()
                .satisfies(n -> assertThat(n.texte()).isEqualTo("Validée après rapprochement"));
        assertThat(monde.journal.deLaDsm(id)).as("le journal, que l'opérateur lit, ignore les notes")
                .extracting(EntreeJournal::action).containsExactly(ActionJournalDeclarations.DSM_VALIDEE,
                        ActionJournalDeclarations.DSM_PRISE_EN_INSTRUCTION, ActionJournalDeclarations.DSM_ENVOYEE,
                        ActionJournalDeclarations.DSM_INITIEE);
    }

    @Test
    @DisplayName("RG-DEC-22 : supprimer un brouillon efface ses notes internes et sa conversation")
    void rg_dec_22_notes_et_conversation_supprimees() {
        annoter.ecrire(AGENT_BNIA, NEGOCIANT, id, null, "Note");
        cas.supprimer(AGENT_NEGOCIANT, NEGOCIANT, id);
        assertThat(monde.notes.deLaDsm(id)).isEmpty();
        assertThat(monde.messagerie.supprimees).containsExactly(id);
    }

    @Test
    void les_notes_d_une_ligne_supprimee_passent_sur_la_dsm() {
        List<SaisieLigneDsm> lignes = new ArrayList<>(dsm().lignes().stream().map(l -> telle(l, Map.of())).toList());
        lignes.add(new SaisieLigneDsm(null, CleConditionnement.NB, null, "Réserve", "8", Map.of()));
        DeclarationStocksMouvements avecAjout = cas.enregistrer(AGENT_NEGOCIANT, NEGOCIANT, id,
                new SaisieDsm(null, lignes), null).dsm();
        long ajoutee = avecAjout.lignes().getLast().id();
        long gardee = avecAjout.lignes().getFirst().id();
        annoter.ecrire(AGENT_BNIA, NEGOCIANT, id, ajoutee, "Quelle origine ?");
        annoter.ecrire(AGENT_BNIA, NEGOCIANT, id, gardee, "Vu");

        cas.enregistrer(AGENT_NEGOCIANT, NEGOCIANT, id, new SaisieDsm(null, lignes.subList(0, 2).stream()
                .map(s -> telle(avecAjout.lignes().get(lignes.indexOf(s)), Map.of())).toList()), null);

        List<NoteInterne> carnet = annoter.lister(AGENT_BNIA, NEGOCIANT, id);
        assertThat(carnet.get(0).ligne()).isNull();
        assertThat(carnet.get(0).ligneSupprimee()).isEqualTo("« dénomination Réserve, clé NB, compte d'âge 8 »");
        assertThat(carnet.get(1).ligne()).isEqualTo(gardee);
    }

    @Test
    @DisplayName("RG-DEC-01 : le listing de toutes les DSM est réservé au personnel du BNIA, filtrable")
    void rg_dec_01_listing_du_bnia() {
        ListerDsm lister = monde.listerDsm();
        refuse(() -> lister.lister(AGENT_NEGOCIANT, TOUT));
        refuse(() -> lister.lister(INCONNU, TOUT));
        assertThat(lister.lister(OBSERVATEUR_BNIA, TOUT)).singleElement()
                .satisfies(d -> assertThat(d.code()).isEqualTo("N7"));
        cas.envoyer(AGENT_NEGOCIANT, NEGOCIANT, id, null);
        FiltreDsm enCours = new FiltreDsm(Optional.empty(), Optional.empty(), Optional.empty(),
                Set.of(EtatDsm.BROUILLON, EtatDsm.ENVOYEE));
        assertThat(lister.lister(AGENT_BNIA, enCours)).extracting(d -> d.dsm().orElseThrow().id()).containsExactly(id);
        FiltreDsm validees = new FiltreDsm(Optional.empty(), Optional.empty(), Optional.empty(),
                Set.of(EtatDsm.VALIDEE));
        assertThat(lister.lister(AGENT_BNIA, validees)).isEmpty();
        FiltreDsm juillet = new FiltreDsm(Optional.of(NEGOCIANT), Optional.of(TypeDeclarant.NEGOCIANT),
                Optional.of(YearMonth.of(2026, 7)), Set.of());
        assertThat(lister.lister(AGENT_BNIA, juillet)).isEmpty();
        FiltreDsm juin = new FiltreDsm(Optional.of(NEGOCIANT), Optional.empty(), Optional.of(YearMonth.of(2026, 6)),
                Set.of());
        assertThat(lister.lister(AGENT_BNIA, juin)).extracting(DsmListee::raisonSociale).containsExactly("Opérateur 7");
    }
}
