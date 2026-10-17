package fr.bnia.facturation.application;

import static fr.bnia.facturation.application.DemanderFacturesTest.demandeDeLExemple;
import static fr.bnia.facturation.application.Monde.GESTIONNAIRE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import fr.bnia.commun.domain.RegleEnfreinte;
import fr.bnia.facturation.domain.EtatZeendoc;
import fr.bnia.facturation.domain.EvenementJournal;
import fr.bnia.facturation.domain.Exemples;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class EnregistrerReglementTest {

    private final Monde monde = new Monde();
    private final EnregistrerReglement cas = monde.reglement();

    @Test
    @DisplayName("RG-FAC-33 : le règlement d'une pièce transmise la marque payée, journalisé au nom de Zeendoc")
    void rg_fac_33_reglement() {
        monde.vendeurs.vendeur = Exemples.vendeur();
        long id = monde.demander().demander(GESTIONNAIRE, List.of(demandeDeLExemple("7"))).getFirst();
        monde.emettreLot().emettre(GESTIONNAIRE, List.of(id));
        assertThatThrownBy(() -> cas.enregistrer(id, LocalDate.of(2026, 11, 30)))
                .isInstanceOfSatisfying(RegleEnfreinte.class, e -> assertThat(e.regle()).contains("RG-FAC-33"));

        monde.transmettre().transmettre(GESTIONNAIRE, List.of(id));
        cas.enregistrer(id, LocalDate.of(2026, 11, 30));

        assertThat(monde.factures.parId(id).orElseThrow().zeendoc()).hasValueSatisfying(z -> {
            assertThat(z.etat()).isEqualTo(EtatZeendoc.PAYEE);
            assertThat(z.payeeLe()).isEqualTo(LocalDate.of(2026, 11, 30));
        });
        assertThat(monde.journal.deLaPiece(id).getFirst()).satisfies(e -> {
            assertThat(e.evenement()).isEqualTo(EvenementJournal.PAYEE);
            assertThat(e.utilisateur()).isNull();
            assertThat(e.nomActeur()).isEqualTo("Zeendoc");
        });
    }
}
