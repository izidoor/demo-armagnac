package fr.bnia.referentiels;

import static org.assertj.core.api.Assertions.assertThat;

import fr.bnia.commun.domain.CompteDAge;
import fr.bnia.referentiels.application.partage.ReferentielsLecture;
import fr.bnia.referentiels.domain.partage.LangueCertificat;
import fr.bnia.referentiels.domain.partage.NiveauDetailCertificat;
import fr.bnia.referentiels.domain.partage.ValeurVue;
import fr.bnia.support.AbstractIT;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.client.RestTestClient;

/** Partie publiée en lecture (ADR-008) : ce que les autres contextes voient des référentiels. */
class ReferentielsLectureIT extends AbstractIT {

    private static final String RACINE = "/api/referentiels/";

    @Autowired
    RestTestClient client;

    @Autowired
    ReferentielsLecture lecture;

    static Stream<Arguments> listesSimples() {
        return Stream.of(
                liste("zones-commerciales", ReferentielsLecture::zoneCommerciale, ReferentielsLecture::zonesCommercialesActives),
                liste("titres", ReferentielsLecture::titre, ReferentielsLecture::titresActifs),
                liste("labels", ReferentielsLecture::label, ReferentielsLecture::labelsActifs),
                liste("types-visite", ReferentielsLecture::typeVisite, ReferentielsLecture::typesVisiteActifs),
                liste("langues", ReferentielsLecture::langue, ReferentielsLecture::languesActives),
                liste("centres-interet", ReferentielsLecture::centreInteret, ReferentielsLecture::centresInteretActifs),
                liste("marques", ReferentielsLecture::marque, ReferentielsLecture::marquesActives),
                liste("circuits-distribution", ReferentielsLecture::circuitDistribution, ReferentielsLecture::circuitsDistributionActifs),
                liste("produits-commercialises", ReferentielsLecture::produitCommercialise, ReferentielsLecture::produitsCommercialisesActifs),
                liste("classes-tva", ReferentielsLecture::classeTva, ReferentielsLecture::classesTvaActives));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("listesSimples")
    @DisplayName("RG-REF-04 : une valeur inactive reste lisible par son code, mais sort des valeurs actives")
    void rg_ref_04_valeur_inactive_lisible_mais_pas_proposee(String chemin,
            BiFunction<ReferentielsLecture, String, Optional<ValeurVue>> parCode,
            Function<ReferentielsLecture, List<ValeurVue>> actives) {
        String active = creer(chemin);
        String inactive = creer(chemin);
        client.post().uri(RACINE + chemin + "/{code}/desactivation", inactive).exchange().expectStatus().isOk();

        assertThat(parCode.apply(lecture, active)).hasValueSatisfying(v -> assertThat(v.actif()).isTrue());
        assertThat(parCode.apply(lecture, inactive)).hasValueSatisfying(v -> assertThat(v.actif()).isFalse());
        assertThat(parCode.apply(lecture, "INCONNU")).isEmpty();
        assertThat(actives.apply(lecture)).extracting(ValeurVue::code).contains(active).doesNotContain(inactive);
    }

    @Test
    void un_pays_expose_ses_attributs_pour_l_export() {
        String zone = creer("zones-commerciales");
        String code = "P" + suffixe();
        client.post().uri(RACINE + "pays").body(Map.of("code", code, "libelle", "Espagne " + code, "codeIso", "YE",
                        "zoneCommerciale", zone, "membreUe", true, "langueCertificat", "FR_ES", "ageMinimum", "00"))
                .exchange().expectStatus().isCreated();

        var pays = lecture.pays(code).orElseThrow();

        assertThat(pays.zoneCommerciale()).isEqualTo(zone);
        assertThat(pays.membreUe()).isTrue();
        assertThat(pays.langueCertificat()).isEqualTo(LangueCertificat.FR_ES);
        assertThat(pays.niveauDetailCertificat()).isEqualTo(NiveauDetailCertificat.CUMUL_DEGRE);
        assertThat(pays.ageMinimum()).contains(CompteDAge.DOUBLE_ZERO);
        assertThat(lecture.paysActifs()).extracting(p -> p.code()).contains(code);
    }

    @Test
    void une_region_expose_l_indicateur_de_region_delimitee() {
        String code = "R" + suffixe();
        client.post().uri(RACINE + "regions").body(Map.of("code", code, "libelle", "Gers " + code, "enRegionDelimitee", true))
                .exchange().expectStatus().isCreated();

        assertThat(lecture.region(code).orElseThrow().enRegionDelimitee()).isTrue();
        assertThat(lecture.regionsActives()).extracting(r -> r.code()).contains(code);
    }

    @Test
    void une_contenance_se_lit_par_son_volume_quelle_que_soit_son_ecriture() {
        client.post().uri(RACINE + "contenances").body(Map.of("volume", "8001.25"))
                .exchange().expectStatus().isCreated();

        var contenance = lecture.contenance(new BigDecimal("8001.250")).orElseThrow();

        assertThat(contenance.volume()).isEqualByComparingTo("8001.25");
        assertThat(contenance.libelle()).isEqualTo("8001,25 L");
        assertThat(lecture.contenance(new BigDecimal("8001.251"))).isEmpty();
        assertThat(lecture.contenancesActives()).extracting(c -> c.libelle()).contains("8001,25 L");
    }

    @Test
    @DisplayName("RG-REF-12 : le taux applicable à une date, ou aucun ; RG-REF-13 : même pour une classe inactive")
    void rg_ref_12_et_13_taux_applicable() {
        String classe = creer("classes-tva");
        client.post().uri(RACINE + "classes-tva/{code}/taux", classe)
                .body(Map.of("taux", "20", "dateDebut", "2014-01-01", "dateFin", "2019-12-31"))
                .exchange().expectStatus().isOk();
        client.post().uri(RACINE + "classes-tva/{code}/desactivation", classe).exchange().expectStatus().isOk();

        var taux = lecture.tauxTva(classe, LocalDate.parse("2019-12-31")).orElseThrow();

        assertThat(taux.taux()).isEqualByComparingTo("20");
        assertThat(taux.debut()).isEqualTo(LocalDate.parse("2014-01-01"));
        assertThat(taux.fin()).contains(LocalDate.parse("2019-12-31"));
        assertThat(lecture.tauxTva(classe, LocalDate.parse("2020-01-01"))).isEmpty();
        assertThat(lecture.tauxTva("INCONNUE", LocalDate.parse("2016-01-01"))).isEmpty();
    }

    private String creer(String chemin) {
        String code = "L" + suffixe();
        client.post().uri(RACINE + chemin).body(Map.of("code", code, "libelle", "Valeur " + code))
                .exchange().expectStatus().isCreated();
        return code;
    }

    private static Arguments liste(String chemin, BiFunction<ReferentielsLecture, String, Optional<ValeurVue>> parCode,
            Function<ReferentielsLecture, List<ValeurVue>> actives) {
        return Arguments.of(chemin, parCode, actives);
    }

    private static String suffixe() {
        return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
