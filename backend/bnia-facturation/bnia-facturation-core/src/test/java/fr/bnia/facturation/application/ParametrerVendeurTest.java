package fr.bnia.facturation.application;

import static fr.bnia.facturation.application.DemanderFacturesTest.demandeDeLExemple;
import static fr.bnia.facturation.application.Monde.GESTIONNAIRE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import fr.bnia.commun.domain.AccesRefuse;
import fr.bnia.facturation.domain.Exemples;
import fr.bnia.facturation.domain.Vendeur;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ParametrerVendeurTest {

    private final Monde monde = new Monde();
    private final ParametrerVendeur cas = monde.parametrer();

    @Test
    @DisplayName("RG-FAC-16 : le vendeur se paramètre ; le modifier ne change aucune pièce émise")
    void rg_fac_16_parametre() {
        assertThat(cas.consulter(Monde.LECTEUR)).isEmpty();
        cas.enregistrer(GESTIONNAIRE, Exemples.vendeur());
        long id = monde.demander().demander(GESTIONNAIRE, List.of(demandeDeLExemple("7"))).getFirst();
        monde.emettre().emettre(GESTIONNAIRE, id);

        Vendeur v = Exemples.vendeur();
        cas.enregistrer(GESTIONNAIRE, new Vendeur("BNIA", v.siege(), null, null, v.siret(), v.numeroTva(), v.iban(),
                v.bic(), v.telephone(), v.email(), true, null, null));

        assertThat(cas.consulter(Monde.LECTEUR).orElseThrow().nom()).isEqualTo("BNIA");
        assertThat(monde.factures.parId(id).orElseThrow().vendeur().orElseThrow().nom())
                .isEqualTo("Bureau National Interprofessionnel de l'Armagnac");
    }

    @Test
    @DisplayName("RG-FAC-26 : le vendeur est lu par le BNIA et paramétré par qui gère la facturation")
    void rg_fac_26_refus() {
        assertThatThrownBy(() -> cas.enregistrer(Monde.LECTEUR, Exemples.vendeur()))
                .isInstanceOfSatisfying(AccesRefuse.class, e -> assertThat(e.regle()).contains("RG-FAC-26"));
        assertThatThrownBy(() -> cas.consulter(Monde.ETRANGER)).isInstanceOf(AccesRefuse.class);
    }
}
