package fr.bnia.boot.demo;

import static org.assertj.core.api.Assertions.assertThat;

import fr.bnia.referentiels.application.GererCentresInteret;
import fr.bnia.referentiels.application.GererCircuitsDistribution;
import fr.bnia.referentiels.application.GererClassesTva;
import fr.bnia.referentiels.application.GererContenances;
import fr.bnia.referentiels.application.GererLabels;
import fr.bnia.referentiels.application.GererLangues;
import fr.bnia.referentiels.application.GererMarques;
import fr.bnia.referentiels.application.GererPays;
import fr.bnia.referentiels.application.GererProduitsCommercialises;
import fr.bnia.referentiels.application.GererRegions;
import fr.bnia.referentiels.application.GererTitres;
import fr.bnia.referentiels.application.GererTypesVisite;
import fr.bnia.referentiels.application.GererZonesCommerciales;
import fr.bnia.referentiels.application.partage.ReferentielsLecture;
import fr.bnia.referentiels.domain.partage.LangueCertificat;
import fr.bnia.support.AbstractIT;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.DefaultApplicationArguments;

/**
 * Le chargeur de démo passe par les cas d'usage et peut être relancé sans erreur. Il est construit à la
 * main : le profil {@code demo} n'est pas actif dans les IT.
 */
class DemoReferentielsIT extends AbstractIT {

    @Autowired GererZonesCommerciales zones;
    @Autowired GererPays pays;
    @Autowired GererRegions regions;
    @Autowired GererTitres titres;
    @Autowired GererLabels labels;
    @Autowired GererTypesVisite typesVisite;
    @Autowired GererLangues langues;
    @Autowired GererCentresInteret centresInteret;
    @Autowired GererMarques marques;
    @Autowired GererCircuitsDistribution circuits;
    @Autowired GererProduitsCommercialises produits;
    @Autowired GererContenances contenances;
    @Autowired GererClassesTva classesTva;
    @Autowired ReferentielsLecture lecture;

    @Test
    void charge_les_referentiels_et_peut_etre_relance() {
        var demo = new DemoReferentiels(zones, pays, regions, titres, labels, typesVisite, langues, centresInteret,
                marques, circuits, produits, contenances, classesTva, lecture);

        demo.run(new DefaultApplicationArguments());
        demo.run(new DefaultApplicationArguments());

        assertThat(lecture.zonesCommercialesActives()).extracting(v -> v.code()).contains("EUR", "OCE");
        assertThat(lecture.paysActifs()).hasSizeGreaterThanOrEqualTo(30);
        assertThat(lecture.paysParCode("ES").orElseThrow().langueCertificat()).isEqualTo(LangueCertificat.FR_ES);
        assertThat(lecture.paysParCode("IT").orElseThrow().langueCertificat()).isEqualTo(LangueCertificat.FR_IT);
        assertThat(lecture.paysParCode("DE").orElseThrow().membreUe()).isTrue();
        assertThat(lecture.paysParCode("CH").orElseThrow().membreUe()).isFalse();
        assertThat(lecture.paysParCode("US").orElseThrow().zoneCommerciale())
                .isEqualTo(lecture.zoneCommercialeParCode("AMN").orElseThrow().id());
        assertThat(lecture.regionParCode("GERS").orElseThrow().enRegionDelimitee()).isTrue();
        assertThat(lecture.regionParCode("GIRONDE").orElseThrow().enRegionDelimitee()).isFalse();
        assertThat(lecture.contenanceParVolume(new BigDecimal("0.7")).orElseThrow().libelle()).isEqualTo("0,70 L");
        long normal = lecture.classeTvaParCode("NORMAL").orElseThrow().id();
        assertThat(lecture.tauxTva(normal, LocalDate.of(2026, 1, 1)).orElseThrow().taux()).isEqualByComparingTo("20");
        assertThat(classesTva.consulter(normal).taux()).hasSize(1);
    }
}
