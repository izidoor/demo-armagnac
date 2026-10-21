package fr.bnia.facturation.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import fr.bnia.commun.domain.RegleEnfreinte;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class NoteInterneTest {

    private static final Auteur ANNE = new Auteur(20, "Anne Lamarque");
    private static final Instant LE = Instant.parse("2026-10-21T08:00:00Z");

    @Test
    @DisplayName("RG-FAC-21 : une note a un texte ; son auteur seul la modifie ou la supprime")
    void rg_fac_21_note_interne() {
        assertThatThrownBy(() -> NoteInterne.ecrire(1, ANNE, "  ", LE)).isInstanceOf(RegleEnfreinte.class)
                .hasMessage("Une note a un texte.");
        NoteInterne note = NoteInterne.ecrire(1, ANNE, " Relancer le comptable ", LE);
        assertThat(note.texte()).isEqualTo("Relancer le comptable");
        assertThatThrownBy(() -> note.modifier(21, "Autre", LE)).isInstanceOf(RegleEnfreinte.class)
                .hasMessage("Seul son auteur, Anne Lamarque, modifie cette note.");
        assertThatThrownBy(() -> note.exigerSupprimable(21)).isInstanceOf(RegleEnfreinte.class);
        note.modifier(20, "Relancé le 21", LE.plusSeconds(60));
        assertThat(note.modifieeLe()).isEqualTo(LE.plusSeconds(60));
        note.exigerSupprimable(20);
    }
}
