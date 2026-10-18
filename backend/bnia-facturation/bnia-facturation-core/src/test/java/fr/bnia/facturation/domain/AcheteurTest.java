package fr.bnia.facturation.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AcheteurTest {

    @Test
    @DisplayName("RG-FAC-10 : complet, SIREN tiré du SIRET")
    void rg_fac_10_complet() {
        Acheteur acheteur = Exemples.acheteur();

        assertThat(acheteur.siren()).isEqualTo("776999955");
        assertThat(acheteur.manques()).isEmpty();
    }

    @Test
    @DisplayName("RG-FAC-10 : ce qui manque est nommé ; SIREN exigé d'un professionnel français, clés contrôlées")
    void rg_fac_10_manques() {
        Adresse sansVille = new Adresse("12 rue des Chais", null, "32100", null, "France", "FR");

        assertThat(new Acheteur(null, CategorieAcheteur.PROFESSIONNEL, "Gascogne", null, null, null, sansVille, null, null)
                .manques()).containsExactly("ville", "e-mail du destinataire des factures", "SIREN");
        assertThat(new Acheteur(null, CategorieAcheteur.PROFESSIONNEL, "Gascogne", "776999954", null, "FR00776999955",
                Exemples.acheteur().adresseFacturation(), null, "a@b.fr").manques())
                .containsExactly("SIREN valide (776999954)", "n° de TVA valide (FR00776999955)");
        Adresse espagne = new Adresse("Calle Mayor 1", null, "28013", "Madrid", "Espagne", "ES");
        assertThat(new Acheteur(null, CategorieAcheteur.PROFESSIONNEL, "Bodega", null, null, null, espagne, null,
                "a@b.es").manques()).as("hors de France, pas de SIREN").isEmpty();
    }
}
