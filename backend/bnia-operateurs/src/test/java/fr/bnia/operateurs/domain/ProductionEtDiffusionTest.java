package fr.bnia.operateurs.domain;

import static fr.bnia.operateurs.domain.OperateurTest.adresse;
import static fr.bnia.operateurs.domain.OperateurTest.identite;
import static fr.bnia.operateurs.domain.partage.PeriodiciteDsm.ANNUELLE;
import static fr.bnia.operateurs.domain.partage.PeriodiciteDsm.MENSUELLE;
import static fr.bnia.operateurs.domain.partage.PeriodiciteDsm.TRIMESTRIELLE;
import static fr.bnia.operateurs.domain.partage.StatutDeclarationStock.CLOTURE;
import static fr.bnia.operateurs.domain.partage.StatutDeclarationStock.NON;
import static fr.bnia.operateurs.domain.partage.StatutDeclarationStock.OUI;
import static fr.bnia.operateurs.domain.partage.StatutDeclarationStock.SOMMEIL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import fr.bnia.commun.domain.Appellation;
import fr.bnia.commun.domain.Millesime;
import fr.bnia.commun.domain.RegleEnfreinte;
import fr.bnia.operateurs.domain.partage.TypeOperateur;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ProductionEtDiffusionTest {

    private static Operateur operateur(TypeOperateur type) {
        return Operateur.creer(type, CodeOperateur.de("000001"), identite("Durand", adresse("32100")));
    }

    private static String regle(Throwable e) {
        return ((RegleEnfreinte) e).regle().orElseThrow();
    }

    @Test
    @DisplayName("RG-OPE-01 : une fiche AUTRE ne déclare pas, ni périodicité ni statut OUI")
    void rg_ope_01_autre_ne_declare_pas() {
        Operateur courtier = operateur(TypeOperateur.AUTRE);

        assertThat(courtier.declaratif()).isEqualTo(new Declaratif(null, NON));
        assertThatThrownBy(() -> courtier.modifierProduction(new Declaratif(ANNUELLE, NON), Production.aucune()))
                .satisfies(e -> assertThat(regle(e)).isEqualTo("RG-OPE-01"));
    }

    @Test
    @DisplayName("RG-OPE-10 : périodicité par défaut selon le type, obligatoire hors AUTRE, modifiable")
    void rg_ope_10_periodicite() {
        Operateur producteur = operateur(TypeOperateur.PRODUCTEUR);
        Operateur negociant = operateur(TypeOperateur.NEGOCIANT);

        assertThat(producteur.declaratif().periodicite()).isEqualTo(ANNUELLE);
        assertThat(negociant.declaratif().periodicite()).isEqualTo(MENSUELLE);
        producteur.modifierProduction(new Declaratif(TRIMESTRIELLE, OUI), Production.aucune());
        assertThat(producteur.declaratif().periodicite()).isEqualTo(TRIMESTRIELLE);
        assertThatThrownBy(() -> producteur.modifierProduction(new Declaratif(null, OUI), Production.aucune()))
                .satisfies(e -> assertThat(regle(e)).isEqualTo("RG-OPE-10"));
    }

    @Test
    @DisplayName("RG-OPE-10 : entre producteur et négociant, changer le type ne change pas la périodicité")
    void rg_ope_10_changer_de_type_garde_la_periodicite() {
        Operateur operateur = operateur(TypeOperateur.PRODUCTEUR);
        operateur.modifierProduction(new Declaratif(TRIMESTRIELLE, SOMMEIL), Production.aucune());

        operateur.modifierIdentite(TypeOperateur.NEGOCIANT, operateur.code(), operateur.identite());

        assertThat(operateur.declaratif()).isEqualTo(new Declaratif(TRIMESTRIELLE, SOMMEIL));
    }

    @Test
    @DisplayName("RG-OPE-01 et RG-OPE-11 : passer en AUTRE retire la périodicité, et OUI devient NON ; en sortir rend les défauts")
    void rg_ope_01_11_passage_par_autre() {
        Operateur operateur = operateur(TypeOperateur.NEGOCIANT);

        operateur.modifierIdentite(TypeOperateur.AUTRE, operateur.code(), operateur.identite());
        assertThat(operateur.declaratif()).isEqualTo(new Declaratif(null, NON));

        operateur.modifierIdentite(TypeOperateur.PRODUCTEUR, operateur.code(), operateur.identite());
        assertThat(operateur.declaratif()).isEqualTo(new Declaratif(ANNUELLE, OUI));

        operateur.modifierProduction(new Declaratif(ANNUELLE, CLOTURE), Production.aucune());
        operateur.modifierIdentite(TypeOperateur.AUTRE, operateur.code(), operateur.identite());
        assertThat(operateur.declaratif().statut()).as("un statut autorisé est conservé").isEqualTo(CLOTURE);
    }

    @Test
    @DisplayName("RG-OPE-11 : statut OUI par défaut, jamais pour AUTRE ; toutes les transitions permises")
    void rg_ope_11_statut() {
        Operateur producteur = operateur(TypeOperateur.PRODUCTEUR);
        assertThat(producteur.declaratif().statut()).isEqualTo(OUI);
        for (var statut : List.of(SOMMEIL, CLOTURE, NON, OUI)) {
            producteur.modifierProduction(new Declaratif(ANNUELLE, statut), Production.aucune());
            assertThat(producteur.declaratif().statut()).isEqualTo(statut);
        }

        Operateur courtier = operateur(TypeOperateur.AUTRE);
        assertThatThrownBy(() -> courtier.modifierProduction(new Declaratif(null, OUI), Production.aucune()))
                .satisfies(e -> assertThat(regle(e)).isEqualTo("RG-OPE-11"));
        courtier.modifierProduction(new Declaratif(null, SOMMEIL), Production.aucune());
        assertThatThrownBy(() -> courtier.modifierProduction(new Declaratif(null, null), Production.aucune()))
                .satisfies(e -> assertThat(regle(e)).isEqualTo("RG-OPE-11"));
    }

    @Test
    @DisplayName("RG-OPE-12 : appellations en choix multiple parmi cinq, Blanche Armagnac comprise")
    void rg_ope_12_appellations() {
        var toutes = EnumSet.allOf(Appellation.class);

        assertThat(toutes).hasSize(5).contains(Appellation.BLANCHE_ARMAGNAC);
        assertThat(new Production(null, toutes, List.of()).appellations()).isEqualTo(toutes);
        assertThat(new Production(null, null, null).appellations()).isEmpty();
    }

    @Test
    @DisplayName("RG-OPE-13 : un alambic a un type, une capacité positive ; le détenteur se calcule")
    void rg_ope_13_alambics() {
        assertThat(Production.aucune().detenteurAlambic()).isFalse();
        assertThat(new Production(null, Set.of(), List.of(new Alambic(null, TypeAlambic.ARMAGNACAIS, null)))
                .detenteurAlambic()).isTrue();
        assertThatThrownBy(() -> new Alambic("A-12", null, BigDecimal.TEN))
                .satisfies(e -> assertThat(regle(e)).isEqualTo("RG-OPE-13"));
        assertThatThrownBy(() -> new Alambic("A-12", TypeAlambic.DOUBLE_CHAUFFE, BigDecimal.ZERO))
                .satisfies(e -> assertThat(regle(e)).isEqualTo("RG-OPE-13"));
    }

    @Test
    @DisplayName("RG-OPE-14 : refus à la création ; en partiel, au moins un millésime valide, sans doublon")
    void rg_ope_14_diffusion() {
        assertThat(operateur(TypeOperateur.PRODUCTEUR).diffusion()).isEqualTo(DiffusionMillesimes.refus());

        var partiel = DiffusionMillesimes.saisie(NiveauDiffusion.PARTIEL, List.of(2010, 2005));
        assertThat(partiel.millesimes()).containsExactly(Millesime.de(2005), Millesime.de(2010));

        assertThatThrownBy(() -> DiffusionMillesimes.saisie(NiveauDiffusion.PARTIEL, List.of()))
                .satisfies(e -> assertThat(regle(e)).isEqualTo("RG-OPE-14"));
        assertThatThrownBy(() -> DiffusionMillesimes.saisie(NiveauDiffusion.PARTIEL, List.of(2005, 2005)))
                .satisfies(e -> assertThat(regle(e)).isEqualTo("RG-OPE-14"));
        assertThatThrownBy(() -> DiffusionMillesimes.saisie(NiveauDiffusion.PARTIEL, List.of(1750)))
                .satisfies(e -> assertThat(regle(e)).isEqualTo("RG-OPE-14"));
    }

    @Test
    @DisplayName("RG-OPE-14 : passer de partiel à un autre niveau vide la liste")
    void rg_ope_14_hors_partiel_liste_videe() {
        assertThat(DiffusionMillesimes.saisie(NiveauDiffusion.TOTAL, Arrays.asList(2005, 2010)).millesimes()).isEmpty();
        assertThat(DiffusionMillesimes.saisie(NiveauDiffusion.REFUS, List.of(2005)).millesimes()).isEmpty();
    }

    @Test
    @DisplayName("RG-OPE-15 : accès aux statistiques du portail, faux à la création, fixé par le BNIA")
    void rg_ope_15_acces_statistiques() {
        Operateur operateur = operateur(TypeOperateur.PRODUCTEUR);
        assertThat(operateur.accesStatistiques()).isFalse();

        operateur.modifierAccesStatistiques(true);

        assertThat(operateur.accesStatistiques()).isTrue();
    }
}
