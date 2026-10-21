package fr.bnia.boot.demo;

import static org.assertj.core.api.Assertions.assertThat;

import fr.bnia.declarations.application.GererDsm;
import fr.bnia.declarations.domain.partage.EtatDsm;
import fr.bnia.messagerie.api.Acteur;
import fr.bnia.messagerie.api.ObjetRef;
import fr.bnia.messagerie.api.TypeObjet;
import fr.bnia.messagerie.application.ConsulterMessagerie;
import fr.bnia.messagerie.application.Converser;
import fr.bnia.messagerie.application.MessagerieLecture;
import fr.bnia.messagerie.application.VueConversation;
import fr.bnia.messagerie.domain.Cote;
import fr.bnia.support.AbstractIT;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.context.ApplicationContext;

/** Chargeur de démonstration de la messagerie (spec 15 §7.1), après tous les autres : relançable, et conforme à la spec. */
class DemoMessagerieIT extends AbstractIT {

    @Autowired ApplicationContext contexte;
    @Autowired GererDsm dsms;
    @Autowired Converser converser;
    @Autowired ConsulterMessagerie consulter;

    @Test
    void ouvre_la_conversation_de_la_dsm_de_gascogne_et_peut_etre_relance() throws Exception {
        var fabrique = contexte.getAutowireCapableBeanFactory();
        var chargeurs = List.of(fabrique.createBean(DemoPersonnelBnia.class), fabrique.createBean(DemoReferentiels.class),
                fabrique.createBean(DemoOperateurs.class), fabrique.createBean(DemoDeclarations.class),
                fabrique.createBean(DemoFacturation.class), fabrique.createBean(DemoMessagerie.class));
        for (int i = 0; i < 2; i++) {
            for (var chargeur : chargeurs) {
                ((ApplicationRunner) chargeur).run(new DefaultApplicationArguments());
            }
        }
        var anne = new fr.bnia.operateurs.domain.partage.Acteur(DemoPersonnelBnia.anne(operateurs));
        long gascogne = operateurs.operateurParCode("N320001").orElseThrow().id();
        long complements = dsms.lister(anne, gascogne).stream().map(GererDsm.DsmDeLOperateur::dsm)
                .filter(d -> d.etat() == EtatDsm.COMPLEMENTS_DEMANDES).findFirst().orElseThrow().id();
        Acteur julie = new Acteur(operateurs.utilisateurParEmail("agent@negociant.fr").orElseThrow());

        List<VueConversation.MessageVu> fil = converser.consulterObjet(julie, ObjetRef.de(TypeObjet.DSM, complements))
                .messages();
        assertThat(fil).extracting(m -> m.message().cote(), VueConversation.MessageVu::nonLu)
                .containsExactly(org.assertj.core.groups.Tuple.tuple(Cote.BNIA, true),
                        org.assertj.core.groups.Tuple.tuple(Cote.OPERATEUR, false),
                        org.assertj.core.groups.Tuple.tuple(Cote.BNIA, true));

        // Lot 5 : Julie voit sa conversation libre, avec sa pièce jointe ; Henri, l'ADMIN, aucune des deux.
        var deJulie = consulter.conversationsDuPortail(julie, gascogne, libres()).elements();
        assertThat(deJulie).singleElement().satisfies(c -> {
            assertThat(c.intitule()).isEqualTo("Import de la matrice refusé");
            assertThat(c.messages()).isEqualTo(2);
        });
        assertThat(converser.consulter(julie, deJulie.getFirst().id()).messages().getFirst().message().pieces())
                .singleElement().satisfies(d -> assertThat(d.nom()).endsWith(".xlsx"));
        Acteur henri = new Acteur(operateurs.utilisateurParEmail("admin@negociant.fr").orElseThrow());
        assertThat(consulter.conversationsDuPortail(henri, gascogne, libres()).elements()).isEmpty();
        Acteur anneMessagerie = new Acteur(anne.utilisateur());
        assertThat(consulter.conversationsDuBnia(anneMessagerie, new MessagerieLecture.Filtre(Optional.of(gascogne),
                Optional.empty(), true, false, Optional.empty())).elements()).hasSize(2);

        // Lot 6 : la question d'Henri sur sa facture, non lue pour le BNIA.
        assertThat(consulter.nouveauxDuBnia(anneMessagerie).elements())
                .anySatisfy(m -> {
                    assertThat(m.type()).contains(TypeObjet.FACTURE);
                    assertThat(m.intitule()).startsWith("Facture ");
                    assertThat(m.auteur()).isEqualTo("Henri Castex");
                });
    }

    private static MessagerieLecture.Filtre libres() {
        return new MessagerieLecture.Filtre(Optional.empty(), Optional.empty(), true, false, Optional.empty());
    }
}
