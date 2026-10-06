package fr.bnia.referentiels.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import fr.bnia.commun.domain.ConflitUnicite;
import fr.bnia.referentiels.domain.LibelleNormalise;
import fr.bnia.referentiels.domain.Marque;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class GererMarquesTest {

    private final MarquesEnMemoire marques = new MarquesEnMemoire();
    private final GererMarques gerer = new GererMarques(marques, marques, marques);

    @Test
    @DisplayName("RG-REF-14 : créer une marque (depuis une fiche opérateur) applique l'unicité du code")
    void rg_ref_14_unicite_du_code() {
        gerer.creer("CX", "Château X");

        assertThatThrownBy(() -> gerer.creer("cx", "Autre marque"))
                .isInstanceOf(ConflitUnicite.class)
                .extracting(e -> ((ConflitUnicite) e).regle().orElseThrow()).isEqualTo("RG-REF-01");
    }

    @Test
    @DisplayName("RG-REF-14 : créer une marque (depuis une fiche opérateur) applique l'unicité du libellé")
    void rg_ref_14_unicite_du_libelle() {
        gerer.creer("CX", "Château X");

        assertThatThrownBy(() -> gerer.creer("CX2", "CHATEAU  X"))
                .isInstanceOf(ConflitUnicite.class)
                .extracting(e -> ((ConflitUnicite) e).regle().orElseThrow()).isEqualTo("RG-REF-15");
    }

    @Test
    @DisplayName("RG-REF-15 : le message nomme la marque existante")
    void rg_ref_15_message_nomme_la_marque_existante() {
        gerer.creer("CX", "Château X");

        assertThatThrownBy(() -> gerer.creer("CX2", "chateau x"))
                .hasMessage("La marque « Château X » (code CX) existe déjà.");
    }

    @Test
    @DisplayName("RG-REF-15 : une marque inactive bloque son libellé et invite à la réactiver")
    void rg_ref_15_marque_inactive_bloque_son_libelle() {
        gerer.creer("CX", "Château X");
        gerer.desactiver("CX");

        assertThatThrownBy(() -> gerer.creer("CX2", "Château X"))
                .isInstanceOf(ConflitUnicite.class)
                .hasMessageContaining("réactivez-la");
    }

    @Test
    @DisplayName("RG-REF-15 : une modification ne peut pas reprendre le libellé d'une autre marque")
    void rg_ref_15_modification_vers_un_libelle_pris() {
        gerer.creer("CX", "Château X");
        gerer.creer("CY", "Château Y");

        assertThatThrownBy(() -> gerer.modifier("CY", "Chateau X")).isInstanceOf(ConflitUnicite.class);
    }

    @Test
    @DisplayName("RG-REF-15 : une marque peut corriger la casse ou les accents de son propre libellé")
    void rg_ref_15_modification_de_son_propre_libelle() {
        gerer.creer("CX", "Chateau X");

        assertThat(gerer.modifier("CX", "Château X").libelle().valeur()).isEqualTo("Château X");
    }

    @Test
    @DisplayName("RG-REF-15 : les marques proches sont proposées pendant la saisie")
    void rg_ref_15_marques_proches() {
        gerer.creer("CX", "Château X");
        gerer.creer("DY", "Domaine Y");

        assertThat(gerer.proches("chateau")).extracting(ValeurSimpleLigne::code).containsExactly("CX");
        assertThat(gerer.proches(" ")).isEmpty();
    }

    private static final class MarquesEnMemoire extends ValeursEnMemoire<Marque>
            implements MarqueRepository, MarquesProchesLecture {

        @Override
        public Optional<Marque> parLibelleNormalise(LibelleNormalise libelle) {
            return valeurs.values().stream().filter(m -> m.libelleNormalise().equals(libelle)).findFirst();
        }

        @Override
        public List<ValeurSimpleLigne> proches(LibelleNormalise texte, int nombreMaximum) {
            return valeurs.values().stream()
                    .filter(m -> m.libelleNormalise().valeur().contains(texte.valeur()))
                    .limit(nombreMaximum)
                    .map(m -> new ValeurSimpleLigne(m.code().valeur(), m.libelle().valeur(), m.estActif()))
                    .toList();
        }
    }
}
