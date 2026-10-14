package fr.bnia.declarations.application;

import static fr.bnia.declarations.application.Monde.AGENT_BNIA;
import static fr.bnia.declarations.application.Monde.AGENT_NEGOCIANT;
import static fr.bnia.declarations.application.Monde.INCONNU;
import static fr.bnia.declarations.application.Monde.NEGOCIANT;
import static fr.bnia.declarations.application.Monde.OBSERVATEUR_NEGOCIANT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

import fr.bnia.commun.domain.AccesRefuse;
import fr.bnia.commun.domain.RegleEnfreinte;
import fr.bnia.commun.domain.VolumeAlcoolPur;
import fr.bnia.declarations.application.EditerDsm.DocumentEdite;
import fr.bnia.declarations.application.GererDsm.DsmImportee;
import fr.bnia.declarations.application.GererPointDeDepart.PointImporte;
import fr.bnia.declarations.domain.ActionJournalDeclarations;
import fr.bnia.declarations.domain.ColonneDsm;
import fr.bnia.declarations.domain.Commentaire;
import fr.bnia.declarations.domain.DeclarationStocksMouvements;
import fr.bnia.declarations.domain.Fichiers;
import fr.bnia.declarations.domain.ImportRefuse;
import fr.bnia.declarations.domain.LigneDsm;
import fr.bnia.declarations.domain.LigneStockInitial;
import fr.bnia.declarations.domain.OrigineLigne;
import fr.bnia.declarations.domain.SaisieDsm;
import fr.bnia.declarations.domain.SaisieLigneDsm;
import fr.bnia.declarations.domain.partage.CleConditionnement;
import fr.bnia.operateurs.domain.partage.Acteur;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Le modèle Excel (spec 05, lot 6) : édition, import dans une DSM, import d'un stock initial. */
class ImporterExcelTest {

    private static final YearMonth JUIN = YearMonth.of(2026, 6);

    private final Monde monde = new Monde();
    private final GererDsm gerer = monde.gererDsm();
    private final GererPointDeDepart points = monde.gererPointDeDepart();
    private final EditerDsm editer = new EditerDsm(monde.dsms, edition -> new byte[0], monde.classeurs, monde.journal,
            monde.grilles, monde.factures, monde.operateurs, monde.referentiels, Monde.PARAMETRES, Monde.HORLOGE);
    private DeclarationStocksMouvements dsm;

    @BeforeEach
    void dsmDeJuin() {
        points.fixer(AGENT_BNIA, NEGOCIANT, GererPointDeDepartTest.JUIN, GererPointDeDepartTest.STOCK);
        dsm = gerer.initier(AGENT_NEGOCIANT, NEGOCIANT).dsm();
    }

    private FichierDepose fichier(String nom, Fichiers fichier) {
        return new FichierDepose(nom, monde.classeurs.deposer(nom, fichier.classeur()));
    }

    private static void refuse(ThrowingCallable appel) {
        assertThatThrownBy(appel).isInstanceOf(AccesRefuse.class)
                .extracting(e -> ((RegleEnfreinte) e).regle().orElseThrow()).isEqualTo("RG-DEC-01");
    }

    @Test
    @DisplayName("RG-DEC-36, 23 : tout lecteur édite l'Excel, observateur compris ; l'édition est au journal")
    void rg_dec_36_excel() {
        DocumentEdite excel = editer.excel(OBSERVATEUR_NEGOCIANT, NEGOCIANT, dsm.id());

        assertThat(excel.nom()).isEqualTo("DSM-N7-2026-06.xlsx");
        assertThat(monde.classeurs.ecrits).singleElement().satisfies(e -> assertThat(e.dsm().id()).isEqualTo(dsm.id()));
        assertThat(monde.journal.deLaDsm(dsm.id()).getFirst()).satisfies(e -> {
            assertThat(e.action()).isEqualTo(ActionJournalDeclarations.DSM_EDITEE);
            assertThat(e.detail()).isEqualTo("Excel");
        });
        assertThatThrownBy(() -> editer.excel(INCONNU, NEGOCIANT, dsm.id())).isInstanceOf(AccesRefuse.class);
    }

    @Test
    @DisplayName("RG-DEC-38, 23 : l'opérateur importe ; lignes rapprochées, ajout absent supprimé avec ses commentaires "
            + "passés sur la DSM ; le journal garde le nom du fichier")
    void rg_dec_38_importer_dsm() {
        List<SaisieLigneDsm> lignes = new ArrayList<>();
        for (LigneDsm l : dsm.lignes()) {
            lignes.add(new SaisieLigneDsm(l.id(), l.identite().cle(),
                    l.identite().millesime() == null ? null : l.identite().millesime().annee(),
                    l.identite().denomination() == null ? null : l.identite().denomination().texte(),
                    l.identite().compte().toString(), Map.of()));
        }
        lignes.add(new SaisieLigneDsm(null, CleConditionnement.NV, null, "VSOP", "5", Map.of(ColonneDsm.C04, VolumeAlcoolPur.litres("10"))));
        DeclarationStocksMouvements saisie = gerer.enregistrer(AGENT_NEGOCIANT, NEGOCIANT, dsm.id(),
                new SaisieDsm(null, lignes), null).dsm();
        long ajoutee = saisie.lignes().getLast().id();
        Commentaire commentaire = monde.commenterDsm().ecrire(AGENT_NEGOCIANT, NEGOCIANT, dsm.id(), ajoutee, "Assemblage", false);

        DsmImportee importee = gerer.importer(AGENT_NEGOCIANT, NEGOCIANT, dsm.id(), fichier("juin.xlsx", Fichiers.du(JUIN)
                .ligne("XO", "NB", "12", Map.of(17, 300))
                .ligne("Blanche", "NB", "00", Map.of(4, 50, 26, 40))), saisie.version());

        assertThat(importee.vue().dsm().lignes())
                .extracting(l -> l.identite().libelle(), LigneDsm::origine, l -> l.volume(ColonneDsm.C26))
                .containsExactly(
                        tuple("« dénomination XO, clé NB, compte d'âge 12 »", OrigineLigne.REPRISE, VolumeAlcoolPur.litres("1200")),
                        tuple("« millésime 2010, clé MB, compte d'âge 15 »", OrigineLigne.REPRISE, VolumeAlcoolPur.litres("640.25")),
                        tuple("« dénomination Blanche, clé NB, compte d'âge 00 »", OrigineLigne.AJOUT, VolumeAlcoolPur.litres("50")));
        assertThat(importee.avertissements()).containsExactly("Ligne 19 du fichier (« dénomination Blanche, clé NB, compte"
                + " d'âge 00 ») : stock de fin 40,00 L dans le fichier, 50,00 L calculé ; le calcul prévaut.");
        assertThat(monde.commentaires.deLaDsm(dsm.id())).singleElement().satisfies(c -> {
            assertThat(c.id()).isEqualTo(commentaire.id());
            assertThat(c.ligne()).isNull();
        });
        assertThat(monde.journal.deLaDsm(dsm.id()).getFirst()).satisfies(e -> {
            assertThat(e.action()).isEqualTo(ActionJournalDeclarations.DSM_IMPORTEE);
            assertThat(e.detail()).isEqualTo("juin.xlsx");
        });
    }

    @Test
    @DisplayName("RG-DEC-37 : un fichier en erreur est refusé ; rien n'est importé ni journalisé")
    void rg_dec_37_refus() {
        FichierDepose errone = fichier("errone.xlsx", Fichiers.du(JUIN).ligne("XO", "NB", "12", Map.of(17, "beaucoup")));
        int journal = monde.journal.deLaDsm(dsm.id()).size();

        assertThatThrownBy(() -> gerer.importer(AGENT_NEGOCIANT, NEGOCIANT, dsm.id(), errone, null))
                .isInstanceOfSatisfying(ImportRefuse.class, e -> assertThat(e.erreurs()).hasSize(1));

        assertThat(gerer.consulter(AGENT_NEGOCIANT, NEGOCIANT, dsm.id()).dsm().total(ColonneDsm.C17).estNul()).isTrue();
        assertThat(monde.journal.deLaDsm(dsm.id())).hasSize(journal);
    }

    @Test
    @DisplayName("RG-DEC-01, 21 : seul l'opérateur importe, dans une DSM modifiable, avant toute lecture du fichier")
    void rg_dec_01_import_dsm_refus() {
        FichierDepose vide = fichier("vide.xlsx", Fichiers.du(JUIN));
        for (Acteur acteur : List.of(AGENT_BNIA, OBSERVATEUR_NEGOCIANT, INCONNU)) {
            refuse(() -> gerer.importer(acteur, NEGOCIANT, dsm.id(), vide, null));
        }
        gerer.envoyer(AGENT_NEGOCIANT, NEGOCIANT, dsm.id(), null);
        FichierDepose illisible = new FichierDepose("illisible.xlsx", new byte[] {1, 2});
        assertThatThrownBy(() -> gerer.importer(AGENT_NEGOCIANT, NEGOCIANT, dsm.id(), illisible, null))
                .isInstanceOf(RegleEnfreinte.class).extracting(e -> ((RegleEnfreinte) e).regle().orElseThrow())
                .isEqualTo("RG-DEC-21");
    }

    @Test
    @DisplayName("RG-DEC-38, 08, 23 : le BNIA importe un stock initial ; le point de départ est modifié, la DSM en cours "
            + "suit ; sans point de départ, il est fixé au lendemain de la période du fichier")
    void rg_dec_38_importer_stock_initial() {
        long version = points.consulter(AGENT_BNIA, NEGOCIANT).orElseThrow().point().version();
        PointImporte modifie = points.importer(AGENT_BNIA, NEGOCIANT, fichier("mai.xlsx", Fichiers.du(YearMonth.of(2026, 5))
                .ligne("XO", "NB", "12", Map.of(1, 1000, 4, 200))
                .ligne("VSOP", "NV", "5", Map.of(1, 300, 22, 100))), Optional.empty(), version);

        assertThat(modifie.point().point().debut()).isEqualTo(LocalDate.of(2026, 6, 1));
        assertThat(modifie.point().point().lignes()).extracting(l -> l.identite().libelle(), LigneStockInitial::volume)
                .containsExactly(tuple("« dénomination XO, clé NB, compte d'âge 12 »", VolumeAlcoolPur.litres("1200")),
                        tuple("« dénomination VSOP, clé NV, compte d'âge 5 »", VolumeAlcoolPur.litres("200")));
        assertThat(gerer.consulter(AGENT_NEGOCIANT, NEGOCIANT, dsm.id()).dsm().lignes())
                .extracting(l -> l.identite().libelle(), LigneDsm::origine, LigneDsm::stockDebut).containsExactly(
                        tuple("« dénomination XO, clé NB, compte d'âge 12 »", OrigineLigne.REPRISE, VolumeAlcoolPur.litres("1200")),
                        tuple("« millésime 2010, clé MB, compte d'âge 15 »", OrigineLigne.AJOUT, VolumeAlcoolPur.ZERO),
                        tuple("« dénomination VSOP, clé NV, compte d'âge 5 »", OrigineLigne.REPRISE, VolumeAlcoolPur.litres("200")));
        assertThat(monde.journal.deLOperateur(NEGOCIANT).getFirst()).satisfies(e -> {
            assertThat(e.action()).isEqualTo(ActionJournalDeclarations.POINT_DE_DEPART_IMPORTE);
            assertThat(e.detail()).isEqualTo("mai.xlsx : Début au 01/06/2026, 2 lignes de stock initial");
        });

        PointImporte fixe = points.importer(AGENT_BNIA, Monde.PRODUCTEUR, fichier("campagne.xlsx",
                Fichiers.du(YearMonth.of(2025, 4)).ligne(2018, "MB", null, Map.of(1, 80))), Optional.empty(), null);
        assertThat(fixe.point().point().debut()).isEqualTo(LocalDate.of(2026, 4, 1));
        assertThat(fixe.point().point().lignes()).singleElement().extracting(l -> l.identite().compte().toString())
                .isEqualTo("7");
    }

    @Test
    @DisplayName("RG-DEC-01 : seul le BNIA qui gère le module importe un stock initial")
    void rg_dec_01_import_stock_refus() {
        FichierDepose mai = fichier("mai.xlsx", Fichiers.du(YearMonth.of(2026, 5)));
        for (Acteur acteur : List.of(AGENT_NEGOCIANT, Monde.OBSERVATEUR_BNIA, INCONNU)) {
            refuse(() -> points.importer(acteur, NEGOCIANT, mai, Optional.empty(), null));
        }
    }
}
