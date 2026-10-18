package fr.bnia.facturation.application;

import static fr.bnia.facturation.application.DemanderFacturesTest.demandeDeLExemple;
import static fr.bnia.facturation.application.Monde.GESTIONNAIRE;
import static org.assertj.core.api.Assertions.assertThat;

import fr.bnia.facturation.domain.Exemples;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Les pièces décrites à la messagerie (spec 15, lot 6). */
class DecrirePieceTest {

    private final Monde monde = new Monde();
    private final DecrirePiece decrire = new DecrirePiece(monde.factures);

    @Test
    @DisplayName("RG-MSG-17 : une pièce émise d'un client opérateur est conversable, libellée par son sens et son numéro")
    void rg_msg_17_piece_emise_conversable() {
        monde.vendeurs.vendeur = Exemples.vendeur();
        long brouillon = monde.demander().demander(GESTIONNAIRE, List.of(demandeDeLExemple("7"))).getFirst();
        assertThat(decrire.decrire(brouillon)).as("un brouillon, jamais vu du portail").isEmpty();

        monde.emettre().emettre(GESTIONNAIRE, brouillon);

        assertThat(decrire.decrire(brouillon)).hasValueSatisfying(p -> {
            assertThat(p.libelle()).isEqualTo("Facture 2026000001");
            assertThat(p.operateur()).isEqualTo(Long.parseLong(Exemples.CLIENT.identifiant()));
        });
        assertThat(decrire.decrire(999)).isEmpty();
    }
}
