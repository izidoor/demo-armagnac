package fr.bnia.facturation.application;

import static fr.bnia.facturation.application.DemanderFacturesTest.demandeDeLExemple;
import static fr.bnia.facturation.application.Monde.GESTIONNAIRE;
import static fr.bnia.facturation.application.Monde.LECTEUR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import fr.bnia.commun.domain.AccesRefuse;
import fr.bnia.facturation.application.TransmettreAZeendoc.CompteRendu;
import fr.bnia.facturation.domain.CoteJournal;
import fr.bnia.facturation.domain.EtatZeendoc;
import fr.bnia.facturation.domain.EvenementJournal;
import fr.bnia.facturation.domain.Exemples;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TransmettreAZeendocTest {

    private final Monde monde = new Monde();
    private final TransmettreAZeendoc cas = monde.transmettre();

    @Test
    @DisplayName("RG-FAC-32 : chaque pièce émise de la sélection est déposée, marquée transmise et journalisée")
    void rg_fac_32_transmission() {
        monde.vendeurs.vendeur = Exemples.vendeur();
        List<Long> ids = monde.demander().demander(GESTIONNAIRE, List.of(demandeDeLExemple("7"),
                demandeDeLExemple("8")));
        monde.emettreLot().emettre(GESTIONNAIRE, ids);

        List<CompteRendu> comptesRendus = cas.transmettre(GESTIONNAIRE, ids);

        assertThat(comptesRendus).extracting(CompteRendu::numero).containsExactly("2026000001", "2026000002");
        assertThat(comptesRendus).allMatch(CompteRendu::transmise);
        assertThat(monde.deposes).containsExactly("2026000001", "2026000002");
        assertThat(monde.factures.parId(ids.getFirst()).orElseThrow().zeendoc()).hasValueSatisfying(z -> {
            assertThat(z.etat()).isEqualTo(EtatZeendoc.TRANSMISE);
            assertThat(z.transmiseLe()).isEqualTo(monde.horloge.instant());
        });
        assertThat(monde.journal.deLaPiece(ids.getFirst()).getFirst()).satisfies(e -> {
            assertThat(e.evenement()).isEqualTo(EvenementJournal.TRANSMISE);
            assertThat(e.cote()).isEqualTo(CoteJournal.VENDEUR);
            assertThat(e.nomActeur()).isEqualTo("Utilisateur 1");
        });
    }

    @Test
    @DisplayName("RG-FAC-32 : une pièce déjà transmise, un brouillon ou une pièce introuvable sont rapportés, sans arrêter les autres ni redéposer")
    void rg_fac_32_refus_dans_le_compte_rendu() {
        monde.vendeurs.vendeur = Exemples.vendeur();
        List<Long> ids = monde.demander().demander(GESTIONNAIRE, List.of(demandeDeLExemple("7"),
                demandeDeLExemple("8"), demandeDeLExemple("9")));
        monde.emettreLot().emettre(GESTIONNAIRE, ids.subList(0, 2));
        cas.transmettre(GESTIONNAIRE, List.of(ids.getFirst()));

        List<CompteRendu> comptesRendus = cas.transmettre(GESTIONNAIRE,
                List.of(ids.get(0), ids.get(1), ids.get(2), ids.get(1), 404L));

        assertThat(comptesRendus).extracting(CompteRendu::id).containsExactly(ids.get(0), ids.get(1), ids.get(2),
                404L);
        assertThat(comptesRendus.get(0).motif()).isEqualTo("La pièce 2026000001 est déjà transmise à Zeendoc.");
        assertThat(comptesRendus.get(1).transmise()).isTrue();
        assertThat(comptesRendus.get(2).motif()).contains("n'est pas émise");
        assertThat(comptesRendus.get(3).motif()).contains("introuvable");
        assertThat(monde.deposes).containsExactly("2026000001", "2026000002");
    }

    @Test
    @DisplayName("RG-FAC-26 : transmettre à Zeendoc est réservé à qui gère la facturation")
    void rg_fac_26_refus() {
        assertThatThrownBy(() -> cas.transmettre(LECTEUR, List.of(1L)))
                .isInstanceOfSatisfying(AccesRefuse.class, e -> assertThat(e.regle()).contains("RG-FAC-26"));
    }
}
