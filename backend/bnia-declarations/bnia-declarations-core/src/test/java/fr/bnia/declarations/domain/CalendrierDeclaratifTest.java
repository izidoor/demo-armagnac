package fr.bnia.declarations.domain;

import static fr.bnia.declarations.domain.Exemples.JUIN;
import static fr.bnia.declarations.domain.Exemples.PERIODE_JUIN;
import static fr.bnia.declarations.domain.Exemples.pointDeJuin;
import static fr.bnia.declarations.domain.Exemples.refuse;
import static org.assertj.core.api.Assertions.assertThat;

import fr.bnia.declarations.domain.CalendrierDeclaratif.DsmPassee;
import fr.bnia.declarations.domain.CalendrierDeclaratif.Empechement;
import fr.bnia.declarations.domain.CalendrierDeclaratif.Situation;
import fr.bnia.operateurs.domain.partage.StatutDeclarationStock;
import fr.bnia.operateurs.domain.partage.TypeOperateur;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class CalendrierDeclaratifTest {

    private static final LocalDate AUJOURDHUI = LocalDate.of(2026, 10, 13);
    private static final Optional<PointDeDepartDeclaratif> POINT = Optional.of(pointDeJuin(List.of()));

    private static Situation situation(List<DsmPassee> dsms) {
        return CalendrierDeclaratif.prochaine(TypeOperateur.NEGOCIANT, StatutDeclarationStock.OUI, POINT, dsms, AUJOURDHUI);
    }

    @Test
    @DisplayName("RG-DEC-03 : une année du 1er avril au 31 mars pour un producteur, un mois civil pour un négociant")
    void rg_dec_03_periodes() {
        PeriodeDeclaration annee = PeriodeDeclaration.commencant(TypeDeclarant.PRODUCTEUR, LocalDate.of(2025, 4, 1));
        assertThat(annee.fin()).isEqualTo(LocalDate.of(2026, 3, 31));
        assertThat(annee.libelle()).isEqualTo("2025-2026");
        assertThat(PERIODE_JUIN.fin()).isEqualTo(LocalDate.of(2026, 6, 30));
        assertThat(PERIODE_JUIN.libelle()).isEqualTo("juin 2026");
        assertThat(PeriodeDeclaration.commencant(TypeDeclarant.NEGOCIANT, LocalDate.of(2026, 2, 1)).fin())
                .isEqualTo(LocalDate.of(2026, 2, 28));
        refuse("RG-DEC-03", () -> PeriodeDeclaration.commencant(TypeDeclarant.PRODUCTEUR, JUIN));
    }

    @Test
    @DisplayName("RG-DEC-04 : au point de départ, puis le lendemain de la dernière DSM validée")
    void rg_dec_04_periode_suivante() {
        assertThat(situation(List.of()).periode()).contains(PERIODE_JUIN);
        PeriodeDeclaration juillet = PERIODE_JUIN.suivante(TypeDeclarant.NEGOCIANT);
        assertThat(situation(List.of(new DsmPassee(1, PERIODE_JUIN, EtatDsm.VALIDEE),
                new DsmPassee(2, juillet, EtatDsm.VALIDEE))).periode().orElseThrow().debut())
                .isEqualTo(LocalDate.of(2026, 8, 1));
        PeriodeDeclaration avantLePoint = PeriodeDeclaration.commencant(TypeDeclarant.NEGOCIANT, LocalDate.of(2025, 1, 1));
        assertThat(situation(List.of(new DsmPassee(3, avantLePoint, EtatDsm.VALIDEE))).periode())
                .as("une DSM validée antérieure au point de départ est ignorée").contains(PERIODE_JUIN);
    }

    @Test
    @DisplayName("RG-DEC-05 : refusée sans point de départ, avec une DSM non validée, avant le premier jour")
    void rg_dec_05_empechements() {
        assertThat(situation(List.of()).initiable()).isTrue();
        Situation sansPoint = CalendrierDeclaratif.prochaine(TypeOperateur.NEGOCIANT, StatutDeclarationStock.OUI,
                Optional.empty(), List.of(), AUJOURDHUI);
        assertThat(sansPoint.empechement()).contains(Empechement.SANS_POINT_DE_DEPART);
        Situation enCours = situation(List.of(new DsmPassee(4, PERIODE_JUIN, EtatDsm.BROUILLON)));
        assertThat(enCours.empechement()).contains(Empechement.DSM_EN_COURS);
        assertThat(enCours.dsmEnCours()).contains(4L);
        refuse("RG-DEC-05", enCours::exigerInitiable);
        Situation pasCommencee = CalendrierDeclaratif.prochaine(TypeOperateur.NEGOCIANT, StatutDeclarationStock.OUI,
                POINT, List.of(), LocalDate.of(2026, 5, 31));
        assertThat(pasCommencee.empechement()).contains(Empechement.PERIODE_PAS_COMMENCEE);
        assertThat(pasCommencee.message()).contains("01/06/2026");
    }

    @ParameterizedTest
    @EnumSource(value = StatutDeclarationStock.class, names = "OUI", mode = EnumSource.Mode.EXCLUDE)
    @DisplayName("RG-DEC-02 : seul le statut OUI permet d'initier ; une DSM en cours va à son terme")
    void rg_dec_02_statut(StatutDeclarationStock statut) {
        Situation situation = CalendrierDeclaratif.prochaine(TypeOperateur.PRODUCTEUR, statut, POINT, List.of(), AUJOURDHUI);
        assertThat(situation.empechement()).contains(Empechement.STATUT_NON_DECLARANT);
        refuse("RG-DEC-02", situation::exigerInitiable);
        assertThat(CalendrierDeclaratif.prochaine(TypeOperateur.NEGOCIANT, statut, POINT,
                List.of(new DsmPassee(5, PERIODE_JUIN, EtatDsm.BROUILLON)), AUJOURDHUI).dsmEnCours()).contains(5L);
    }

    @Test
    @DisplayName("RG-DEC-02 : un opérateur AUTRE ne déclare pas")
    void rg_dec_02_type() {
        assertThat(CalendrierDeclaratif.prochaine(TypeOperateur.AUTRE, StatutDeclarationStock.NON, POINT, List.of(),
                AUJOURDHUI).empechement()).contains(Empechement.TYPE_NON_DECLARANT);
    }
}
