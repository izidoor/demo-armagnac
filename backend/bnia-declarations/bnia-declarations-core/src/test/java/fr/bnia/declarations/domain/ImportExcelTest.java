package fr.bnia.declarations.domain;

import static fr.bnia.declarations.domain.Exemples.PERIODE_JUIN;
import static fr.bnia.declarations.domain.Exemples.dsmDeJuin;
import static fr.bnia.declarations.domain.Exemples.l;
import static fr.bnia.declarations.domain.Exemples.refuse;
import static fr.bnia.declarations.domain.Exemples.saisie;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

import fr.bnia.commun.domain.VolumeAlcoolPur;
import fr.bnia.declarations.domain.ImportExcel.Lecture;
import fr.bnia.declarations.domain.ImportExcel.StockImporte;
import fr.bnia.declarations.domain.ImportRefuse.Erreur;
import fr.bnia.declarations.domain.partage.CleConditionnement;
import fr.bnia.declarations.domain.partage.CompteDAgeDsm;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Import du modèle Excel (spec 05 §5.1, lot 6) : contrôle tout ou rien, rapprochement des lignes, stock initial. */
class ImportExcelTest {

    private static final YearMonth JUIN = YearMonth.of(2026, 6);

    @Test
    @DisplayName("RG-DEC-37 : clé inconnue, volume non numérique ou négatif, ligne en double, période différente : "
            + "toutes les erreurs, situées, refusent le fichier")
    void rg_dec_37_erreurs_situees() {
        ClasseurLu fichier = Fichiers.du(YearMonth.of(2026, 5))
                .ligne("XO", "NB", "12", Map.of(17, 100))
                .ligne("XO", "XB", "12", Map.of())
                .ligne("VSOP", "NB", "5", Map.of(17, "abc", 22, -5))
                .ligne("xo", "NB", "12", Map.of())
                .ligne("Blanche", "NB", "25 ans", Map.of())
                .ligne("deux mille", "MB", null, Map.of())
                .vierge()
                .classeur();

        assertThatThrownBy(() -> ImportExcel.pourDsm(fichier, TypeDeclarant.NEGOCIANT, PERIODE_JUIN))
                .isInstanceOfSatisfying(ImportRefuse.class, e -> {
                    assertThat(e.regle()).contains("RG-DEC-37");
                    assertThat(e.getMessage()).isEqualTo("Le fichier est refusé (7 erreurs) : rien n'est importé.");
                    assertThat(e.erreurs()).extracting(Erreur::onglet, Erreur::ligne, Erreur::colonne).containsExactly(
                            tuple(OngletDsm.RECAPITULATIF, 8, "A"),
                            tuple(OngletDsm.RECAPITULATIF, 19, "B"),
                            tuple(OngletDsm.VENTES_FRANCE, 20, "17"),
                            tuple(OngletDsm.VENTES_EXPORT, 20, "22"),
                            tuple(OngletDsm.RECAPITULATIF, 21, "A"),
                            tuple(OngletDsm.RECAPITULATIF, 22, "C"),
                            tuple(OngletDsm.RECAPITULATIF, 23, "A"));
                    assertThat(e.erreurs()).extracting(Erreur::message).containsExactly(
                            "Le fichier porte sur mai 2026, la DSM sur juin 2026 : la période du fichier doit être celle"
                                    + " de la DSM.",
                            "Clé « XB » inconnue (attendu : MB, MV, NB ou NV).",
                            "« abc » n'est pas un volume (colonne 17).",
                            "Volume négatif (-5,00 L, colonne 22) : un volume est positif ou nul.",
                            "La ligne « dénomination xo, clé NB, compte d'âge 12 » figure deux fois (lignes 18 et 21) :"
                                    + " même clé, même millésime ou dénomination, même compte d'âge.",
                            "Compte d'âge invalide : « 25 ans » (attendu : 00, 0, 1 … 19 ou 20+).",
                            "« deux mille » n'est pas un millésime (ligne MB).");
                });
    }

    @Test
    @DisplayName("RG-DEC-37 : une valeur en colonne grisée pour un producteur refuse le fichier ; période absente aussi")
    void rg_dec_37_grisee_et_periode_absente() {
        PeriodeDeclaration campagne = PeriodeDeclaration.commencant(TypeDeclarant.PRODUCTEUR, LocalDate.of(2026, 4, 1));
        ClasseurLu producteur = Fichiers.du(YearMonth.of(2026, 4)).ligne(2018, "MB", null, Map.of(2, 10, 4, 20)).classeur();

        assertThatThrownBy(() -> ImportExcel.pourDsm(producteur, TypeDeclarant.PRODUCTEUR, campagne))
                .isInstanceOfSatisfying(ImportRefuse.class, e -> assertThat(e.erreurs()).singleElement()
                        .satisfies(erreur -> {
                            assertThat(erreur.onglet()).isEqualTo(OngletDsm.RECAPITULATIF);
                            assertThat(erreur.colonne()).isEqualTo("2");
                            assertThat(erreur.message()).isEqualTo("La colonne 2 est grisée pour un producteur : elle reste vide.");
                        }));
        ClasseurLu sansPeriode = Fichiers.du(null).ligne("XO", "NB", "12", Map.of()).classeur();
        assertThatThrownBy(() -> ImportExcel.pourDsm(sansPeriode, TypeDeclarant.NEGOCIANT, PERIODE_JUIN))
                .isInstanceOfSatisfying(ImportRefuse.class, e -> assertThat(e.erreurs()).extracting(Erreur::message)
                        .containsExactly("La période du fichier est absente ou illisible : une date du mois déclaré est attendue."));
    }

    @Test
    @DisplayName("RG-DEC-38 : lignes rapprochées par identité ; reprise absente gardée à 0, ajout absent supprimé, "
            + "ligne nouvelle ajoutée ; colonnes 1, 26 et compte MB ignorés, écarts signalés")
    void rg_dec_38_rapprochement() {
        DeclarationStocksMouvements dsm = dsmDeJuin();
        List<SaisieLigneDsm> lignes = new ArrayList<>(saisie(dsm, Map.of(1, Map.of(ColonneDsm.C17, l("100")))).lignes());
        lignes.add(new SaisieLigneDsm(null, CleConditionnement.NB, null, "Hors d'âge", "20+", Map.of(ColonneDsm.C04, l("10"))));
        dsm.enregistrer(new SaisieDsm(LocalDate.of(2026, 6, 20), lignes), null);
        ClasseurLu fichier = Fichiers.du(JUIN)
                .ligne("XO", "nb", 12, Map.of(1, "1 400,00", 17, 300, 26, 1200))
                .ligne(2010, "MB", "14", Map.of(22, 40.5))
                .ligne("XO", "NV", "10", Map.of(4, 50, 26, 0))
                .ligne(" vsop ", "NV", "5", Map.of())
                .ligne("Blanche", "NB", "00", Map.of(26, 120))
                .ligne(2015, "MV", "6", Map.of(26, 300))
                .vierge()
                .classeur();

        Lecture lecture = ImportExcel.pourDsm(fichier, TypeDeclarant.NEGOCIANT, PERIODE_JUIN);
        List<String> avertissements = dsm.importer(lecture.lignes(), dsm.version());

        assertThat(dsm.lignes()).extracting(li -> li.identite().libelle(), LigneDsm::origine, LigneDsm::volumes)
                .containsExactly(
                        tuple("« dénomination XO, clé NB, compte d'âge 12 »",
                                OrigineLigne.REPRISE, Map.of(ColonneDsm.C01, l("1500"), ColonneDsm.C17, l("300"))),
                        tuple("« dénomination VSOP, clé NB, compte d'âge 5 »",
                                OrigineLigne.REPRISE, Map.of(ColonneDsm.C01, l("2200.50"))),
                        tuple("« dénomination VSOP, clé NV, compte d'âge 5 »",
                                OrigineLigne.REPRISE, Map.of(ColonneDsm.C01, l("800"))),
                        tuple("« millésime 2010, clé MB, compte d'âge 15 »",
                                OrigineLigne.REPRISE, Map.of(ColonneDsm.C01, l("640.25"), ColonneDsm.C22, l("40.50"))),
                        tuple("« millésime 2015, clé MV, compte d'âge 6 »",
                                OrigineLigne.REPRISE, Map.of(ColonneDsm.C01, l("300"))),
                        tuple("« dénomination Blanche, clé NB, compte d'âge 00 »",
                                OrigineLigne.REPRISE, Map.of(ColonneDsm.C01, l("120"))),
                        tuple("« dénomination XO, clé NV, compte d'âge 10 »",
                                OrigineLigne.AJOUT, Map.of(ColonneDsm.C04, l("50"))));
        assertThat(dsm.dateDia()).isEqualTo(LocalDate.of(2026, 6, 20));
        assertThat(lecture.avertissements()).containsExactly("Ligne 19 du fichier : compte d'âge « 14 » ignoré, 15 calculé"
                + " depuis le millésime 2010 (RG-DEC-12).");
        assertThat(avertissements).containsExactly(
                "Ligne 18 du fichier (« dénomination XO, clé NB, compte d'âge 12 ») : stock de début 1 400,00 L dans le"
                        + " fichier, 1 500,00 L dans la DSM ; celui de la DSM est gardé.",
                "Ligne 20 du fichier (« dénomination XO, clé NV, compte d'âge 10 ») : stock de fin 0,00 L dans le"
                        + " fichier, 50,00 L calculé ; le calcul prévaut.");
    }

    @Test
    @DisplayName("RG-DEC-21, 38 : l'import contrôle la version et ne se fait que sur une DSM modifiable")
    void rg_dec_21_import_modifiable() {
        DeclarationStocksMouvements dsm = dsmDeJuin();
        refuse(null, () -> dsm.importer(List.of(), dsm.version() + 1));
        dsm.envoyer(Exemples.MAINTENANT, null);
        refuse("RG-DEC-21", () -> dsm.importer(List.of(), null));
        refuse("RG-DEC-21", dsm::exigerModifiable);
    }

    @Test
    @DisplayName("RG-DEC-38 : le stock de fin du fichier, calculé sur ses colonnes, devient le stock initial au "
            + "lendemain de sa période, vieilli au 1er avril ; les lignes finies à 0 ne sont pas reprises")
    void rg_dec_38_stock_initial() {
        ClasseurLu mars = Fichiers.du(YearMonth.of(2026, 3))
                .ligne("XO", "NB", "12", Map.of(1, 1000, 4, 200, 17, 100, 26, 1100))
                .ligne(2010, "MB", null, Map.of(1, 500, 26, 450))
                .ligne("VSOP", "NV", "5", Map.of(1, 50, 17, 50))
                .classeur();

        StockImporte stock = ImportExcel.stockInitial(mars, TypeDeclarant.NEGOCIANT, Optional.empty());

        assertThat(stock.debut()).isEqualTo(LocalDate.of(2026, 4, 1));
        assertThat(stock.lignes()).extracting(li -> li.identite().libelle(), LigneStockInitial::volume).containsExactly(
                tuple("« dénomination XO, clé NB, compte d'âge 13 »", l("1100")),
                tuple("« millésime 2010, clé MB, compte d'âge 15 »", l("500")));
        assertThat(stock.avertissements()).containsExactly("Ligne 19 du fichier (« millésime 2010, clé MB, compte d'âge"
                + " 14 ») : stock de fin 450,00 L dans le fichier, 500,00 L calculé ; le calcul prévaut.");

        StockImporte choisi = ImportExcel.stockInitial(mars, TypeDeclarant.NEGOCIANT,
                Optional.of(LocalDate.of(2026, 6, 1)));
        assertThat(choisi.debut()).isEqualTo(LocalDate.of(2026, 6, 1));
        assertThat(choisi.lignes().getFirst().identite().compte()).isEqualTo(CompteDAgeDsm.de(12));
        assertThat(choisi.avertissements()).first().isEqualTo("Le fichier porte sur mars 2026 : son stock de fin du"
                + " 31/03/2026 devient le stock initial au 01/06/2026.");
    }

    @Test
    @DisplayName("RG-DEC-37 : en stock initial, un stock de fin négatif ou la période d'un producteur hors avril refuse le fichier")
    void rg_dec_37_stock_initial_refuse() {
        ClasseurLu negatif = Fichiers.du(YearMonth.of(2026, 5)).ligne("XO", "NB", "12", Map.of(1, 10, 17, 30)).classeur();
        assertThatThrownBy(() -> ImportExcel.stockInitial(negatif, TypeDeclarant.NEGOCIANT, Optional.empty()))
                .isInstanceOfSatisfying(ImportRefuse.class, e -> assertThat(e.erreurs()).singleElement()
                        .isEqualTo(new Erreur(OngletDsm.RECAPITULATIF, 18, "I", "Le stock de fin calculé (-20,00 L) est"
                                + " négatif : il ne peut pas devenir un stock initial.")));
        ClasseurLu mai = Fichiers.du(YearMonth.of(2026, 5)).ligne(2018, "MB", null, Map.of(1, 10)).classeur();
        assertThatThrownBy(() -> ImportExcel.stockInitial(mai, TypeDeclarant.PRODUCTEUR, Optional.empty()))
                .isInstanceOfSatisfying(ImportRefuse.class, e -> assertThat(e.erreurs()).extracting(Erreur::message)
                        .containsExactly("Le fichier porte sur mai 2026 : la période d'un producteur commence en avril."));
    }

    @Test
    @DisplayName("RG-DEC-37 : un volume à plus de deux décimales est arrondi au centilitre")
    void rg_dec_37_arrondi() {
        ClasseurLu fichier = Fichiers.du(JUIN).ligne("XO", "NB", "12", Map.of(17, 100.005)).classeur();
        assertThat(ImportExcel.pourDsm(fichier, TypeDeclarant.NEGOCIANT, PERIODE_JUIN).lignes().getFirst().mouvements())
                .containsEntry(ColonneDsm.C17, VolumeAlcoolPur.litres("100.01"));
    }
}
