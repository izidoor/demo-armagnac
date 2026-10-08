package fr.bnia.boot.demo;

import static org.assertj.core.api.Assertions.assertThat;

import fr.bnia.referentiels.application.GererClassesTva;
import fr.bnia.referentiels.application.partage.ReferentielsLecture;
import fr.bnia.referentiels.domain.partage.LangueCertificat;
import fr.bnia.support.AbstractIT;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.context.ApplicationContext;

/**
 * Le chargeur de démo passe par les cas d'usage, au nom d'Anne Lamarque (ADMIN du BNIA, créée par
 * {@link DemoPersonnelBnia}), et peut être relancé sans erreur. Il est construit à la main : le profil
 * {@code demo} n'est pas actif dans les IT.
 */
class DemoReferentielsIT extends AbstractIT {

    @Autowired GererClassesTva classesTva;
    @Autowired ReferentielsLecture lecture;
    @Autowired ApplicationContext contexte;

    @Test
    void charge_les_referentiels_et_peut_etre_relance() {
        var fabrique = contexte.getAutowireCapableBeanFactory();
        fabrique.createBean(DemoPersonnelBnia.class).run(new DefaultApplicationArguments());
        var demo = fabrique.createBean(DemoReferentiels.class);

        demo.run(new DefaultApplicationArguments());
        demo.run(new DefaultApplicationArguments());

        assertThat(lecture.zonesCommercialesActives()).extracting(v -> v.code()).contains("EUR", "OCE");
        assertThat(lecture.membresBniaActifs()).extracting(v -> v.code()).contains("PROD", "NEGO");
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
