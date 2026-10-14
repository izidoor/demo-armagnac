package fr.bnia.operateurs.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import fr.bnia.commun.domain.ModificationConcurrente;
import fr.bnia.commun.domain.RegleEnfreinte;
import fr.bnia.operateurs.domain.partage.PeriodiciteDsm;
import fr.bnia.operateurs.domain.partage.StatutDeclarationStock;
import fr.bnia.operateurs.domain.partage.TypeOperateur;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class OperateurBniaTest {

    private static Operateur bnia() {
        return Operateur.reconstituer(1L, TypeOperateur.BNIA, CodeOperateur.de("BNIA"),
                OperateurTest.identite("BNIA", null), Declaratif.parDefaut(TypeOperateur.BNIA), Production.aucune(),
                DiffusionMillesimes.refus(), false, true, null, null, 0);
    }

    private static Operateur producteur() {
        return Operateur.reconstituer(2L, TypeOperateur.PRODUCTEUR, CodeOperateur.de("000001"),
                OperateurTest.identite("Durand", OperateurTest.adresse("32100")),
                Declaratif.parDefaut(TypeOperateur.PRODUCTEUR), Production.aucune(), DiffusionMillesimes.refus(), false,
                true, null, null, 3);
    }

    @Test
    @DisplayName("RG-OPE-39 : le type BNIA ne se crée pas, ne se choisit pas et ne se quitte pas")
    void rg_ope_39_type_bnia_reserve() {
        assertThatThrownBy(() -> Operateur.creer(TypeOperateur.BNIA, CodeOperateur.de("BNIA2"),
                OperateurTest.identite("Autre BNIA", null)))
                .isInstanceOf(RegleEnfreinte.class).extracting(e -> ((RegleEnfreinte) e).regle().orElseThrow())
                .isEqualTo("RG-OPE-39");
        Operateur producteur = producteur();
        assertThatThrownBy(() -> producteur.modifierIdentite(TypeOperateur.BNIA, producteur.code(), producteur.identite()))
                .hasMessageContaining("réservé à l'interprofession");
        Operateur bnia = bnia();
        assertThatThrownBy(() -> bnia.modifierIdentite(TypeOperateur.AUTRE, bnia.code(), bnia.identite()))
                .extracting(e -> ((RegleEnfreinte) e).regle().orElseThrow()).isEqualTo("RG-OPE-39");
    }

    @Test
    @DisplayName("RG-OPE-39 : l'opérateur BNIA ne se désactive ni ne se supprime ; sa fiche est toujours facturable et certifiable")
    void rg_ope_39_bnia_ni_desactive_ni_supprime() {
        Operateur bnia = bnia();

        assertThatThrownBy(bnia::desactiver).hasMessageContaining("ne se désactive pas");
        assertThatThrownBy(bnia::verifierSuppressible).hasMessageContaining("ne se supprime pas");
        assertThat(bnia.manquePourFacturer(false)).isEmpty();
        assertThat(bnia.manquePourCertifier()).isEmpty();
        assertThat(bnia.serieAttendueSiCodeNonConforme(true)).as("aucun code proposé").isEmpty();
    }

    @Test
    @DisplayName("RG-OPE-01 : un opérateur BNIA ne déclare pas : pas de périodicité, statut NON")
    void rg_ope_01_bnia_ne_declare_pas() {
        assertThat(Declaratif.parDefaut(TypeOperateur.BNIA))
                .isEqualTo(new Declaratif(null, StatutDeclarationStock.NON));
        Operateur bnia = bnia();
        assertThatThrownBy(() -> bnia.modifierProduction(
                new Declaratif(PeriodiciteDsm.ANNUELLE, StatutDeclarationStock.NON), Production.aucune()))
                .extracting(e -> ((RegleEnfreinte) e).regle().orElseThrow()).isEqualTo("RG-OPE-01");
    }

    @Test
    @DisplayName("RG-OPE-22 et RG-OPE-23 : les désignations d'une fiche supprimée tombent")
    void rg_ope_22_23_designations_tombent() {
        Operateur operateur = producteur();
        operateur.designer(10L, DestinataireFactures.fiche(10L));

        assertThat(operateur.oublierFiche(11L)).isFalse();
        assertThat(operateur.oublierFiche(10L)).isTrue();
        assertThat(operateur.contactPrincipal()).isEmpty();
        assertThat(operateur.destinataireFactures()).isEmpty();

        operateur.designer(null, DestinataireFactures.adresse(Email.de("compta@durand.fr", "RG-OPE-24")));
        assertThat(operateur.oublierFiche(10L)).as("une adresse saisie ne tombe pas").isFalse();
        assertThatThrownBy(() -> new DestinataireFactures(10L, Email.de("a@b.fr", "RG-OPE-24")))
                .extracting(e -> ((RegleEnfreinte) e).regle().orElseThrow()).isEqualTo("RG-OPE-23");
    }

    @Test
    @DisplayName("RG-OPE-51 : une saisie faite sur une version dépassée est refusée")
    void rg_ope_51_version() {
        Operateur operateur = producteur();

        operateur.verifierVersion(3L);
        operateur.verifierVersion(null);
        assertThatThrownBy(() -> operateur.verifierVersion(2L))
                .isInstanceOf(ModificationConcurrente.class)
                .hasMessageContaining("modifiée entre-temps")
                .extracting(e -> ((RegleEnfreinte) e).regle().orElseThrow()).isEqualTo("RG-OPE-51");
    }
}
