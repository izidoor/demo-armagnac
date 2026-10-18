package fr.bnia.messagerie.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import fr.bnia.commun.domain.DocumentDepose;
import fr.bnia.commun.domain.RegleEnfreinte;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MessageTest {

    private static final Auteur JULIE = new Auteur(10, "Julie Darrigade");
    private static final Auteur LUC = new Auteur(20, "Luc Dubos");
    private static final Instant LE = Instant.parse("2026-10-14T08:00:00Z");

    private static void enfreint(String regle, Runnable appel) {
        assertThatThrownBy(appel::run).isInstanceOf(RegleEnfreinte.class)
                .extracting(e -> ((RegleEnfreinte) e).regle().orElseThrow()).isEqualTo(regle);
    }

    @Test
    @DisplayName("RG-MSG-05 : un message n'est pas vide et compte 4 000 caractères au plus")
    void rg_msg_05_texte() {
        enfreint("RG-MSG-05", () -> Message.ecrire(1, JULIE, Cote.OPERATEUR, "   ", List.of(), LE));
        enfreint("RG-MSG-05", () -> Message.ecrire(1, JULIE, Cote.OPERATEUR, "a".repeat(4001), List.of(), LE));
        assertThat(Message.ecrire(1, JULIE, Cote.OPERATEUR, " " + "a".repeat(4000) + " ", List.of(), LE).texte())
                .hasSize(4000);
    }

    @Test
    @DisplayName("RG-MSG-09 : écrit, un message est lu pour le côté de son auteur, non lu pour l'autre")
    void rg_msg_09_lu_a_l_ecriture() {
        Message deJulie = Message.ecrire(1, JULIE, Cote.OPERATEUR, "Voici les DAE", List.of(), LE);
        assertThat(deJulie.luPar(Cote.OPERATEUR)).isTrue();
        assertThat(deJulie.luPar(Cote.BNIA)).isFalse();
        Message deLuc = Message.ecrire(1, LUC, Cote.BNIA, "Reçus", List.of(), LE);
        assertThat(deLuc.luPar(Cote.BNIA)).isTrue();
        assertThat(deLuc.luPar(Cote.OPERATEUR)).isFalse();
    }

    @Test
    @DisplayName("RG-MSG-10 : marquer non lu ne vaut que pour le côté qui le fait")
    void rg_msg_10_marquer_non_lu() {
        Message message = Message.ecrire(1, JULIE, Cote.OPERATEUR, "Voici les DAE", List.of(), LE);
        message.marquerNonLu(Cote.OPERATEUR);
        assertThat(message.luPar(Cote.OPERATEUR)).isFalse();
        assertThat(message.luPar(Cote.BNIA)).isFalse();
    }

    @Test
    @DisplayName("RG-MSG-06 : l'auteur seul modifie (« modifié ») ou supprime ; le protégé se modifie sans se supprimer")
    void rg_msg_06_modifier_supprimer() {
        Message message = Message.ecrire(1, JULIE, Cote.OPERATEUR, "Les DAE arrivent demain", List.of(), LE);
        assertThatThrownBy(() -> message.modifier(20, "Autre", LE)).isInstanceOf(RegleEnfreinte.class)
                .hasMessage("Seul son auteur, Julie Darrigade, modifie ce message.");
        enfreint("RG-MSG-06", () -> message.exigerSupprimable(20));
        message.modifier(10, "Les DAE arrivent lundi", LE.plusSeconds(3600));
        assertThat(message.modifieLe()).isEqualTo(LE.plusSeconds(3600));
        assertThat(message.luPar(Cote.BNIA)).as("la modification ne change pas la lecture").isFalse();
        message.exigerSupprimable(10);

        Message motif = Message.motif(1, LUC, "DAE manquants pour l'export", LE);
        assertThat(motif.protege()).isTrue();
        assertThat(motif.cote()).isEqualTo(Cote.BNIA);
        motif.modifier(20, "DAE manquants pour l'export de septembre", LE);
        assertThatThrownBy(() -> motif.exigerSupprimable(20)).isInstanceOf(RegleEnfreinte.class)
                .hasMessage("Le motif d'une demande de compléments ne se supprime pas ; vous pouvez le modifier.");
    }

    @Test
    @DisplayName("RG-MSG-14 : un message porte au plus cinq pièces jointes")
    void rg_msg_14_cinq_pieces() {
        List<DocumentDepose> six = Collections.nCopies(6, new DocumentDepose(1, "a.pdf", "application/pdf", 10));
        enfreint("RG-MSG-14", () -> Message.ecrire(1, JULIE, Cote.OPERATEUR, "Pièces", six, LE));
        assertThat(Message.ecrire(1, JULIE, Cote.OPERATEUR, "Pièces", six.subList(0, 5), LE).pieces()).hasSize(5);
    }

    @Test
    @DisplayName("RG-MSG-12 : une conversation libre a un titre, 120 caractères au plus")
    void rg_msg_12_titre() {
        enfreint("RG-MSG-12", () -> new SujetLibre(" ", JULIE));
        enfreint("RG-MSG-12", () -> new SujetLibre("t".repeat(121), JULIE));
        assertThat(new SujetLibre(" Import de la matrice refusé ", JULIE).titre()).isEqualTo("Import de la matrice refusé");
    }
}
