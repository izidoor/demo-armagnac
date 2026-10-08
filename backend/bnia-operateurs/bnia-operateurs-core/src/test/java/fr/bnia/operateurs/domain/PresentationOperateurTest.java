package fr.bnia.operateurs.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import fr.bnia.commun.domain.Millesime;
import fr.bnia.commun.domain.RegleEnfreinte;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PresentationOperateurTest {

    private final PresentationOperateur presentation = PresentationOperateur.vide(12);

    private static String regle(Throwable e) {
        return ((RegleEnfreinte) e).regle().orElseThrow();
    }

    private void marques(MarquePresentee... marques) {
        presentation.modifier(List.of(marques), List.of(), List.of(), List.of(), Oenotourisme.aucun(), List.of());
    }

    @Test
    @DisplayName("RG-OPE-16 : une marque au plus une fois par portée ; la même dans les deux portées")
    void rg_ope_16_marques() {
        marques(new MarquePresentee(Portee.FRANCE, 3, true), new MarquePresentee(Portee.EXPORT, 3, false));
        assertThat(presentation.marques()).hasSize(2);

        assertThatThrownBy(() -> marques(new MarquePresentee(Portee.FRANCE, 3, true), new MarquePresentee(Portee.FRANCE, 3, false)))
                .satisfies(e -> assertThat(regle(e)).isEqualTo("RG-OPE-16")).hasMessageContaining("France");
        assertThatThrownBy(() -> new MarquePresentee(null, 3, false)).satisfies(e -> assertThat(regle(e)).isEqualTo("RG-OPE-16"));
    }

    @Test
    @DisplayName("RG-OPE-16 : un circuit au plus une fois par portée")
    void rg_ope_16_circuits() {
        var circuits = List.of(new CircuitPresente(Portee.EXPORT, 5), new CircuitPresente(Portee.EXPORT, 5));

        assertThatThrownBy(() -> presentation.modifier(List.of(), circuits, List.of(), List.of(), Oenotourisme.aucun(), List.of()))
                .satisfies(e -> assertThat(regle(e)).isEqualTo("RG-OPE-16")).hasMessageContaining("Export");
    }

    @Test
    @DisplayName("RG-OPE-17 : un type de visite, ou un label, au plus une fois ; commentaire facultatif")
    void rg_ope_17_visites_et_labels() {
        var visites = List.of(new VisiteProposee(1, " "), new VisiteProposee(1, "Sur rendez-vous"));
        assertThatThrownBy(() -> presentation.modifier(List.of(), List.of(), visites, List.of(), Oenotourisme.aucun(), List.of()))
                .satisfies(e -> assertThat(regle(e)).isEqualTo("RG-OPE-17"));
        var labels = List.of(new LabelObtenu(2, null), new LabelObtenu(2, null));
        assertThatThrownBy(() -> presentation.modifier(List.of(), List.of(), List.of(), labels, Oenotourisme.aucun(), List.of()))
                .satisfies(e -> assertThat(regle(e)).isEqualTo("RG-OPE-17"));

        assertThat(new VisiteProposee(1, " ").commentaire()).isNull();
    }

    @Test
    @DisplayName("RG-OPE-18 : les cartes ne se saisissent qu'avec le paiement par carte ; faux par défaut")
    void rg_ope_18_oenotourisme() {
        var avecCarte = new Oenotourisme(true, Set.of(CarteBancaire.VISA), false, false, false, false, Set.of(), Set.of(),
                null, null, null);
        var sansCarte = new Oenotourisme(false, Set.of(CarteBancaire.VISA), false, false, false, false, Set.of(), Set.of(),
                ModePresentation.ARMAGNAC_EXCLUSIVEMENT, null, null);

        assertThat(avecCarte.cartes()).containsExactly(CarteBancaire.VISA);
        assertThat(sansCarte.cartes()).isEmpty();
        assertThat(Oenotourisme.aucun()).isEqualTo(new Oenotourisme(false, null, false, false, false, false, null, null,
                null, " ", null));
        assertThat(presentation.oenotourisme().modePresentation()).isNull();
    }

    @Test
    @DisplayName("RG-OPE-19 : une médaille porte un concours ; année valide, distinction et produit facultatifs")
    void rg_ope_19_medailles() {
        assertThat(MedailleConcours.saisie("Concours général agricole", 2024, Distinction.OR, "Hors d'âge"))
                .isEqualTo(new MedailleConcours("Concours général agricole", Millesime.de(2024), Distinction.OR, "Hors d'âge"));
        assertThat(MedailleConcours.saisie("Spirits Selection", null, null, null).annee()).isNull();

        assertThatThrownBy(() -> MedailleConcours.saisie(" ", 2024, null, null))
                .satisfies(e -> assertThat(regle(e)).isEqualTo("RG-OPE-19"));
        assertThatThrownBy(() -> MedailleConcours.saisie("Concours", 24, null, null))
                .satisfies(e -> assertThat(regle(e)).isEqualTo("RG-OPE-19"));
    }
}
