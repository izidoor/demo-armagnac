package fr.bnia.declarations.domain;

import static fr.bnia.declarations.domain.ColonneDsm.C01;
import static fr.bnia.declarations.domain.ColonneDsm.C05;
import static fr.bnia.declarations.domain.ColonneDsm.C06;
import static fr.bnia.declarations.domain.ColonneDsm.C10;
import static fr.bnia.declarations.domain.ColonneDsm.C11;
import static fr.bnia.declarations.domain.ColonneDsm.C12;
import static fr.bnia.declarations.domain.ColonneDsm.C17;
import static fr.bnia.declarations.domain.ColonneDsm.C19;
import static fr.bnia.declarations.domain.ColonneDsm.C22;
import static fr.bnia.declarations.domain.ColonneDsm.C26;
import static fr.bnia.declarations.domain.Exemples.OPERATEUR;
import static fr.bnia.declarations.domain.Exemples.PERIODE_JUIN;
import static fr.bnia.declarations.domain.Exemples.dsm;
import static fr.bnia.declarations.domain.Exemples.dsmDeJuin;
import static fr.bnia.declarations.domain.Exemples.l;
import static fr.bnia.declarations.domain.Exemples.refuse;
import static fr.bnia.declarations.domain.Exemples.saisie;
import static fr.bnia.declarations.domain.Exemples.sixLignes;
import static fr.bnia.declarations.domain.Exemples.stock;
import static fr.bnia.declarations.domain.Exemples.telleQuelle;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import fr.bnia.commun.domain.ModificationConcurrente;
import fr.bnia.commun.domain.VolumeAlcoolPur;
import fr.bnia.declarations.domain.partage.CleConditionnement;
import fr.bnia.declarations.domain.partage.EtatDsm;
import fr.bnia.operateurs.domain.partage.StatutDeclarationStock;
import fr.bnia.operateurs.domain.partage.TypeOperateur;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class DeclarationStocksMouvementsTest {

    private static final PeriodeDeclaration ANNEE_2026 = PeriodeDeclaration.commencant(TypeDeclarant.PRODUCTEUR,
            LocalDate.of(2026, 4, 1));

    private static SaisieDsm avecLigne(DeclarationStocksMouvements dsm, SaisieLigneDsm ligne) {
        List<SaisieLigneDsm> lignes = new ArrayList<>(saisie(dsm, Map.of()).lignes());
        lignes.add(ligne);
        return new SaisieDsm(dsm.dateDia(), lignes);
    }

    private static SaisieLigneDsm ajout(CleConditionnement cle, Integer millesime, String denomination, String compte,
            Map<ColonneDsm, VolumeAlcoolPur> volumes) {
        return new SaisieLigneDsm(null, cle, millesime, denomination, compte, volumes);
    }

    @Test
    @DisplayName("RG-DEC-02 : le type de déclarant est lu à l'initiation ; un opérateur AUTRE ou non OUI n'initie pas")
    void rg_dec_02_initiation() {
        assertThat(dsmDeJuin().type()).isEqualTo(TypeDeclarant.NEGOCIANT);
        assertThat(dsmDeJuin().etat()).isEqualTo(EtatDsm.BROUILLON);
        refuse("RG-DEC-02", () -> DeclarationStocksMouvements.initier(OPERATEUR, TypeOperateur.AUTRE,
                StatutDeclarationStock.OUI, PERIODE_JUIN, List.of()));
        refuse("RG-DEC-02", () -> DeclarationStocksMouvements.initier(OPERATEUR, TypeOperateur.NEGOCIANT,
                StatutDeclarationStock.SOMMEIL, PERIODE_JUIN, List.of()));
    }

    @Test
    @DisplayName("RG-DEC-13 : les lignes reprennent le stock initial en colonne 1, origine REPRISE")
    void rg_dec_13_reprise_du_stock_initial() {
        DeclarationStocksMouvements dsm = dsmDeJuin();
        assertThat(dsm.lignes()).hasSize(6).allMatch(ligne -> ligne.origine() == OrigineLigne.REPRISE);
        assertThat(dsm.lignes().getFirst().volume(C01)).isEqualTo(l("1500"));
        assertThat(dsm.total(C01)).isEqualTo(l("5560.75"));
    }

    @Test
    @DisplayName("RG-DEC-13 : une ligne ajoutée commence à 0 et se supprime ; une ligne reprise ne se supprime pas")
    void rg_dec_13_ajout_et_suppression() {
        DeclarationStocksMouvements dsm = dsmDeJuin();
        dsm.enregistrer(avecLigne(dsm, ajout(CleConditionnement.NV, null, "Hors d'âge", "15", Map.of())), 0L);
        assertThat(dsm.lignes()).hasSize(7);
        LigneDsm ajoutee = dsm.lignes().getLast();
        assertThat(ajoutee.origine()).isEqualTo(OrigineLigne.AJOUT);
        assertThat(ajoutee.stockDebut()).isEqualTo(VolumeAlcoolPur.ZERO);

        DeclarationStocksMouvements dsm2 = dsmDeJuin();
        dsm2.enregistrer(avecLigne(dsm2, ajout(CleConditionnement.NV, null, "Hors d'âge", "15", Map.of())), null);
        List<SaisieLigneDsm> sansLAjout = saisie(dsm2, Map.of()).lignes().subList(0, 6);
        dsm2.enregistrer(new SaisieDsm(null, sansLAjout), null);
        assertThat(dsm2.lignes()).hasSize(6);

        List<SaisieLigneDsm> sansLaPremiere = saisie(dsm, Map.of()).lignes().subList(1, 7);
        refuse("RG-DEC-13", () -> dsm.enregistrer(new SaisieDsm(null, sansLaPremiere), null));
    }

    @Test
    @DisplayName("RG-DEC-13 : l'identité d'une ligne reprise ne se modifie pas")
    void rg_dec_13_identite_figee() {
        DeclarationStocksMouvements dsm = dsmDeJuin();
        List<SaisieLigneDsm> lignes = new ArrayList<>(saisie(dsm, Map.of()).lignes());
        SaisieLigneDsm xo = lignes.getFirst();
        lignes.set(0, new SaisieLigneDsm(xo.id(), xo.cle(), null, "XO", "13", Map.of()));
        refuse("RG-DEC-13", () -> dsm.enregistrer(new SaisieDsm(null, lignes), null));
        lignes.set(0, new SaisieLigneDsm(xo.id(), xo.cle(), null, "  xo ", "12", Map.of()));
        dsm.enregistrer(new SaisieDsm(null, lignes), null);
        assertThat(dsm.lignes().getFirst().identite().denomination().texte()).as("texte d'origine gardé").isEqualTo("XO");
    }

    @Test
    @DisplayName("RG-DEC-10 : millésime pour une clé M, dénomination pour une clé N")
    void rg_dec_10_millesime_ou_denomination() {
        DeclarationStocksMouvements dsm = dsmDeJuin();
        refuse("RG-DEC-10", () -> dsm.enregistrer(avecLigne(dsm, ajout(CleConditionnement.MV, null, "XO", "3", Map.of())), null));
        refuse("RG-DEC-10", () -> dsm.enregistrer(avecLigne(dsm, ajout(CleConditionnement.NB, 2020, null, "3", Map.of())), null));
        refuse("RG-DEC-10", () -> dsm.enregistrer(avecLigne(dsm, ajout(CleConditionnement.NB, null, "  ", "3", Map.of())), null));
        refuse("RG-DEC-10", () -> dsm.enregistrer(avecLigne(dsm, ajout(null, 2020, null, "3", Map.of())), null));
    }

    @Test
    @DisplayName("RG-DEC-11 : lignes uniques ; dénominations comparées sans casse, accents ni espaces")
    void rg_dec_11_unicite() {
        DeclarationStocksMouvements dsm = dsmDeJuin();
        refuse("RG-DEC-11", () -> dsm.enregistrer(avecLigne(dsm, ajout(CleConditionnement.NB, null, " xo", "12", Map.of())), null));
        refuse("RG-DEC-11", () -> dsm.enregistrer(avecLigne(dsm, ajout(CleConditionnement.NB, null, "BLANCHE", "00", Map.of())), null));
        dsm.enregistrer(avecLigne(dsm, ajout(CleConditionnement.NB, null, "XO", "13", Map.of())), null);
        assertThat(dsm.lignes()).hasSize(7);
        assertThat(Denomination.de("Hors  d'Âge")).isEqualTo(Denomination.de("hors d'age"));
    }

    @Test
    @DisplayName("RG-DEC-12 : compte MB calculé au début de la période, saisi pour MV, NB, NV")
    void rg_dec_12_compte_mb_calcule() {
        DeclarationStocksMouvements dsm = dsmDeJuin();
        dsm.enregistrer(avecLigne(dsm, ajout(CleConditionnement.MB, 2020, null, "0", Map.of())), null);
        assertThat(dsm.lignes().getLast().identite().compte()).hasToString("5");
        refuse("RG-DEC-12", () -> dsm.enregistrer(avecLigne(dsm, ajout(CleConditionnement.NV, null, "Extra", null, Map.of())), null));
    }

    @Test
    @DisplayName("RG-DEC-15 : un volume est positif ou nul, au centilitre")
    void rg_dec_15_volumes_positifs() {
        DeclarationStocksMouvements dsm = dsmDeJuin();
        refuse("RG-DEC-15", () -> dsm.enregistrer(saisie(dsm, Map.of(0, Map.of(C17, l("-1")))), null));
        dsm.enregistrer(saisie(dsm, Map.of(0, Map.of(C17, l("12.345")))), null);
        assertThat(dsm.lignes().getFirst().volume(C17)).isEqualTo(l("12.35"));
    }

    @Test
    @DisplayName("RG-DEC-16 : colonnes grisées vides pour un producteur, saisissables pour un négociant")
    void rg_dec_16_colonnes_grisees() {
        DeclarationStocksMouvements producteur = dsm(TypeOperateur.PRODUCTEUR, ANNEE_2026,
                List.of(stock(CleConditionnement.NB, "XO", "12", "100")));
        refuse("RG-DEC-16", () -> producteur.enregistrer(saisie(producteur, Map.of(0, Map.of(C19, l("1")))), null));
        producteur.enregistrer(saisie(producteur, Map.of(0, Map.of(C19, VolumeAlcoolPur.ZERO, C17, l("1")))), null);
        assertThat(ColonneDsm.mouvements().stream().filter(c -> c.griseePour(TypeDeclarant.PRODUCTEUR)))
                .extracting(ColonneDsm::numero).containsExactly(2, 3, 5, 9, 11, 19, 24, 25);
        DeclarationStocksMouvements negociant = dsmDeJuin();
        negociant.enregistrer(saisie(negociant, Map.of(0, Map.of(C19, l("1")))), null);
        assertThat(negociant.total(C19)).isEqualTo(l("1"));
    }

    @Test
    @DisplayName("RG-DEC-17 : stock de fin = 1 + entrées − sorties ; totaux par ligne et par colonne ; 1 et 26 non saisies")
    void rg_dec_17_calculs() {
        DeclarationStocksMouvements dsm = dsmDeJuin();
        dsm.enregistrer(saisie(dsm, Map.of(0, Map.of(C06, l("100"), C10, l("2.5"), C17, l("300"), C22, l("97.5")),
                1, Map.of(C12, l("100")))), null);
        LigneDsm xo = dsm.lignes().getFirst();
        assertThat(xo.totalEntrees()).isEqualTo(l("100"));
        assertThat(xo.totalSorties()).isEqualTo(l("400"));
        assertThat(xo.stockFin()).isEqualTo(l("1200"));
        assertThat(xo.volume(C26)).isEqualTo(l("1200"));
        assertThat(dsm.total(C26)).isEqualTo(l("5160.75"));
        refuse("RG-DEC-17", () -> dsm.enregistrer(saisie(dsm, Map.of(0, Map.of(C01, l("1")))), null));
    }

    @Test
    @DisplayName("RG-DEC-18 : stock négatif, Σ6 ≠ Σ12, Σ5 ≠ Σ11 (négociant), compte MV trop vieux ; l'enregistrement reste permis")
    void rg_dec_18_anomalies() {
        DeclarationStocksMouvements dsm = dsmDeJuin();
        assertThat(dsm.anomalies()).isEmpty();
        dsm.enregistrer(saisie(dsm, Map.of(5, Map.of(C17, l("150")), 0, Map.of(C06, l("10"), C05, l("4")))), null);
        dsm.enregistrer(avecLigne(dsm, ajout(CleConditionnement.MV, 2024, null, "3", Map.of())), null);
        List<Anomalie> anomalies = dsm.anomalies();
        assertThat(anomalies).extracting(Anomalie::type).containsExactlyInAnyOrder(
                TypeAnomalie.STOCK_FIN_NEGATIF, TypeAnomalie.ASSEMBLAGES_DESEQUILIBRES,
                TypeAnomalie.PASSAGES_SOUS_BOIS_DESEQUILIBRES, TypeAnomalie.COMPTE_MV_TROP_VIEUX);
        Anomalie negatif = anomalies.stream().filter(a -> a.type() == TypeAnomalie.STOCK_FIN_NEGATIF).findFirst().orElseThrow();
        assertThat(negatif.niveau()).isEqualTo(NiveauAnomalie.LIGNE);
        assertThat(negatif.ligne()).isEqualTo(5);
        assertThat(negatif.ecart()).isEqualTo(l("-30"));
        // La ligne dans l'ordre et avec les intitulés des colonnes de la grille ; le volume à la virgule.
        assertThat(negatif.message()).matches("Ligne « (millésime|dénomination) .+, clé .., compte d'âge .+ » : .*")
                .contains("colonne 26", "-30,00 L");
        Anomalie assemblages = anomalies.stream().filter(a -> a.type() == TypeAnomalie.ASSEMBLAGES_DESEQUILIBRES)
                .findFirst().orElseThrow();
        assertThat(assemblages.niveau()).isEqualTo(NiveauAnomalie.ONGLET);
        assertThat(assemblages.ecart()).isEqualTo(l("10"));
        assertThat(assemblages.message()).contains("colonne 6", "colonne 12");
    }

    @Test
    @DisplayName("RG-DEC-18 : Σ5 ≠ Σ11 ne se contrôle pas pour un producteur, dont les colonnes 5 et 11 sont grisées")
    void rg_dec_18_producteur() {
        DeclarationStocksMouvements producteur = dsm(TypeOperateur.PRODUCTEUR, ANNEE_2026,
                List.of(stock(CleConditionnement.NB, "XO", "12", "100")));
        producteur.enregistrer(saisie(producteur, Map.of(0, Map.of(C06, l("3"), C12, l("3")))), null);
        assertThat(producteur.anomalies()).isEmpty();
        assertThat(C11.griseePour(TypeDeclarant.PRODUCTEUR)).isTrue();
    }

    @Test
    @DisplayName("RG-DEC-19 : date de DIA facultative")
    void rg_dec_19_date_dia() {
        DeclarationStocksMouvements dsm = dsmDeJuin();
        dsm.enregistrer(new SaisieDsm(LocalDate.of(2026, 6, 30), saisie(dsm, Map.of()).lignes()), null);
        assertThat(dsm.dateDia()).isEqualTo(LocalDate.of(2026, 6, 30));
        dsm.enregistrer(new SaisieDsm(null, saisie(dsm, Map.of()).lignes()), null);
        assertThat(dsm.dateDia()).isNull();
    }

    @ParameterizedTest
    @EnumSource(value = EtatDsm.class, names = "BROUILLON", mode = EnumSource.Mode.EXCLUDE)
    @DisplayName("RG-DEC-22 : seul un brouillon se supprime")
    void rg_dec_22_suppression(EtatDsm etat) {
        DeclarationStocksMouvements brouillon = dsmDeJuin();
        brouillon.exigerSupprimable();
        DeclarationStocksMouvements autre = DeclarationStocksMouvements.reconstituer(1, OPERATEUR, TypeDeclarant.NEGOCIANT,
                PERIODE_JUIN, etat, null, List.of(), null, null, null, 0);
        refuse("RG-DEC-22", autre::exigerSupprimable);
    }

    @Test
    @DisplayName("RG-DEC-08 : la DSM en cours suit le stock initial modifié")
    void rg_dec_08_la_dsm_suit() {
        DeclarationStocksMouvements dsm = dsmDeJuin();
        dsm.enregistrer(avecLigne(dsm, ajout(CleConditionnement.NV, null, "Extra", "8", Map.of(C17, l("5")))), null);
        dsm.enregistrer(saisie(dsm, Map.of(0, Map.of(C17, l("10")))), null);
        List<LigneStockInitial> nouveau = PointDeDepartDeclaratif.fixer(OPERATEUR, TypeDeclarant.NEGOCIANT,
                PERIODE_JUIN.debut(), List.of(
                        stock(CleConditionnement.NB, "XO", "12", "1600"),
                        stock(CleConditionnement.NV, "Extra", "8", "50"),
                        stock(CleConditionnement.NB, "Folle blanche", "3", "70")),
                Exemples.BNIA, Exemples.MAINTENANT).lignes();
        dsm.suivrePointDeDepart(nouveau);
        LigneDsm xo = dsm.lignes().getFirst();
        assertThat(xo.stockDebut()).as("colonne 1 recalculée").isEqualTo(l("1600"));
        assertThat(xo.volume(C17)).as("mouvements gardés").isEqualTo(l("10"));
        assertThat(dsm.lignes().get(1).origine()).as("ligne retirée gardée en AJOUT").isEqualTo(OrigineLigne.AJOUT);
        assertThat(dsm.lignes().get(1).stockDebut()).isEqualTo(VolumeAlcoolPur.ZERO);
        LigneDsm extra = dsm.lignes().get(6);
        assertThat(extra.origine()).as("ligne ajoutée par l'opérateur, désormais reprise").isEqualTo(OrigineLigne.REPRISE);
        assertThat(extra.stockDebut()).isEqualTo(l("50"));
        assertThat(dsm.lignes().getLast().identite().denomination().texte()).isEqualTo("Folle blanche");
        assertThat(dsm.lignes()).hasSize(8);
    }

    @Test
    void vue_provisoire_sans_modifier_la_dsm() {
        DeclarationStocksMouvements dsm = dsmDeJuin();
        DeclarationStocksMouvements apercu = dsm.apercu(saisie(dsm, Map.of(5, Map.of(C17, l("150")))));
        assertThat(apercu.anomalies()).hasSize(1);
        assertThat(dsm.anomalies()).isEmpty();
        assertThat(dsm.total(C17)).isEqualTo(VolumeAlcoolPur.ZERO);
    }

    @Test
    void une_version_perimee_est_refusee() {
        DeclarationStocksMouvements dsm = dsmDeJuin();
        assertThatThrownBy(() -> dsm.enregistrer(saisie(dsm, Map.of()), 4L)).isInstanceOf(ModificationConcurrente.class);
    }

    @Test
    void une_ligne_inconnue_est_refusee() {
        DeclarationStocksMouvements dsm = dsmDeJuin();
        SaisieLigneDsm inconnue = telleQuelle(dsm.lignes().getFirst(), Map.of());
        SaisieLigneDsm fausse = new SaisieLigneDsm(999L, inconnue.cle(), null, "Autre", "1", Map.of());
        refuse(null, () -> dsm.enregistrer(avecLigne(dsm, fausse), null));
        assertThat(sixLignes()).hasSize(6);
    }
}
