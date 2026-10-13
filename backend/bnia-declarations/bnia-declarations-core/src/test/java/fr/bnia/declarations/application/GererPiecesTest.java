package fr.bnia.declarations.application;

import static fr.bnia.declarations.application.Monde.AGENT_BNIA;
import static fr.bnia.declarations.application.Monde.AGENT_NEGOCIANT;
import static fr.bnia.declarations.application.Monde.INCONNU;
import static fr.bnia.declarations.application.Monde.NEGOCIANT;
import static fr.bnia.declarations.application.Monde.OBSERVATEUR_BNIA;
import static fr.bnia.declarations.application.Monde.OBSERVATEUR_NEGOCIANT;
import static fr.bnia.declarations.application.Monde.PRODUCTEUR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import fr.bnia.commun.domain.AccesRefuse;
import fr.bnia.commun.domain.ElementIntrouvable;
import fr.bnia.commun.domain.RegleEnfreinte;
import fr.bnia.commun.domain.VolumeAlcoolPur;
import fr.bnia.declarations.application.GererPieces.MoisDrm;
import fr.bnia.declarations.domain.ActionJournalDeclarations;
import fr.bnia.declarations.domain.CleConditionnement;
import fr.bnia.declarations.domain.ColonneDsm;
import fr.bnia.declarations.domain.EtatDsm;
import fr.bnia.declarations.domain.Justificatif;
import fr.bnia.declarations.domain.LigneDsm;
import fr.bnia.declarations.domain.SaisieDsm;
import fr.bnia.declarations.domain.SaisieLigneDsm;
import fr.bnia.declarations.domain.SaisieLigneStock;
import fr.bnia.declarations.domain.TypeMouvement;
import fr.bnia.declarations.domain.TypePiece;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Justificatifs, DRM et manques (spec 05, lot 3). */
class GererPiecesTest {

    private static final FichierDepose PDF = new FichierDepose("dae.pdf", "%PDF-1.4".getBytes());

    private final Monde monde = new Monde();
    private final GererDsm dsms = monde.gererDsm();
    private final GererPieces cas = monde.gererPieces();
    private long id;

    @BeforeEach
    void dsmDeJuin() {
        monde.gererPointDeDepart().fixer(AGENT_BNIA, NEGOCIANT, GererPointDeDepartTest.JUIN, GererPointDeDepartTest.STOCK);
        id = dsms.initier(AGENT_NEGOCIANT, NEGOCIANT).dsm().id();
    }

    private static SaisieLigneDsm telle(LigneDsm l, Map<ColonneDsm, VolumeAlcoolPur> volumes) {
        return new SaisieLigneDsm(l.id(), l.identite().cle(),
                l.identite().millesime() == null ? null : l.identite().millesime().annee(),
                l.identite().denomination() == null ? null : l.identite().denomination().texte(),
                l.identite().compte().toString(), volumes);
    }

    private Justificatif dae(Long ligne) {
        return cas.deposerJustificatif(AGENT_NEGOCIANT, NEGOCIANT, id, ligne, TypeMouvement.VENTE_EXPORT, TypePiece.DAE,
                "DAE-1", PDF);
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
    @DisplayName("RG-DEC-01 : l'observateur et l'inconnu ne déposent pas ; tout lecteur consulte et télécharge")
    void rg_dec_01_droits() {
        long piece = dae(null).id();
        refuse(() -> cas.deposerJustificatif(OBSERVATEUR_NEGOCIANT, NEGOCIANT, id, null, TypeMouvement.ACHAT,
                TypePiece.FACTURE, null, PDF));
        refuse(() -> cas.deposerJustificatif(OBSERVATEUR_BNIA, NEGOCIANT, id, null, TypeMouvement.ACHAT,
                TypePiece.FACTURE, null, PDF));
        refuse(() -> cas.supprimerJustificatif(OBSERVATEUR_NEGOCIANT, NEGOCIANT, id, piece));
        refuse(() -> cas.pieces(INCONNU, NEGOCIANT, id));
        refuse(() -> cas.telechargerJustificatif(INCONNU, NEGOCIANT, id, piece));
        assertThat(cas.pieces(OBSERVATEUR_NEGOCIANT, NEGOCIANT, id).justificatifs()).hasSize(1);
        assertThat(cas.telechargerJustificatif(OBSERVATEUR_BNIA, NEGOCIANT, id, piece).contenu())
                .isEqualTo(PDF.contenu());
    }

    @Test
    @DisplayName("RG-DEC-26, 23 : dépôt sur la DSM ou une ligne de la DSM, au journal ; suppression avec le document")
    void rg_dec_26_deposer_supprimer() {
        long ligne = monde.dsms.parId(id).orElseThrow().lignes().getFirst().id();
        Justificatif piece = dae(ligne);
        assertThat(piece.ligne()).isEqualTo(ligne);
        assertThat(piece.deposePar().nom()).isEqualTo("Utilisateur 10");
        assertThat(monde.journal.deLaDsm(id).getFirst().detail()).isEqualTo("DAE : dae.pdf");
        assertThatThrownBy(() -> dae(9999L)).isInstanceOf(ElementIntrouvable.class);
        enfreint("RG-DEC-26", () -> cas.deposerJustificatif(AGENT_NEGOCIANT, NEGOCIANT, id, null,
                TypeMouvement.VENTE_EXPORT, TypePiece.AVOIR, null, PDF));
        assertThat(monde.stockage.documents).hasSize(1);

        cas.supprimerJustificatif(AGENT_NEGOCIANT, NEGOCIANT, id, piece.id());

        assertThat(cas.pieces(AGENT_NEGOCIANT, NEGOCIANT, id).justificatifs()).isEmpty();
        assertThat(monde.stockage.documents).isEmpty();
        assertThat(monde.journal.deLaDsm(id).getFirst().action()).isEqualTo(ActionJournalDeclarations.JUSTIFICATIF_SUPPRIME);
    }

    @Test
    @DisplayName("RG-DEC-26 : le justificatif d'une ligne ajoutée puis supprimée passe sur la DSM, avec le libellé de la ligne")
    void rg_dec_26_ligne_supprimee() {
        List<SaisieLigneDsm> lignes = new ArrayList<>(monde.dsms.parId(id).orElseThrow().lignes().stream()
                .map(l -> telle(l, Map.of())).toList());
        lignes.add(new SaisieLigneDsm(null, CleConditionnement.NB, null, "Réserve", "8", Map.of()));
        VueDsm avecAjout = dsms.enregistrer(AGENT_NEGOCIANT, NEGOCIANT, id, new SaisieDsm(null, lignes), null);
        long ajoutee = avecAjout.dsm().lignes().getLast().id();
        dae(ajoutee);

        dsms.enregistrer(AGENT_NEGOCIANT, NEGOCIANT, id, new SaisieDsm(null, lignes.subList(0, lignes.size() - 1)),
                null);

        assertThat(cas.pieces(AGENT_NEGOCIANT, NEGOCIANT, id).justificatifs()).singleElement().satisfies(j -> {
            assertThat(j.ligne()).isNull();
            assertThat(j.ligneSupprimee()).contains("Réserve");
        });
    }

    @Test
    @DisplayName("RG-DEC-27 : DRM d'un mois de la période, une seule par mois, aucune pour un négociant")
    void rg_dec_27_drm() {
        monde.gererPointDeDepart().fixer(AGENT_BNIA, PRODUCTEUR, LocalDate.of(2025, 4, 1),
                List.of(new SaisieLigneStock(CleConditionnement.MB, 2010, null, null, VolumeAlcoolPur.litres("640"))));
        long producteur = dsms.initier(AGENT_NEGOCIANT, PRODUCTEUR).dsm().id();
        YearMonth mai = YearMonth.of(2025, 5);

        cas.deposerDrm(AGENT_NEGOCIANT, PRODUCTEUR, producteur, mai, PDF);

        enfreint("RG-DEC-27", () -> cas.deposerDrm(AGENT_NEGOCIANT, PRODUCTEUR, producteur, mai, PDF));
        enfreint("RG-DEC-27", () -> cas.deposerDrm(AGENT_NEGOCIANT, PRODUCTEUR, producteur, YearMonth.of(2026, 4), PDF));
        enfreint("RG-DEC-27", () -> cas.deposerDrm(AGENT_NEGOCIANT, NEGOCIANT, id, YearMonth.of(2026, 6), PDF));
        List<MoisDrm> mois = cas.pieces(AGENT_NEGOCIANT, PRODUCTEUR, producteur).mois();
        assertThat(mois).hasSize(12);
        assertThat(mois.get(1).drm()).isPresent();
        assertThat(mois.get(0).drm()).isEmpty();
        assertThat(monde.journal.deLaDsm(producteur).getFirst().detail()).isEqualTo("mai 2025");

        cas.supprimerDrm(AGENT_NEGOCIANT, PRODUCTEUR, producteur, mai);
        cas.deposerDrm(AGENT_NEGOCIANT, PRODUCTEUR, producteur, mai, PDF);
        assertThat(monde.stockage.documents).hasSize(1);
    }

    @Test
    @DisplayName("RG-DEC-28 : l'opérateur ne dépose plus après l'envoi ; le BNIA dépose pendant l'instruction, plus après validation")
    void rg_dec_28_etats() {
        dsms.envoyer(AGENT_NEGOCIANT, NEGOCIANT, id, null);
        enfreint("RG-DEC-28", () -> dae(null));
        Justificatif piece = cas.deposerJustificatif(AGENT_BNIA, NEGOCIANT, id, null, TypeMouvement.VENTE_EXPORT,
                TypePiece.DAE, null, PDF);
        dsms.prendreEnInstruction(AGENT_BNIA, NEGOCIANT, id, null);
        dsms.valider(AGENT_BNIA, NEGOCIANT, id, null);
        enfreint("RG-DEC-28", () -> cas.supprimerJustificatif(AGENT_BNIA, NEGOCIANT, id, piece.id()));
        assertThat(dsms.consulter(AGENT_BNIA, NEGOCIANT, id).actions()).doesNotContain(ActionDsm.DEPOSER_PIECES);
    }

    @Test
    @DisplayName("RG-DEC-29 : les manques sont dans la vue ; la validation est refusée tant qu'il en reste")
    void rg_dec_29_validation() {
        LigneDsm premiere = monde.dsms.parId(id).orElseThrow().lignes().getFirst();
        List<SaisieLigneDsm> lignes = new ArrayList<>(monde.dsms.parId(id).orElseThrow().lignes().stream()
                .map(l -> telle(l, Map.of())).toList());
        lignes.set(0, telle(premiere, Map.of(ColonneDsm.C22, VolumeAlcoolPur.litres("40"))));
        VueDsm vue = dsms.enregistrer(AGENT_NEGOCIANT, NEGOCIANT, id, new SaisieDsm(null, lignes), null);
        assertThat(vue.manques()).singleElement()
                .satisfies(m -> assertThat(m.typeMouvement()).isEqualTo(TypeMouvement.VENTE_EXPORT));
        dsms.envoyer(AGENT_NEGOCIANT, NEGOCIANT, id, null);
        dsms.prendreEnInstruction(AGENT_BNIA, NEGOCIANT, id, null);

        enfreint("RG-DEC-29", () -> dsms.valider(AGENT_BNIA, NEGOCIANT, id, null));

        cas.deposerJustificatif(AGENT_BNIA, NEGOCIANT, id, premiere.id(), TypeMouvement.VENTE_EXPORT, TypePiece.DAE,
                null, PDF);
        assertThat(dsms.valider(AGENT_BNIA, NEGOCIANT, id, null).dsm().etat()).isEqualTo(EtatDsm.VALIDEE);
    }

    @Test
    @DisplayName("RG-DEC-22 : le brouillon supprimé emporte ses pièces et leurs documents")
    void rg_dec_22_suppression() {
        dae(null);
        dsms.supprimer(AGENT_NEGOCIANT, NEGOCIANT, id);
        assertThat(monde.stockage.documents).isEmpty();
    }
}
