package fr.bnia.facturation.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import fr.bnia.commun.domain.RegleEnfreinte;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class VendeurTest {

    @Test
    @DisplayName("RG-FAC-16 : le vendeur est complet ; SIRET, TVA, IBAN et BIC contrôlés")
    void rg_fac_16_complet() {
        Vendeur v = Exemples.vendeur();

        assertThat(v.siren()).isEqualTo("776999955");
        assertThat(v.escompte()).isEqualTo(Vendeur.ESCOMPTE);
        assertThatThrownBy(() -> new Vendeur(v.nom(), v.siege(), null, null, "77699995500028", v.numeroTva(), "FR76",
                v.bic(), v.telephone(), v.email(), false, null, null))
                .isInstanceOfSatisfying(RegleEnfreinte.class, e -> {
                    assertThat(e.regle()).contains("RG-FAC-16");
                    assertThat(e).hasMessageContaining("un SIRET valide").hasMessageContaining("un IBAN valide");
                });
    }

    @Test
    @DisplayName("RG-FAC-15 : mentions générées, indemnité de 40 € pour un professionnel seulement")
    void rg_fac_15_mentions() {
        assertThat(Exemples.vendeur().mentions(CategorieAcheteur.PROFESSIONNEL)).containsExactly(
                "Pénalités de retard : trois fois le taux d'intérêt légal.",
                "Indemnité forfaitaire pour frais de recouvrement en cas de retard de paiement : 40 €.",
                "Pas d'escompte pour paiement anticipé.");
        assertThat(Exemples.vendeur().mentions(CategorieAcheteur.PARTICULIER)).hasSize(2);
    }
}
