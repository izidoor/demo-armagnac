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
import fr.bnia.declarations.domain.CleConditionnement;
import fr.bnia.declarations.domain.ColonneDsm;
import fr.bnia.declarations.domain.Commentaire;
import fr.bnia.declarations.domain.Cote;
import fr.bnia.declarations.domain.DeclarationStocksMouvements;
import fr.bnia.declarations.domain.EtatDsm;
import fr.bnia.declarations.domain.LigneDsm;
import fr.bnia.declarations.domain.OrigineLigne;
import fr.bnia.declarations.domain.SaisieDsm;
import fr.bnia.declarations.domain.SaisieLigneDsm;
import fr.bnia.declarations.domain.TypeDeclarant;
import fr.bnia.declarations.domain.TypeMouvement;
import fr.bnia.declarations.domain.TypePiece;
import fr.bnia.declarations.domain.partage.ComplementsDemandes;
import fr.bnia.declarations.domain.partage.DsmEnvoyee;
import fr.bnia.declarations.domain.partage.DsmValidee;
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

/** Envoi, instruction, validation, commentaires, listing du BNIA, DSM suivante (spec 05, lot 2). */
class InstruireDsmTest {

    private static final FiltreDsm TOUT = new FiltreDsm(Optional.empty(), Optional.empty(), Optional.empty(),
            Set.of());

    private final Monde monde = new Monde();
    private final GererDsm cas = monde.gererDsm();
    private final CommenterDsm commenter = monde.commenterDsm();
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
    @DisplayName("RG-DEC-20 : cycle complet, compléments reçus validés sans reprise, journal, événements, motif en commentaire")
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
        assertThat(commenter.lister(AGENT_NEGOCIANT, NEGOCIANT, id)).singleElement()
                .satisfies(c -> assertThat(c.motif()).isTrue())
                .satisfies(c -> assertThat(c.cote()).isEqualTo(Cote.BNIA));
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
                ActionDsm.ENREGISTRER, ActionDsm.ENVOYER, ActionDsm.SUPPRIMER, ActionDsm.COMMENTER,
                ActionDsm.DEPOSER_PIECES);
        assertThat(cas.consulter(AGENT_BNIA, NEGOCIANT, id).actions()).containsExactlyInAnyOrder(
                ActionDsm.SUPPRIMER, ActionDsm.COMMENTER, ActionDsm.COMMENTER_INTERNE, ActionDsm.DEPOSER_PIECES);
        assertThat(cas.consulter(OBSERVATEUR_NEGOCIANT, NEGOCIANT, id).actions()).isEmpty();
        cas.envoyer(AGENT_NEGOCIANT, NEGOCIANT, id, null);
        assertThat(cas.consulter(AGENT_NEGOCIANT, NEGOCIANT, id).actions()).containsExactly(ActionDsm.COMMENTER);
        assertThat(cas.consulter(AGENT_BNIA, NEGOCIANT, id).actions()).containsExactlyInAnyOrder(
                ActionDsm.PRENDRE_EN_INSTRUCTION, ActionDsm.COMMENTER, ActionDsm.COMMENTER_INTERNE,
                ActionDsm.DEPOSER_PIECES);
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
    @DisplayName("RG-DEC-01 : tout lecteur lit les commentaires ; l'opérateur ne voit pas les internes ; l'observateur n'écrit pas")
    void rg_dec_01_commentaires() {
        commenter.ecrire(AGENT_NEGOCIANT, NEGOCIANT, id, null, "Transfert prévu", false);
        commenter.ecrire(AGENT_BNIA, NEGOCIANT, id, null, "À vérifier avec les douanes", true);
        assertThat(commenter.lister(OBSERVATEUR_NEGOCIANT, NEGOCIANT, id)).extracting(Commentaire::texte)
                .containsExactly("Transfert prévu");
        assertThat(commenter.lister(OBSERVATEUR_BNIA, NEGOCIANT, id)).hasSize(2);
        refuse(() -> commenter.lister(INCONNU, NEGOCIANT, id));
        refuse(() -> commenter.ecrire(OBSERVATEUR_NEGOCIANT, NEGOCIANT, id, null, "Note", false));
        refuse(() -> commenter.ecrire(OBSERVATEUR_BNIA, NEGOCIANT, id, null, "Note", false));
    }

    @Test
    @DisplayName("RG-DEC-24 : un commentaire vise la DSM ou une de ses lignes, dans tout état ; l'opérateur n'écrit pas d'interne")
    void rg_dec_24_commenter() {
        long ligne = dsm().lignes().getFirst().id();
        Commentaire surLigne = commenter.ecrire(AGENT_NEGOCIANT, NEGOCIANT, id, ligne, "Transfert vers Eauze", false);
        assertThat(surLigne.ligne()).isEqualTo(ligne);
        assertThat(surLigne.cote()).isEqualTo(Cote.OPERATEUR);
        enfreint("RG-DEC-24", () -> commenter.ecrire(AGENT_NEGOCIANT, NEGOCIANT, id, null, "Note", true));
        assertThatThrownBy(() -> commenter.ecrire(AGENT_NEGOCIANT, NEGOCIANT, id, 9999L, "Note", false))
                .isInstanceOf(ElementIntrouvable.class);
        cas.envoyer(AGENT_NEGOCIANT, NEGOCIANT, id, null);
        cas.prendreEnInstruction(AGENT_BNIA, NEGOCIANT, id, null);
        cas.valider(AGENT_BNIA, NEGOCIANT, id, null);
        commenter.ecrire(AGENT_NEGOCIANT, NEGOCIANT, id, null, "Merci", false);
        assertThat(commenter.lister(AGENT_NEGOCIANT, NEGOCIANT, id)).hasSize(2);
    }

    @Test
    @DisplayName("RG-DEC-25 : l'auteur modifie ou supprime le sien, trace au journal ; le motif reste")
    void rg_dec_25_modifier_supprimer() {
        long commentaire = commenter.ecrire(AGENT_NEGOCIANT, NEGOCIANT, id, null, "Note", false).id();
        enfreint("RG-DEC-25", () -> commenter.modifier(AGENT_BNIA, NEGOCIANT, id, commentaire, "Autre", false));
        assertThat(commenter.modifier(AGENT_NEGOCIANT, NEGOCIANT, id, commentaire, "Note corrigée", false).modifieLe())
                .isNotNull();
        enfreint("RG-DEC-25", () -> commenter.supprimer(AGENT_BNIA, NEGOCIANT, id, commentaire));
        commenter.supprimer(AGENT_NEGOCIANT, NEGOCIANT, id, commentaire);
        assertThat(commenter.lister(AGENT_NEGOCIANT, NEGOCIANT, id)).isEmpty();
        assertThat(monde.journal.deLaDsm(id).getFirst().action()).isEqualTo(ActionJournalDeclarations.COMMENTAIRE_SUPPRIME);

        cas.envoyer(AGENT_NEGOCIANT, NEGOCIANT, id, null);
        cas.prendreEnInstruction(AGENT_BNIA, NEGOCIANT, id, null);
        cas.demanderComplements(AGENT_BNIA, NEGOCIANT, id, "DAE manquants", null);
        long motif = commenter.lister(AGENT_BNIA, NEGOCIANT, id).getFirst().id();
        enfreint("RG-DEC-25", () -> commenter.supprimer(AGENT_BNIA, NEGOCIANT, id, motif));
    }

    @Test
    @DisplayName("RG-DEC-22 : supprimer un brouillon efface ses commentaires")
    void rg_dec_22_commentaires_supprimes() {
        commenter.ecrire(AGENT_NEGOCIANT, NEGOCIANT, id, null, "Note", false);
        cas.supprimer(AGENT_NEGOCIANT, NEGOCIANT, id);
        assertThat(monde.commentaires.deLaDsm(id)).isEmpty();
    }

    @Test
    void les_commentaires_d_une_ligne_supprimee_passent_sur_la_dsm() {
        List<SaisieLigneDsm> lignes = new ArrayList<>(dsm().lignes().stream().map(l -> telle(l, Map.of())).toList());
        lignes.add(new SaisieLigneDsm(null, CleConditionnement.NB, null, "Réserve", "8", Map.of()));
        DeclarationStocksMouvements avecAjout = cas.enregistrer(AGENT_NEGOCIANT, NEGOCIANT, id,
                new SaisieDsm(null, lignes), null).dsm();
        long ajoutee = avecAjout.lignes().getLast().id();
        long gardee = avecAjout.lignes().getFirst().id();
        commenter.ecrire(AGENT_BNIA, NEGOCIANT, id, ajoutee, "Quelle origine ?", false);
        commenter.ecrire(AGENT_BNIA, NEGOCIANT, id, gardee, "Vu", false);

        cas.enregistrer(AGENT_NEGOCIANT, NEGOCIANT, id, new SaisieDsm(null, lignes.subList(0, 2).stream()
                .map(s -> telle(avecAjout.lignes().get(lignes.indexOf(s)), Map.of())).toList()), null);

        List<Commentaire> fil = commenter.lister(AGENT_BNIA, NEGOCIANT, id);
        assertThat(fil.get(0).ligne()).isNull();
        assertThat(fil.get(0).ligneSupprimee()).isEqualTo("« dénomination Réserve, clé NB, compte d'âge 8 »");
        assertThat(fil.get(1).ligne()).isEqualTo(gardee);
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
        assertThat(lister.lister(AGENT_BNIA, enCours)).extracting(d -> d.dsm().id()).containsExactly(id);
        assertThat(lister.lister(AGENT_BNIA, enCours)).singleElement()
                .satisfies(d -> assertThat(d.prendreEnInstruction()).isTrue());
        assertThat(lister.lister(OBSERVATEUR_BNIA, enCours)).singleElement()
                .satisfies(d -> assertThat(d.prendreEnInstruction()).as("un observateur n'instruit pas").isFalse());
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
