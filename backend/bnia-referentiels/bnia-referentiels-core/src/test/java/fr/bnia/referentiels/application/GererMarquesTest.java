package fr.bnia.referentiels.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import fr.bnia.commun.domain.AccesRefuse;
import fr.bnia.commun.domain.ConflitUnicite;
import fr.bnia.referentiels.domain.LibelleNormalise;
import fr.bnia.referentiels.domain.Marque;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class GererMarquesTest {

    private final MarquesEnMemoire marques = new MarquesEnMemoire();
    private final GererMarques gerer = new GererMarques(marques, marques, marques, Parametrage.AUTORISE);

    @Test
    @DisplayName("RG-REF-14 : créer une marque (depuis une fiche opérateur) applique l'unicité du code")
    void rg_ref_14_unicite_du_code() {
        gerer.creer(Parametrage.ACTEUR, "CX", "Château X");

        assertThatThrownBy(() -> gerer.creer(Parametrage.ACTEUR, "cx", "Autre marque"))
                .isInstanceOf(ConflitUnicite.class)
                .extracting(e -> ((ConflitUnicite) e).regle().orElseThrow()).isEqualTo("RG-REF-01");
    }

    @Test
    @DisplayName("RG-REF-14 : créer une marque (depuis une fiche opérateur) applique l'unicité du libellé")
    void rg_ref_14_unicite_du_libelle() {
        gerer.creer(Parametrage.ACTEUR, "CX", "Château X");

        assertThatThrownBy(() -> gerer.creer(Parametrage.ACTEUR, "CX2", "CHATEAU  X"))
                .isInstanceOf(ConflitUnicite.class)
                .extracting(e -> ((ConflitUnicite) e).regle().orElseThrow()).isEqualTo("RG-REF-15");
    }

    @Test
    @DisplayName("RG-REF-15 : le message nomme la marque existante")
    void rg_ref_15_message_nomme_la_marque_existante() {
        gerer.creer(Parametrage.ACTEUR, "CX", "Château X");

        assertThatThrownBy(() -> gerer.creer(Parametrage.ACTEUR, "CX2", "chateau x"))
                .hasMessage("La marque « Château X » (code CX) existe déjà.");
    }

    @Test
    @DisplayName("RG-REF-15 : une marque inactive bloque son libellé et invite à la réactiver")
    void rg_ref_15_marque_inactive_bloque_son_libelle() {
        long id = gerer.creer(Parametrage.ACTEUR, "CX", "Château X").id();
        gerer.desactiver(Parametrage.ACTEUR, id);

        assertThatThrownBy(() -> gerer.creer(Parametrage.ACTEUR, "CX2", "Château X"))
                .isInstanceOf(ConflitUnicite.class)
                .hasMessageContaining("réactivez-la");
    }

    @Test
    @DisplayName("RG-REF-15 : une modification ne peut pas reprendre le libellé d'une autre marque")
    void rg_ref_15_modification_vers_un_libelle_pris() {
        gerer.creer(Parametrage.ACTEUR, "CX", "Château X");
        long cy = gerer.creer(Parametrage.ACTEUR, "CY", "Château Y").id();

        assertThatThrownBy(() -> gerer.modifier(Parametrage.ACTEUR, cy, "CY", "Chateau X")).isInstanceOf(ConflitUnicite.class);
    }

    @Test
    @DisplayName("RG-REF-15 : une marque peut corriger la casse ou les accents de son propre libellé")
    void rg_ref_15_modification_de_son_propre_libelle() {
        long id = gerer.creer(Parametrage.ACTEUR, "CX", "Chateau X").id();

        assertThat(gerer.modifier(Parametrage.ACTEUR, id, "CX", "Château X").libelle().valeur()).isEqualTo("Château X");
    }

    @Test
    @DisplayName("RG-REF-15 : les marques proches sont proposées pendant la saisie")
    void rg_ref_15_marques_proches() {
        gerer.creer(Parametrage.ACTEUR, "CX", "Château X");
        gerer.creer(Parametrage.ACTEUR, "DY", "Domaine Y");

        assertThat(gerer.proches("chateau")).extracting(ValeurSimpleLigne::code).containsExactly("CX");
        assertThat(gerer.proches(" ")).isEmpty();
    }

    @Test
    @DisplayName("RG-OPE-31 : le Paramétrage est réservé au BNIA ; un membre d'opérateur est refusé")
    void rg_ope_31_parametrage_reserve_au_bnia() {
        long id = gerer.creer(Parametrage.ACTEUR, "CX", "Château X").id();

        assertThatThrownBy(() -> gerer.creer(Parametrage.MEMBRE, "CY", "Château Y")).isInstanceOf(AccesRefuse.class);
        assertThatThrownBy(() -> gerer.modifier(Parametrage.MEMBRE, id, "CX", "Château X2")).isInstanceOf(AccesRefuse.class);
        assertThatThrownBy(() -> gerer.supprimer(Parametrage.MEMBRE, id)).isInstanceOf(AccesRefuse.class);
    }

    @Test
    @DisplayName("RG-REF-14 : une marque se crée depuis la fiche par qui gère les contacts de cet opérateur, avec les mêmes règles")
    void rg_ref_14_depuis_la_fiche() {
        gerer.creer(Parametrage.ACTEUR, "CX", "Château X");

        assertThat(gerer.creerDepuisFiche(Parametrage.MEMBRE, Parametrage.OPERATEUR, "CY", "Château Y").code().valeur())
                .isEqualTo("CY");
        assertThatThrownBy(() -> gerer.creerDepuisFiche(Parametrage.MEMBRE, Parametrage.OPERATEUR, "CZ", "chateau x"))
                .as("RG-REF-15").isInstanceOf(ConflitUnicite.class);
        assertThatThrownBy(() -> gerer.creerDepuisFiche(Parametrage.MEMBRE, 99, "CW", "Château W"))
                .isInstanceOf(AccesRefuse.class);
    }

    private static final class MarquesEnMemoire extends ValeursSimplesEnMemoire<Marque>
            implements MarqueRepository, MarquesProchesLecture {

        MarquesEnMemoire() {
            super((id, m) -> Marque.reconstituer(id, m.code(), m.libelle(), m.estActif()));
        }

        @Override
        public Optional<Marque> parLibelleNormalise(LibelleNormalise libelle) {
            return valeurs.values().stream().filter(m -> m.libelleNormalise().equals(libelle)).findFirst();
        }

        @Override
        public List<ValeurSimpleLigne> proches(LibelleNormalise texte, int nombreMaximum) {
            return valeurs.values().stream()
                    .filter(m -> m.libelleNormalise().valeur().contains(texte.valeur()))
                    .limit(nombreMaximum)
                    .map(m -> new ValeurSimpleLigne(m.id(), m.code().valeur(), m.libelle().valeur(), m.estActif()))
                    .toList();
        }
    }
}
