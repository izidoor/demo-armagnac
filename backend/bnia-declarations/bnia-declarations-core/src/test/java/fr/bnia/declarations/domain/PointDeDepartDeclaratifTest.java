package fr.bnia.declarations.domain;

import static fr.bnia.declarations.domain.Exemples.BNIA;
import static fr.bnia.declarations.domain.Exemples.JUIN;
import static fr.bnia.declarations.domain.Exemples.MAINTENANT;
import static fr.bnia.declarations.domain.Exemples.OPERATEUR;
import static fr.bnia.declarations.domain.Exemples.PERIODE_JUIN;
import static fr.bnia.declarations.domain.Exemples.pointDeJuin;
import static fr.bnia.declarations.domain.Exemples.refuse;
import static fr.bnia.declarations.domain.Exemples.sixLignes;
import static fr.bnia.declarations.domain.Exemples.stock;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import fr.bnia.commun.domain.ModificationConcurrente;
import fr.bnia.declarations.domain.partage.CleConditionnement;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class PointDeDepartDeclaratifTest {

    @ParameterizedTest(name = "{0} au {1} : permis {2}")
    @CsvSource({"PRODUCTEUR, 2025-04-01, true", "PRODUCTEUR, 2025-05-01, false", "NEGOCIANT, 2026-06-01, true",
            "NEGOCIANT, 2026-06-15, false"})
    @DisplayName("RG-DEC-06 : un 1er avril pour un producteur, un premier du mois pour un négociant")
    void rg_dec_06_debut(TypeDeclarant type, LocalDate debut, boolean permis) {
        if (permis) {
            assertThat(PointDeDepartDeclaratif.fixer(OPERATEUR, type, debut, List.of(), BNIA, MAINTENANT).debut())
                    .isEqualTo(debut);
        } else {
            refuse("RG-DEC-06", () -> PointDeDepartDeclaratif.fixer(OPERATEUR, type, debut, List.of(), BNIA, MAINTENANT));
        }
    }

    @Test
    @DisplayName("RG-DEC-07 : lignes contrôlées comme celles d'une DSM ; compte MB calculé au début ; liste vide permise")
    void rg_dec_07_lignes_du_stock_initial() {
        assertThat(pointDeJuin(List.of()).lignes()).isEmpty();
        PointDeDepartDeclaratif point = pointDeJuin(sixLignes());
        assertThat(point.lignes()).hasSize(6);
        assertThat(point.lignes().get(3).identite().compte()).hasToString("15");
        refuse("RG-DEC-11", () -> pointDeJuin(List.of(stock(CleConditionnement.NB, "XO", "12", "1"),
                stock(CleConditionnement.NB, " xo ", "12", "2"))));
        refuse("RG-DEC-15", () -> pointDeJuin(List.of(stock(CleConditionnement.NB, "XO", "12", "-1"))));
        refuse("RG-DEC-10", () -> pointDeJuin(List.of(stock(CleConditionnement.MV, "XO", "12", "1"))));
    }

    @Test
    @DisplayName("RG-DEC-08 : modifiable tant qu'il n'est pas figé")
    void rg_dec_08_fige() {
        PointDeDepartDeclaratif point = pointDeJuin(sixLignes());
        refuse("RG-DEC-08", () -> point.modifier(TypeDeclarant.NEGOCIANT, JUIN, List.of(), true, Optional.empty(), BNIA,
                MAINTENANT, null));
        point.modifier(TypeDeclarant.NEGOCIANT, JUIN, List.of(), false, Optional.of(PERIODE_JUIN), BNIA, MAINTENANT, 0L);
        assertThat(point.lignes()).isEmpty();
    }

    @Test
    @DisplayName("RG-DEC-08 : la date de début ne change pas tant qu'une DSM est en cours")
    void rg_dec_08_debut_et_dsm_en_cours() {
        PointDeDepartDeclaratif point = pointDeJuin(sixLignes());
        LocalDate juillet = LocalDate.of(2026, 7, 1);
        refuse("RG-DEC-08", () -> point.modifier(TypeDeclarant.NEGOCIANT, juillet, sixLignes(), false,
                Optional.of(PERIODE_JUIN), BNIA, MAINTENANT, null));
        point.modifier(TypeDeclarant.NEGOCIANT, juillet, sixLignes(), false, Optional.empty(), BNIA, MAINTENANT, null);
        assertThat(point.debut()).isEqualTo(juillet);
    }

    @Test
    void une_version_perimee_est_refusee() {
        PointDeDepartDeclaratif point = pointDeJuin(sixLignes());
        assertThatThrownBy(() -> point.modifier(TypeDeclarant.NEGOCIANT, JUIN, List.of(), false, Optional.empty(), BNIA,
                MAINTENANT, 3L)).isInstanceOf(ModificationConcurrente.class);
    }

    @Test
    @DisplayName("RG-DEC-09 : un nouveau point de départ, sans DSM en cours, après la dernière DSM validée")
    void rg_dec_09_nouveau_point_de_depart() {
        PeriodeDeclaration derniere = PERIODE_JUIN;
        refuse("RG-DEC-09", () -> PointDeDepartDeclaratif.fixerApres(OPERATEUR, TypeDeclarant.NEGOCIANT,
                LocalDate.of(2026, 9, 1), List.of(), Optional.of(PERIODE_JUIN.suivante(TypeDeclarant.NEGOCIANT)),
                derniere, BNIA, MAINTENANT));
        refuse("RG-DEC-09", () -> PointDeDepartDeclaratif.fixerApres(OPERATEUR, TypeDeclarant.NEGOCIANT,
                LocalDate.of(2026, 6, 1), List.of(), Optional.empty(), derniere, BNIA, MAINTENANT));
        refuse("RG-DEC-06", () -> PointDeDepartDeclaratif.fixerApres(OPERATEUR, TypeDeclarant.NEGOCIANT,
                LocalDate.of(2026, 9, 15), List.of(), Optional.empty(), derniere, BNIA, MAINTENANT));

        PointDeDepartDeclaratif lendemain = PointDeDepartDeclaratif.fixerApres(OPERATEUR, TypeDeclarant.NEGOCIANT,
                LocalDate.of(2026, 7, 1), sixLignes(), Optional.empty(), derniere, BNIA, MAINTENANT);
        assertThat(lendemain.debut()).isEqualTo(LocalDate.of(2026, 7, 1));
        assertThat(lendemain.lignes()).hasSize(6);
        assertThat(PointDeDepartDeclaratif.fixerApres(OPERATEUR, TypeDeclarant.NEGOCIANT, LocalDate.of(2027, 2, 1),
                List.of(), Optional.empty(), derniere, BNIA, MAINTENANT).debut())
                .as("après une interruption").isEqualTo(LocalDate.of(2027, 2, 1));
    }
}
