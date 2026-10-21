package fr.bnia.messagerie.application;

import static fr.bnia.messagerie.application.Monde.ADMIN;
import static fr.bnia.messagerie.application.Monde.AGENT;
import static fr.bnia.messagerie.application.Monde.AGENT_BNIA;
import static fr.bnia.messagerie.application.Monde.DSM;
import static fr.bnia.messagerie.application.Monde.NEGOCIANT;
import static fr.bnia.messagerie.application.Monde.OBSERVATEUR;
import static fr.bnia.messagerie.application.Monde.OBSERVATEUR_BNIA;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import fr.bnia.commun.domain.AccesRefuse;
import fr.bnia.commun.domain.DocumentRefuse;
import fr.bnia.commun.domain.RegleEnfreinte;
import fr.bnia.messagerie.domain.Cote;
import fr.bnia.messagerie.domain.Message;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Conversations libres et pièces jointes (spec 15, lot 5). */
class ConversationsLibresTest {

    private static final FichierJoint MATRICE = new FichierJoint("matrice.xlsx",
            "PK\u0003\u0004…".getBytes(StandardCharsets.ISO_8859_1));
    private static final FichierJoint PDF = new FichierJoint("attestation.pdf",
            "%PDF-1.7".getBytes(StandardCharsets.ISO_8859_1));

    private final Monde monde = new Monde();
    private final Converser converser = monde.converser();

    private static void enfreint(String regle, ThrowingCallable appel) {
        assertThatThrownBy(appel).isInstanceOf(RegleEnfreinte.class)
                .extracting(e -> ((RegleEnfreinte) e).regle().orElseThrow()).isEqualTo(regle);
    }

    private long ouvrir() {
        return converser.ouvrirLibre(AGENT, NEGOCIANT, "Import de la matrice refusé", "Voici ma matrice.",
                List.of(MATRICE)).conversation().orElseThrow().id();
    }

    @Test
    @DisplayName("RG-MSG-02 : côté opérateur, son seul auteur la voit, ADMIN compris exclu ; tout le BNIA la voit, ses agents et administrateurs y répondent")
    void rg_msg_02_confidentialite() {
        long id = ouvrir();
        assertThat(converser.consulter(AGENT, id).peutEcrire()).isTrue();
        for (var autre : List.of(ADMIN, OBSERVATEUR)) {
            assertThatThrownBy(() -> converser.consulter(autre, id)).isInstanceOf(AccesRefuse.class)
                    .hasMessage("La conversation « Import de la matrice refusé » n'est visible que de Utilisateur 10"
                            + " et du BNIA.");
        }
        var vueObservateur = converser.consulter(OBSERVATEUR_BNIA, id);
        assertThat(vueObservateur.peutEcrire()).as("OBSERVATEUR lit le module Messagerie sans le gérer").isFalse();
        assertThat(vueObservateur.lectureSeule()).contains("Votre rôle permet de lire cette conversation, pas d'y écrire.");
        assertThatThrownBy(() -> converser.ecrire(OBSERVATEUR_BNIA, id, "Pouvez-vous préciser la ligne ?"))
                .isInstanceOf(AccesRefuse.class)
                .extracting(e -> ((RegleEnfreinte) e).regle().orElseThrow()).isEqualTo("RG-MSG-02");
        assertThat(converser.ecrire(AGENT_BNIA, id, "Pouvez-vous préciser la ligne ?").cote()).isEqualTo(Cote.BNIA);
        assertThat(converser.consulter(AGENT_BNIA, id).messages()).hasSize(2);
    }

    @Test
    @DisplayName("RG-MSG-12 : seul un utilisateur du portail qui gère la Messagerie ouvre une conversation libre, avec un titre")
    void rg_msg_12_ouverture() {
        assertThatThrownBy(() -> converser.ouvrirLibre(AGENT_BNIA, NEGOCIANT, "Titre", "Texte", List.of()))
                .isInstanceOf(AccesRefuse.class);
        enfreint("RG-MSG-12", () -> converser.ouvrirLibre(AGENT, NEGOCIANT, " ", "Texte", List.of()));
        enfreint("RG-MSG-05", () -> converser.ouvrirLibre(AGENT, NEGOCIANT, "Titre", " ", List.of()));
        assertThatThrownBy(() -> converser.ouvrirLibre(OBSERVATEUR, NEGOCIANT, "Titre", "Texte", List.of()))
                .isInstanceOf(AccesRefuse.class).hasMessage("Seul un membre de l'opérateur ouvre une conversation"
                        + " avec le BNIA.");
        assertThat(monde.conversations.parId).as("rien n'est ouvert sur un refus").isEmpty();
        var vue = converser.ouvrirLibre(ADMIN, NEGOCIANT, "Accès à l'export", "Bonjour", List.of());
        assertThat(vue.conversation().orElseThrow().operateur()).isEqualTo(NEGOCIANT);
        assertThat(vue.sujet().intitule()).isEqualTo("Accès à l'export");
    }

    @Test
    @DisplayName("RG-MSG-13 : l'auteur parti, elle passe en lecture seule pour le BNIA ; réactivé, elle redevient active")
    void rg_msg_13_auteur_parti() {
        long id = ouvrir();
        monde.droits.retirer(AGENT.utilisateur(), NEGOCIANT);
        var vue = converser.consulter(AGENT_BNIA, id);
        assertThat(vue.auteurParti()).isTrue();
        assertThat(vue.peutEcrire()).isFalse();
        assertThat(vue.lectureSeule()).contains("Utilisateur 10 n'est plus membre de l'opérateur : la conversation est"
                + " en lecture seule.");
        assertThatThrownBy(() -> converser.ecrire(AGENT_BNIA, id, "Réponse")).isInstanceOf(AccesRefuse.class)
                .extracting(e -> ((RegleEnfreinte) e).regle().orElseThrow()).isEqualTo("RG-MSG-13");
        monde.droits.membre(AGENT.utilisateur(), NEGOCIANT);
        assertThat(converser.consulter(AGENT_BNIA, id).peutEcrire()).isTrue();
    }

    @Test
    @DisplayName("RG-MSG-14 : cinq pièces au plus, formats admis, en conversation libre seulement ; supprimées avec leur message")
    void rg_msg_14_pieces_jointes() {
        long id = ouvrir();
        Message reponse = converser.ecrire(AGENT, id, "Et l'attestation", List.of(PDF));
        assertThat(reponse.pieces()).singleElement().satisfies(d -> assertThat(d.type()).isEqualTo("application/pdf"));
        assertThat(converser.piece(AGENT_BNIA, id, reponse.pieces().getFirst().cle()).contenu()).isEqualTo(PDF.contenu());
        assertThatThrownBy(() -> converser.piece(ADMIN, id, reponse.pieces().getFirst().cle()))
                .isInstanceOf(AccesRefuse.class);

        enfreint("RG-MSG-14", () -> converser.ecrire(AGENT, id, "Six", Collections.nCopies(6, PDF)));
        assertThatThrownBy(() -> converser.ecrire(AGENT, id, "Image ?", List.of(new FichierJoint("a.exe",
                new byte[] {1, 2, 3, 4})))).isInstanceOf(DocumentRefuse.class);
        converser.ecrireSurObjet(AGENT, DSM, "Sur la DSM");
        long dsm = monde.conversations.parObjet(DSM).orElseThrow().id();
        enfreint("RG-MSG-14", () -> converser.ecrire(AGENT, dsm, "Pièce", List.of(PDF)));

        int avant = monde.stockage.documents.size();
        converser.supprimer(AGENT, reponse.id());
        assertThat(monde.stockage.documents).hasSize(avant - 1);
    }
}
