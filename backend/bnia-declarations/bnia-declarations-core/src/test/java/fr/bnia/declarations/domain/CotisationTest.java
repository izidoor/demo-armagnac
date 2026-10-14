package fr.bnia.declarations.domain;

import static fr.bnia.declarations.domain.Exemples.l;
import static fr.bnia.declarations.domain.Exemples.refuse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.groups.Tuple.tuple;

import fr.bnia.commun.domain.ElementIntrouvable;
import fr.bnia.commun.domain.VolumeAlcoolPur;
import fr.bnia.declarations.domain.GrilleCotisation.TauxEnVigueur;
import fr.bnia.declarations.domain.PolitiqueCotisation.Detail;
import fr.bnia.declarations.domain.PolitiqueCotisation.LigneDetail;
import fr.bnia.declarations.domain.partage.LigneCotisation;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Grille CVO et calcul de la cotisation (spec 05 §3.5, §3.6, lot 4). */
class CotisationTest {

    private static final LocalDate FIN_OCTOBRE = LocalDate.of(2026, 10, 31);

    private static TauxCotisation taux(String montant, String pourcentage, LocalDate debut) {
        return new TauxCotisation(new BigDecimal(montant), new BigDecimal(pourcentage), debut);
    }

    /** La grille de la démonstration : 55 € puis 60 €, 20 % exonérés. */
    private static GrilleCotisation grille() {
        return GrilleCotisation.de(List.of(taux("60", "20", LocalDate.of(2024, 1, 1)),
                taux("55", "20", LocalDate.of(2021, 1, 1))));
    }

    /** Une valeur dans chaque colonne de mouvement, pour vérifier qu'aucune autre n'entre dans l'assiette. */
    private static Map<ColonneDsm, VolumeAlcoolPur> partout(String litres) {
        Map<ColonneDsm, VolumeAlcoolPur> totaux = new EnumMap<>(ColonneDsm.class);
        ColonneDsm.mouvements().forEach(c -> totaux.put(c, l(litres)));
        return totaux;
    }

    private static Detail calculer(TypeDeclarant type, Map<ColonneDsm, VolumeAlcoolPur> totaux) {
        return PolitiqueCotisation.calculer(type, FIN_OCTOBRE, c -> totaux.getOrDefault(c, VolumeAlcoolPur.ZERO),
                grille()).orElseThrow();
    }

    @Test
    @DisplayName("RG-DEC-30 : chaîne continue, fin à la veille du taux suivant, recomposée à chaque modification")
    void rg_dec_30_chaine() {
        GrilleCotisation grille = grille();
        assertThat(grille.chaine()).extracting(t -> t.taux().debut(), TauxEnVigueur::fin).containsExactly(
                tuple(LocalDate.of(2021, 1, 1), Optional.of(LocalDate.of(2023, 12, 31))),
                tuple(LocalDate.of(2024, 1, 1), Optional.empty()));

        grille.ajouter(taux("62.50", "15", LocalDate.of(2027, 1, 1)));
        assertThat(grille.chaine().get(1).fin()).contains(LocalDate.of(2026, 12, 31));
        grille.modifier(LocalDate.of(2027, 1, 1), taux("62.50", "15", LocalDate.of(2026, 7, 1)));
        assertThat(grille.chaine().get(1).fin()).contains(LocalDate.of(2026, 6, 30));
        grille.supprimer(LocalDate.of(2024, 1, 1));
        assertThat(grille.chaine()).extracting(t -> t.taux().debut(), TauxEnVigueur::fin).containsExactly(
                tuple(LocalDate.of(2021, 1, 1), Optional.of(LocalDate.of(2026, 6, 30))),
                tuple(LocalDate.of(2026, 7, 1), Optional.empty()));
        assertThat(grille.tauxAu(LocalDate.of(2026, 6, 30)).orElseThrow().taux().montant()).isEqualByComparingTo("55");
        assertThatThrownBy(() -> grille.supprimer(LocalDate.of(2020, 1, 1))).isInstanceOf(ElementIntrouvable.class);
    }

    @Test
    @DisplayName("RG-DEC-30 : début unique ; montant strictement positif, pourcentage de 0 à 100, deux décimales")
    void rg_dec_30_saisie() {
        GrilleCotisation grille = grille();
        refuse("RG-DEC-30", () -> grille.ajouter(taux("70", "20", LocalDate.of(2024, 1, 1))));
        refuse("RG-DEC-30", () -> grille.modifier(LocalDate.of(2021, 1, 1), taux("55", "20", LocalDate.of(2024, 1, 1))));
        refuse("RG-DEC-30", () -> taux("0", "20", LocalDate.of(2025, 1, 1)));
        refuse("RG-DEC-30", () -> taux("60", "100.01", LocalDate.of(2025, 1, 1)));
        refuse("RG-DEC-30", () -> taux("60", "-1", LocalDate.of(2025, 1, 1)));
        refuse("RG-DEC-30", () -> taux("60.001", "20", LocalDate.of(2025, 1, 1)));
        assertThat(taux("60", "0", LocalDate.of(2025, 1, 1)).montant()).isEqualTo(new BigDecimal("60.00"));
    }

    @Test
    @DisplayName("RG-DEC-31 : assiette 14, 16 à 19, 21 à 25 pour un négociant ; sans les colonnes grisées pour un producteur")
    void rg_dec_31_assiette() {
        assertThat(Arrays.stream(ColonneDsm.values()).filter(c -> c.assiettePour(TypeDeclarant.NEGOCIANT))
                .map(ColonneDsm::numero)).containsExactly(14, 16, 17, 18, 19, 21, 22, 23, 24, 25);
        assertThat(Arrays.stream(ColonneDsm.values()).filter(c -> c.assiettePour(TypeDeclarant.PRODUCTEUR))
                .map(ColonneDsm::numero)).containsExactly(14, 16, 17, 18, 21, 22, 23);

        Map<LigneCotisation, VolumeAlcoolPur> negociant = PolitiqueCotisation.assiette(TypeDeclarant.NEGOCIANT,
                partout("1.00")::get);
        assertThat(negociant).containsExactly(Map.entry(LigneCotisation.SORTIES_FRANCE, l("5.00")),
                Map.entry(LigneCotisation.SORTIES_EXPORT, l("3.00")),
                Map.entry(LigneCotisation.EDV_MARCHE_INTERIEUR, l("1.00")),
                Map.entry(LigneCotisation.EDV_EXPORT, l("1.00")));
        Map<LigneCotisation, VolumeAlcoolPur> producteur = PolitiqueCotisation.assiette(TypeDeclarant.PRODUCTEUR,
                partout("1.00")::get);
        assertThat(producteur).containsExactly(Map.entry(LigneCotisation.SORTIES_FRANCE, l("4.00")),
                Map.entry(LigneCotisation.SORTIES_EXPORT, l("2.00")),
                Map.entry(LigneCotisation.EDV_MARCHE_INTERIEUR, l("1.00")));
    }

    @Test
    @DisplayName("RG-DEC-32 : taux en vigueur à la fin de la période ; sans taux, non calculable")
    void rg_dec_32_taux_applique() {
        Map<ColonneDsm, VolumeAlcoolPur> totaux = new EnumMap<>(ColonneDsm.class);
        ColonneDsm.mouvements().forEach(c -> totaux.put(c, VolumeAlcoolPur.ZERO));
        totaux.put(ColonneDsm.C17, l("100.00"));
        Detail fin2023 = PolitiqueCotisation.calculer(TypeDeclarant.NEGOCIANT, LocalDate.of(2023, 12, 31),
                totaux::get, grille()).orElseThrow();
        assertThat(fin2023.taux().taux().montant()).isEqualByComparingTo("55");
        assertThat(calculer(TypeDeclarant.NEGOCIANT, totaux).taux().taux().montant()).isEqualByComparingTo("60");
        assertThat(PolitiqueCotisation.calculer(TypeDeclarant.NEGOCIANT, LocalDate.of(2020, 12, 31), totaux::get,
                grille())).isEmpty();
        assertThat(PolitiqueCotisation.calculer(TypeDeclarant.NEGOCIANT, FIN_OCTOBRE, totaux::get,
                GrilleCotisation.de(List.of()))).isEmpty();
    }

    @Test
    @DisplayName("RG-DEC-33 : la facture d'exemple, parts arrondies au centime, total HT = somme des parts")
    void rg_dec_33_facture_exemple() {
        // Armagnacs de Gascogne : sorties France 839,11 L, export 2 254,15 L, à 60 €/hlAP dont 20 % exonérés.
        Detail detail = calculer(TypeDeclarant.NEGOCIANT, Map.of(ColonneDsm.C17, l("719.11"), ColonneDsm.C21,
                l("120.00"), ColonneDsm.C22, l("954.15"), ColonneDsm.C23, l("1300.00"), ColonneDsm.C20, l("500.00")));

        assertThat(detail.lignes()).extracting(LigneDetail::ligne, LigneDetail::hectolitres, LigneDetail::nonAssujettie,
                LigneDetail::assujettie).containsExactly(
                tuple(LigneCotisation.SORTIES_FRANCE, new BigDecimal("8.3911"),
                        new BigDecimal("100.69"), new BigDecimal("402.77")),
                tuple(LigneCotisation.SORTIES_EXPORT, new BigDecimal("22.5415"),
                        new BigDecimal("270.50"), new BigDecimal("1081.99")));
        assertThat(detail.lignes().getFirst().total()).isEqualByComparingTo("503.46");
        assertThat(detail.totalNonAssujettie()).isEqualByComparingTo("371.19");
        assertThat(detail.totalAssujettie()).isEqualByComparingTo("1484.76");
        assertThat(detail.totalHt()).isEqualTo(new BigDecimal("1855.95"));
    }
}
