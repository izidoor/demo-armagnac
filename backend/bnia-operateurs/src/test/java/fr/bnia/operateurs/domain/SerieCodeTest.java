package fr.bnia.operateurs.domain;

import static fr.bnia.operateurs.domain.partage.TypeOperateur.AUTRE;
import static fr.bnia.operateurs.domain.partage.TypeOperateur.NEGOCIANT;
import static fr.bnia.operateurs.domain.partage.TypeOperateur.PRODUCTEUR;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import java.util.OptionalInt;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class SerieCodeTest {

    @Test
    @DisplayName("RG-OPE-03 : producteur et AUTRE partagent la série à six chiffres")
    void rg_ope_03_producteur_et_autre() {
        SerieCode producteur = SerieCode.pour(PRODUCTEUR, null, false).orElseThrow();

        assertThat(SerieCode.pour(AUTRE, "32100", true)).contains(producteur);
        assertThat(producteur.suivant(OptionalInt.of(41))).contains(CodeOperateur.de("000042"));
        assertThat(producteur.suivant(OptionalInt.empty())).contains(CodeOperateur.de("000001"));
    }

    @ParameterizedTest(name = "{0} → {1}")
    @CsvSource({"32100, N320001", "40000, N400001", "20090, N2A0001", "20250, N2B0001", "97100, N9710001",
            "97600, N9760001", "97700, N970001", "01000, N010001"})
    @DisplayName("RG-OPE-03 : négociant, N, le département du code postal, puis quatre chiffres")
    void rg_ope_03_negociant_par_departement(String codePostal, String premierCode) {
        assertThat(SerieCode.pour(NEGOCIANT, codePostal, true).flatMap(s -> s.suivant(OptionalInt.empty())))
                .contains(CodeOperateur.de(premierCode));
    }

    @Test
    @DisplayName("RG-OPE-03 : négociant, numéro suivant dans son département")
    void rg_ope_03_negociant_numero_suivant() {
        SerieCode gers = SerieCode.pour(NEGOCIANT, "32100", true).orElseThrow();

        assertThat(gers.suivant(OptionalInt.of(3))).contains(CodeOperateur.de("N320004"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "3210", "ABCDE", "20700"})
    @DisplayName("RG-OPE-03 : sans code postal français exploitable, aucun code n'est proposé à un négociant")
    void rg_ope_03_pas_de_proposition_sans_code_postal_francais(String codePostal) {
        assertThat(SerieCode.pour(NEGOCIANT, codePostal, true)).isEmpty();
    }

    @Test
    @DisplayName("RG-OPE-03 : un négociant étranger n'a pas de code proposé")
    void rg_ope_03_negociant_etranger() {
        assertThat(SerieCode.pour(NEGOCIANT, "32100", false)).isEmpty();
    }

    @Test
    @DisplayName("RG-OPE-03 : une série pleine ne propose plus rien")
    void rg_ope_03_serie_pleine() {
        SerieCode gers = SerieCode.pour(NEGOCIANT, "32100", true).orElseThrow();

        assertThat(gers.suivant(OptionalInt.of(9999))).isEqualTo(Optional.empty());
    }

    @Test
    @DisplayName("RG-OPE-05 : un code appartient à la série s'il en a la forme, à la casse près")
    void rg_ope_05_forme_du_code() {
        SerieCode gers = SerieCode.pour(NEGOCIANT, "32100", true).orElseThrow();
        SerieCode producteur = SerieCode.pour(PRODUCTEUR, null, true).orElseThrow();

        assertThat(gers.contient(CodeOperateur.de("n320012"))).isTrue();
        assertThat(gers.contient(CodeOperateur.de("N400012"))).isFalse();
        assertThat(producteur.contient(CodeOperateur.de("000123"))).isTrue();
        assertThat(producteur.contient(CodeOperateur.de("N320012"))).isFalse();
    }
}
