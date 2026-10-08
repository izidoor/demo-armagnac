package fr.bnia.referentiels.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import fr.bnia.commun.domain.RegleEnfreinte;
import fr.bnia.referentiels.domain.ClasseTva;
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
            Clock.fixed(Instant.parse("2026-10-06T10:00:00Z"), ZoneOffset.UTC), Parametrage.AUTORISE);

    @Test
    void la_date_du_jour_vient_de_l_horloge() {
        assertThat(gerer.aujourdhui()).isEqualTo(LocalDate.parse("2026-10-06"));
    }

    @Test
    @DisplayName("RG-REF-11 : un taux ajouté est enregistré avec sa classe ; un chevauchement est refusé")
    void rg_ref_11_ajouter_un_taux() {
        long id = gerer.creer(Parametrage.ACTEUR, "NORMAL", "Normal").id();

        gerer.ajouterTaux(Parametrage.ACTEUR, id, new SaisieTauxTva(new BigDecimal("20"), LocalDate.parse("2014-01-01"), null));

        assertThat(classes.parId(id).orElseThrow().taux()).hasSize(1);
        assertThatThrownBy(() -> gerer.ajouterTaux(Parametrage.ACTEUR, id,
                new SaisieTauxTva(new BigDecimal("21"), LocalDate.parse("2026-01-01"), null)))
                .isInstanceOf(RegleEnfreinte.class);
        assertThat(classes.parId(id).orElseThrow().taux()).hasSize(1);
    }

    @Test
    void modifier_puis_retirer_un_taux() {
        long id = gerer.creer(Parametrage.ACTEUR, "NORMAL", "Normal").id();
        gerer.ajouterTaux(Parametrage.ACTEUR, id, new SaisieTauxTva(new BigDecimal("20"), LocalDate.parse("2014-01-01"), null));

        gerer.modifierTaux(Parametrage.ACTEUR, id, LocalDate.parse("2014-01-01"),
                new SaisieTauxTva(new BigDecimal("20"), LocalDate.parse("2014-01-01"), LocalDate.parse("2025-12-31")));
        assertThat(gerer.consulter(id).taux().getFirst().fin()).isEqualTo(LocalDate.parse("2025-12-31"));

        gerer.retirerTaux(Parametrage.ACTEUR, id, LocalDate.parse("2014-01-01"));
        assertThat(gerer.consulter(id).taux()).isEmpty();
    }

    private static final class ClassesEnMemoire extends ValeursEnMemoire<ClasseTva>
            implements ClasseTvaRepository, ClassesTvaLecture {

        ClassesEnMemoire() {
            super((id, c) -> ClasseTva.reconstituer(id, c.code(), c.libelle(), c.estActif(), c.taux()));
        }

        @Override
        public List<ClasseTvaLigne> lister(FiltreValeurs filtre, LocalDate date) {
            throw new UnsupportedOperationException();
        }
    }
}
