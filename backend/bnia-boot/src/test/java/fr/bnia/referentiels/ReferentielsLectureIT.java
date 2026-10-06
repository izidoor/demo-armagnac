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

/** Partie publiée en lecture (ADR-008, ADR-011) : ce que les autres contextes voient des référentiels. */
class ReferentielsLectureIT extends AbstractIT {

    private static final String RACINE = "/api/referentiels/";

    @Autowired
    ReferentielsLecture lecture;

    static Stream<Arguments> listesSimples() {
        return Stream.of(
                liste("zones-commerciales", ReferentielsLecture::zoneCommerciale,
                        ReferentielsLecture::zoneCommercialeParCode, ReferentielsLecture::zonesCommercialesActives),
                liste("titres", ReferentielsLecture::titre, ReferentielsLecture::titreParCode,
                        ReferentielsLecture::titresActifs),
                liste("labels", ReferentielsLecture::label, ReferentielsLecture::labelParCode,
                        ReferentielsLecture::labelsActifs),
                liste("types-visite", ReferentielsLecture::typeVisite, ReferentielsLecture::typeVisiteParCode,
                        ReferentielsLecture::typesVisiteActifs),
                liste("langues", ReferentielsLecture::langue, ReferentielsLecture::langueParCode,
                        ReferentielsLecture::languesActives),
                liste("centres-interet", ReferentielsLecture::centreInteret, ReferentielsLecture::centreInteretParCode,
                        ReferentielsLecture::centresInteretActifs),
                liste("marques", ReferentielsLecture::marque, ReferentielsLecture::marqueParCode,
                        ReferentielsLecture::marquesActives),
                liste("circuits-distribution", ReferentielsLecture::circuitDistribution,
                        ReferentielsLecture::circuitDistributionParCode, ReferentielsLecture::circuitsDistributionActifs),
                liste("produits-commercialises", ReferentielsLecture::produitCommercialise,
                        ReferentielsLecture::produitCommercialiseParCode,
                        ReferentielsLecture::produitsCommercialisesActifs),
                liste("classes-tva", ReferentielsLecture::classeTva, ReferentielsLecture::classeTvaParCode,
                        ReferentielsLecture::classesTvaActives));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("listesSimples")
    @DisplayName("RG-REF-04 : une valeur inactive reste lisible, par identifiant et par code, mais sort des valeurs actives")
    void rg_ref_04_valeur_inactive_lisible_mais_pas_proposee(String chemin,
            BiFunction<ReferentielsLecture, Long, Optional<ValeurVue>> parId,
            BiFunction<ReferentielsLecture, String, Optional<ValeurVue>> parCode,
            Function<ReferentielsLecture, List<ValeurVue>> actives) {
        String code = "L" + suffixe();
        long active = creer(RACINE + chemin, Map.of("code", code, "libelle", "Valeur " + code));
        long inactive = creer(RACINE + chemin, Map.of("code", code + "-I", "libelle", "Valeur " + code + " inactive"));
        client.post().uri(RACINE + chemin + "/{id}/desactivation", inactive).exchange().expectStatus().isOk();

        assertThat(parId.apply(lecture, active)).hasValueSatisfying(v -> assertThat(v.code()).isEqualTo(code));
        assertThat(parId.apply(lecture, inactive)).hasValueSatisfying(v -> assertThat(v.actif()).isFalse());
        assertThat(parId.apply(lecture, Long.MAX_VALUE)).isEmpty();
        assertThat(parCode.apply(lecture, code.toLowerCase())).hasValueSatisfying(v -> assertThat(v.id()).isEqualTo(active));
        assertThat(actives.apply(lecture)).extracting(ValeurVue::id).contains(active).doesNotContain(inactive);
    }

    @Test
    void un_pays_expose_ses_attributs_pour_l_export() {
        String suffixe = suffixe();
        long zone = creer(RACINE + "zones-commerciales", Map.of("code", "Z" + suffixe, "libelle", "Zone " + suffixe));
        long id = creer(RACINE + "pays", Map.of("code", "P" + suffixe, "libelle", "Espagne " + suffixe,
                "codeIso", "YE", "zoneCommerciale", zone, "membreUe", true, "langueCertificat", "FR_ES",
                "ageMinimum", "00"));

        var pays = lecture.pays(id).orElseThrow();

        assertThat(pays.zoneCommerciale()).isEqualTo(zone);
        assertThat(pays.membreUe()).isTrue();
        assertThat(pays.langueCertificat()).isEqualTo(LangueCertificat.FR_ES);
        assertThat(pays.niveauDetailCertificat()).isEqualTo(NiveauDetailCertificat.CUMUL_DEGRE);
        assertThat(pays.ageMinimum()).contains(CompteDAge.DOUBLE_ZERO);
        assertThat(lecture.paysParCode("p" + suffixe.toLowerCase())).map(p -> p.id()).contains(id);
        assertThat(lecture.paysActifs()).extracting(p -> p.id()).contains(id);
    }

    @Test
    void une_region_expose_l_indicateur_de_region_delimitee() {
        String code = "R" + suffixe();
        long id = creer(RACINE + "regions", Map.of("code", code, "libelle", "Gers " + code, "enRegionDelimitee", true));

        assertThat(lecture.region(id).orElseThrow().enRegionDelimitee()).isTrue();
        assertThat(lecture.regionParCode(code)).isPresent();
        assertThat(lecture.regionsActives()).extracting(r -> r.id()).contains(id);
    }

    @Test
    void une_contenance_se_lit_par_identifiant_ou_par_volume() {
        long id = creer(RACINE + "contenances", Map.of("volume", "8001.25"));

        var contenance = lecture.contenance(id).orElseThrow();

        assertThat(contenance.volume()).isEqualByComparingTo("8001.25");
        assertThat(contenance.libelle()).isEqualTo("8001,25 L");
        assertThat(lecture.contenanceParVolume(new BigDecimal("8001.250"))).map(c -> c.id()).contains(id);
        assertThat(lecture.contenanceParVolume(new BigDecimal("8001.251"))).isEmpty();
        assertThat(lecture.contenancesActives()).extracting(c -> c.id()).contains(id);
    }

    @Test
    @DisplayName("RG-REF-12 : le taux applicable à une date, ou aucun ; RG-REF-13 : même pour une classe inactive")
    void rg_ref_12_et_13_taux_applicable() {
        String code = "T" + suffixe();
        long classe = creer(RACINE + "classes-tva", Map.of("code", code, "libelle", "Classe " + code));
        client.post().uri(RACINE + "classes-tva/{id}/taux", classe)
                .body(Map.of("taux", "20", "dateDebut", "2014-01-01", "dateFin", "2019-12-31"))
                .exchange().expectStatus().isOk();
        client.post().uri(RACINE + "classes-tva/{id}/desactivation", classe).exchange().expectStatus().isOk();

        var taux = lecture.tauxTva(classe, LocalDate.parse("2019-12-31")).orElseThrow();

        assertThat(taux.classeTva()).isEqualTo(classe);
        assertThat(taux.taux()).isEqualByComparingTo("20");
        assertThat(taux.debut()).isEqualTo(LocalDate.parse("2014-01-01"));
        assertThat(taux.fin()).contains(LocalDate.parse("2019-12-31"));
        assertThat(lecture.tauxTva(classe, LocalDate.parse("2020-01-01"))).isEmpty();
        assertThat(lecture.tauxTva(Long.MAX_VALUE, LocalDate.parse("2016-01-01"))).isEmpty();
    }

    private static Arguments liste(String chemin, BiFunction<ReferentielsLecture, Long, Optional<ValeurVue>> parId,
            BiFunction<ReferentielsLecture, String, Optional<ValeurVue>> parCode,
            Function<ReferentielsLecture, List<ValeurVue>> actives) {
        return Arguments.of(chemin, parId, parCode, actives);
    }

    private static String suffixe() {
        return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
