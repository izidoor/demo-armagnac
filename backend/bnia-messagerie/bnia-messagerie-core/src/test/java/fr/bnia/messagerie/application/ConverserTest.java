package fr.bnia.messagerie.application;

import static fr.bnia.messagerie.application.Monde.ADMIN;
import static fr.bnia.messagerie.application.Monde.AGENT;
import static fr.bnia.messagerie.application.Monde.AGENT_BNIA;
import static fr.bnia.messagerie.application.Monde.DSM;
import static fr.bnia.messagerie.application.Monde.FACTURE;
import static fr.bnia.messagerie.application.Monde.INCONNU;
import static fr.bnia.messagerie.application.Monde.NEGOCIANT;
import static fr.bnia.messagerie.application.Monde.OBSERVATEUR;
import static fr.bnia.messagerie.application.Monde.OBSERVATEUR_BNIA;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import fr.bnia.commun.domain.AccesRefuse;
import fr.bnia.commun.domain.ElementIntrouvable;
import fr.bnia.commun.domain.RegleEnfreinte;
import fr.bnia.messagerie.api.Acteur;
import fr.bnia.messagerie.api.ObjetRef;
import fr.bnia.messagerie.api.TypeObjet;
import fr.bnia.messagerie.domain.Cote;
import fr.bnia.messagerie.domain.Message;
import java.util.List;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Conversations d'objet : écrire, lire, modifier, supprimer (spec 15, lots 2 et 3). */
class ConverserTest {

    private final Monde monde = new Monde();
    private final Converser converser = monde.converser();

    private static void refuse(String regle, ThrowingCallable appel) {
        assertThatThrownBy(appel).isInstanceOf(AccesRefuse.class)
                .extracting(e -> ((RegleEnfreinte) e).regle().orElseThrow()).isEqualTo(regle);
    }

    @Test
    @DisplayName("RG-MSG-01 : lire l'objet pour lire sa conversation, le gérer pour y écrire ; la lecture suffit sur une facture")
    void rg_msg_01_droits_par_type() {
        converser.ecrireSurObjet(AGENT, DSM, "Voici les DAE");
        assertThat(converser.consulterObjet(OBSERVATEUR, DSM).peutEcrire()).isFalse();
        refuse("RG-MSG-01", () -> converser.ecrireSurObjet(OBSERVATEUR, DSM, "Note"));
        refuse("RG-MSG-01", () -> converser.ecrireSurObjet(OBSERVATEUR_BNIA, DSM, "Note"));
        refuse("RG-MSG-01", () -> converser.consulterObjet(INCONNU, DSM));
        assertThat(converser.consulterObjet(OBSERVATEUR_BNIA, DSM).messages()).hasSize(1);

        assertThat(converser.consulterObjet(OBSERVATEUR, FACTURE).peutEcrire()).as("client en lecture seule").isTrue();
        Message question = converser.ecrireSurObjet(OBSERVATEUR, FACTURE, "Le volume facturé est faux");
        assertThat(question.cote()).isEqualTo(Cote.OPERATEUR);
        refuse("RG-MSG-01", () -> converser.ecrireSurObjet(INCONNU, FACTURE, "Note"));
    }

    @Test
    @DisplayName("RG-MSG-03 : le côté d'un message est celui de son auteur")
    void rg_msg_03_cote() {
        assertThat(converser.ecrireSurObjet(AGENT, DSM, "Voici les DAE").cote()).isEqualTo(Cote.OPERATEUR);
        assertThat(converser.ecrireSurObjet(AGENT_BNIA, DSM, "Reçus").cote()).isEqualTo(Cote.BNIA);
        assertThat(converser.consulterObjet(AGENT_BNIA, DSM).cote()).isEqualTo(Cote.BNIA);
    }

    @Test
    @DisplayName("RG-MSG-04 : au plus une conversation par objet, née au premier message ; un objet inconnu est introuvable")
    void rg_msg_04_conversation_d_objet() {
        var vide = converser.consulterObjet(AGENT, DSM);
        assertThat(vide.conversation()).isEmpty();
        assertThat(vide.sujet().intitule()).isEqualTo("DSM octobre 2026");
        assertThat(vide.peutEcrire()).isTrue();
        assertThat(monde.conversations.parId).isEmpty();

        converser.ecrireSurObjet(AGENT, DSM, "Voici les DAE");
        converser.ecrireSurObjet(AGENT_BNIA, DSM, "Reçus");
        assertThat(monde.conversations.parId).hasSize(1);
        assertThat(converser.consulterObjet(AGENT, DSM).messages()).hasSize(2);
        assertThat(monde.conversations.parId.values().iterator().next().operateur()).isEqualTo(NEGOCIANT);

        assertThatThrownBy(() -> converser.ecrireSurObjet(AGENT, ObjetRef.de(TypeObjet.DSM, 99), "Note"))
                .isInstanceOf(ElementIntrouvable.class).hasMessage("DSM introuvable : n° 99.");
    }

    @Test
    @DisplayName("RG-MSG-05 : un message vide est refusé")
    void rg_msg_05_message_vide() {
        assertThatThrownBy(() -> converser.ecrireSurObjet(AGENT, DSM, " ")).isInstanceOf(RegleEnfreinte.class)
                .hasMessage("Un message n'est pas vide.");
    }

    @Test
    @DisplayName("RG-MSG-06 : l'auteur modifie ou supprime le sien ; le dernier supprimé emporte la conversation")
    void rg_msg_06_modifier_supprimer() {
        Message message = converser.ecrireSurObjet(AGENT, DSM, "Les DAE arrivent demain");
        assertThatThrownBy(() -> converser.modifier(ADMIN, message.id(), "Autre")).isInstanceOf(RegleEnfreinte.class);
        assertThat(converser.modifier(AGENT, message.id(), "Les DAE arrivent lundi").modifieLe()).isNotNull();
        assertThatThrownBy(() -> converser.supprimer(ADMIN, message.id())).isInstanceOf(RegleEnfreinte.class);
        converser.supprimer(AGENT, message.id());
        assertThat(monde.conversations.parId).isEmpty();
        refuse("RG-MSG-01", () -> converser.supprimer(INCONNU, converser.ecrireSurObjet(AGENT, DSM, "Bis").id()));
    }

    @Test
    @DisplayName("RG-MSG-07, 08 : le motif déposé est protégé, côté BNIA ; la conversation disparaît avec son objet")
    void rg_msg_07_08_motif_et_suppression() {
        ConversationsDesObjets objets = monde.conversationsDesObjets();
        objets.deposerMotif(DSM, AGENT_BNIA, "DAE manquants pour l'export");
        var vue = converser.consulterObjet(AGENT, DSM);
        assertThat(vue.messages()).singleElement().satisfies(m -> {
            assertThat(m.message().protege()).isTrue();
            assertThat(m.message().cote()).isEqualTo(Cote.BNIA);
            assertThat(m.message().auteur().nom()).isEqualTo("Utilisateur 20");
            assertThat(m.nonLu()).isTrue();
        });
        long motif = vue.messages().getFirst().message().id();
        assertThatThrownBy(() -> converser.supprimer(AGENT_BNIA, motif)).isInstanceOf(RegleEnfreinte.class)
                .extracting(e -> ((RegleEnfreinte) e).regle().orElseThrow()).isEqualTo("RG-MSG-06");

        objets.supprimer(DSM);
        assertThat(monde.conversations.parId).isEmpty();
        assertThat(monde.messages.parId).isEmpty();
        objets.supprimer(DSM);
    }

    @Test
    @DisplayName("RG-MSG-09, 10 : afficher lit pour tout le côté ; marquer non lu remet le message compté")
    void rg_msg_09_10_lecture_par_cote() {
        converser.ecrireSurObjet(AGENT, DSM, "Voici les DAE");
        converser.ecrireSurObjet(AGENT, DSM, "J'oubliais la DRM");
        assertThat(converser.consulterObjet(AGENT, DSM).messages()).noneMatch(VueConversation.MessageVu::nonLu);

        List<VueConversation.MessageVu> vus = converser.consulterObjet(OBSERVATEUR_BNIA, DSM).messages();
        assertThat(vus).allMatch(VueConversation.MessageVu::nonLu);
        assertThat(converser.consulterObjet(AGENT_BNIA, DSM).messages()).as("lu pour tout le BNIA")
                .noneMatch(VueConversation.MessageVu::nonLu);

        converser.marquerNonLu(AGENT_BNIA, vus.getLast().message().id());
        assertThat(monde.messages.parId(vus.getLast().message().id()).orElseThrow().luPar(Cote.BNIA)).isFalse();
        assertThat(monde.messages.parId(vus.getLast().message().id()).orElseThrow().luPar(Cote.OPERATEUR)).isTrue();
        assertThat(converser.consulterObjet(OBSERVATEUR_BNIA, DSM).messages()).filteredOn(VueConversation.MessageVu::nonLu)
                .hasSize(1);
        refuse("RG-MSG-01", () -> converser.marquerNonLu(new Acteur(99), vus.getLast().message().id()));
    }
}
