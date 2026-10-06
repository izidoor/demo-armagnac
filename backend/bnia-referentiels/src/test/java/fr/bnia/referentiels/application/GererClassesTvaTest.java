package fr.bnia.referentiels.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import fr.bnia.commun.domain.RegleEnfreinte;
import fr.bnia.referentiels.domain.ClasseTva;
import fr.bnia.referentiels.domain.Code;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class GererClassesTvaTest {

    private final ClassesEnMemoire classes = new ClassesEnMemoire();
    private final GererClassesTva gerer = new GererClassesTva(classes, classes,
            Clock.fixed(Instant.parse("2026-10-06T10:00:00Z"), ZoneOffset.UTC));

    @Test
    void la_date_du_jour_vient_de_l_horloge() {
        assertThat(gerer.aujourdhui()).isEqualTo(LocalDate.parse("2026-10-06"));
    }

    @Test
    @DisplayName("RG-REF-11 : un taux ajouté est enregistré avec sa classe ; un chevauchement est refusé")
    void rg_ref_11_ajouter_un_taux() {
        gerer.creer("NORMAL", "Normal");

        gerer.ajouterTaux("NORMAL", new SaisieTauxTva(new BigDecimal("20"), LocalDate.parse("2014-01-01"), null));

        assertThat(classes.parCode(Code.de("NORMAL")).orElseThrow().taux()).hasSize(1);
        assertThatThrownBy(() -> gerer.ajouterTaux("NORMAL",
                new SaisieTauxTva(new BigDecimal("21"), LocalDate.parse("2026-01-01"), null)))
                .isInstanceOf(RegleEnfreinte.class);
        assertThat(classes.parCode(Code.de("NORMAL")).orElseThrow().taux()).hasSize(1);
    }

    @Test
    void modifier_puis_retirer_un_taux() {
        gerer.creer("NORMAL", "Normal");
        gerer.ajouterTaux("NORMAL", new SaisieTauxTva(new BigDecimal("20"), LocalDate.parse("2014-01-01"), null));

        gerer.modifierTaux("NORMAL", LocalDate.parse("2014-01-01"),
                new SaisieTauxTva(new BigDecimal("20"), LocalDate.parse("2014-01-01"), LocalDate.parse("2025-12-31")));
        assertThat(gerer.consulter("NORMAL").taux().getFirst().fin()).isEqualTo(LocalDate.parse("2025-12-31"));

        gerer.retirerTaux("NORMAL", LocalDate.parse("2014-01-01"));
        assertThat(gerer.consulter("NORMAL").taux()).isEmpty();
    }

    private static final class ClassesEnMemoire extends ValeursEnMemoire<ClasseTva>
            implements ClasseTvaRepository, ClassesTvaLecture {

        @Override
        public List<ClasseTvaLigne> lister(FiltreValeurs filtre, LocalDate date) {
            throw new UnsupportedOperationException();
        }
    }
}
