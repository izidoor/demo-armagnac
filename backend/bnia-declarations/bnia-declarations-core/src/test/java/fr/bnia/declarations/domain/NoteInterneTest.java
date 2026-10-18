package fr.bnia.declarations.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import fr.bnia.commun.domain.RegleEnfreinte;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class NoteInterneTest {

    private static final Auteur ANNE = new Auteur(20, "Anne Lamarque");
    private static final Instant LE = Instant.parse("2026-10-14T08:00:00Z");

    @Test
    @DisplayName("RG-DEC-24 : une note n'est pas vide ; son auteur seul la modifie ou la supprime")
    void rg_dec_24_note_interne() {
        assertThatThrownBy(() -> NoteInterne.ecrire(1, null, ANNE, "  ", LE)).isInstanceOf(RegleEnfreinte.class)
                .hasMessage("Une note n'est pas vide.");
        NoteInterne note = NoteInterne.ecrire(1, 4L, ANNE, " Sorties export à rapprocher des DAE ", LE);
        assertThat(note.texte()).isEqualTo("Sorties export à rapprocher des DAE");
        assertThatThrownBy(() -> note.modifier(21, "Autre", LE)).isInstanceOf(RegleEnfreinte.class)
                .hasMessage("Seul son auteur, Anne Lamarque, modifie cette note.");
        assertThatThrownBy(() -> note.exigerSupprimable(21)).isInstanceOf(RegleEnfreinte.class);
        note.modifier(20, "DAE reçus, conforme", LE.plusSeconds(60));
        assertThat(note.modifieeLe()).isEqualTo(LE.plusSeconds(60));
        note.exigerSupprimable(20);
    }

    @Test
    void la_note_d_une_ligne_supprimee_passe_sur_la_dsm() {
        NoteInterne note = NoteInterne.ecrire(1, 4L, ANNE, "Vu", LE);
        note.detacher("« dénomination XO, clé NB, compte d'âge 12 »");
        assertThat(note.ligne()).isNull();
        assertThat(note.ligneSupprimee()).isEqualTo("« dénomination XO, clé NB, compte d'âge 12 »");
    }
}
