package fr.bnia.facturation.application;

import static fr.bnia.facturation.application.DemanderFacturesTest.demandeDeLExemple;
import static fr.bnia.facturation.application.Monde.GESTIONNAIRE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import fr.bnia.commun.domain.AccesRefuse;
import fr.bnia.commun.domain.ElementIntrouvable;
import fr.bnia.facturation.domain.CoteJournal;
import fr.bnia.facturation.domain.EntreeJournal;
import fr.bnia.facturation.domain.EvenementJournal;
import fr.bnia.facturation.domain.Exemples;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ConsulterFacturesTest {

    private final Monde monde = new Monde();
    private final ConsulterFactures cas = monde.consulter();
    private final long brouillon = monde.demander().demander(GESTIONNAIRE, List.of(demandeDeLExemple("7"))).getFirst();

    @Test
    @DisplayName("RG-FAC-09 : un brouillon montre ce qui l'empêche d'être émis, et l'action à qui gère la facturation")
    void rg_fac_09_brouillon() {
        VuePiece vue = cas.consulter(Monde.LECTEUR, brouillon);

        assertThat(vue.empechements()).containsExactly("vendeur non paramétré (Paramétrage > Facturation)");
        assertThat(vue.emettre()).isFalse();
        assertThat(cas.consulter(GESTIONNAIRE, brouillon).emettre()).isTrue();
        assertThatThrownBy(() -> cas.pdf(GESTIONNAIRE, brouillon)).isInstanceOf(ElementIntrouvable.class);
    }

    @Test
    @DisplayName("RG-FAC-20 : l'ouverture et le PDF d'une pièce émise sont journalisés « consultée », côté vendeur")
    void rg_fac_20_consultation() {
        monde.vendeurs.vendeur = Exemples.vendeur();
        monde.emettre().emettre(GESTIONNAIRE, brouillon);

        cas.consulter(Monde.LECTEUR, brouillon);
        assertThat(cas.pdf(Monde.LECTEUR, brouillon).contenu()).startsWith("%PDF".getBytes());

        assertThat(cas.journal(Monde.LECTEUR, brouillon)).extracting(EntreeJournal::evenement, EntreeJournal::cote)
                .containsExactly(org.assertj.core.groups.Tuple.tuple(EvenementJournal.CONSULTEE, CoteJournal.VENDEUR),
                        org.assertj.core.groups.Tuple.tuple(EvenementJournal.CONSULTEE, CoteJournal.VENDEUR),
                        org.assertj.core.groups.Tuple.tuple(EvenementJournal.EMISE, CoteJournal.VENDEUR),
                        org.assertj.core.groups.Tuple.tuple(EvenementJournal.DEMANDEE, CoteJournal.VENDEUR));
    }

    @Test
    @DisplayName("RG-FAC-26 : l'historique, les pièces et leur journal sont lus par le personnel du BNIA seulement")
    void rg_fac_26_refus() {
        assertThatThrownBy(() -> cas.consulter(Monde.ETRANGER, brouillon))
                .isInstanceOfSatisfying(AccesRefuse.class, e -> assertThat(e.regle()).contains("RG-FAC-26"));
        assertThatThrownBy(() -> cas.lister(Monde.ETRANGER, FiltrePieces.aucun())).isInstanceOf(AccesRefuse.class);
        assertThatThrownBy(() -> cas.journal(Monde.ETRANGER, brouillon)).isInstanceOf(AccesRefuse.class);
    }
}
