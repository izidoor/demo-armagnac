package fr.bnia.operateurs.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import fr.bnia.commun.domain.RegleEnfreinte;
import fr.bnia.operateurs.domain.partage.TypeOperateur;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class OperateurTest {

    static IdentiteOperateur identite(String raisonSociale, Adresse adresse) {
        return new IdentiteOperateur(null, null, null, null, null, null, null, null, raisonSociale, null, adresse, null,
                null, null, null, null, null, null, null, false, null);
    }

    static IdentiteOperateur identite(String raisonSociale, Adresse adresse, String siret) {
        return identite(raisonSociale, adresse, siret, null);
    }

    static IdentiteOperateur identite(String raisonSociale, Adresse adresse, String siret, String numeroTva) {
        return new IdentiteOperateur(null, null, Siret.facultatif(siret), NumeroTva.facultatif(numeroTva), null, null,
                null, null, raisonSociale, null, adresse, null, null, null, null, null, null, null, null, false, null);
    }

    static Adresse adresse(String codePostal) {
        return new Adresse("1 route de Condom", null, null, codePostal, "Condom", 1L);
    }

    @Test
    @DisplayName("RG-OPE-01 : le type d'opérateur est obligatoire")
    void rg_ope_01_type_obligatoire() {
        assertThatThrownBy(() -> Operateur.creer(null, CodeOperateur.de("000001"), identite("Durand", adresse("32100"))))
                .isInstanceOf(RegleEnfreinte.class)
                .extracting(e -> ((RegleEnfreinte) e).regle().orElseThrow()).isEqualTo("RG-OPE-01");
    }

    @Test
    @DisplayName("RG-OPE-02 : code et raison sociale sont obligatoires")
    void rg_ope_02_code_et_raison_sociale_obligatoires() {
        assertThatThrownBy(() -> CodeOperateur.de("  ")).isInstanceOf(RegleEnfreinte.class).hasMessageContaining("code");
        assertThatThrownBy(() -> identite(" ", adresse("32100")))
                .isInstanceOf(RegleEnfreinte.class).hasMessageContaining("raison sociale");
    }

    @Test
    @DisplayName("RG-OPE-02 : une fiche non facturable ou non certifiable s'enregistre, et nomme ce qui manque")
    void rg_ope_02_facturable_et_certifiable() {
        Operateur complet = Operateur.creer(TypeOperateur.PRODUCTEUR, CodeOperateur.de("000001"),
                identite("Durand", adresse("32100"), "41234567800012"));
        Operateur vide = Operateur.creer(TypeOperateur.PRODUCTEUR, CodeOperateur.de("000002"),
                identite("Petit", new Adresse(" ", null, null, "", null, null)));

        assertThat(complet.manquePourFacturer(true)).isEmpty();
        assertThat(complet.manquePourFacturer(false)).containsExactly("destinataire des factures");
        assertThat(complet.manquePourCertifier()).containsExactly("région");
        complet.modifierProduction(complet.declaratif(), new Production(7L, Set.of(), List.of()));
        assertThat(complet.manquePourCertifier()).isEmpty();
        assertThat(vide.estActif()).isTrue();
        assertThat(vide.manquePourFacturer(false))
                .containsExactly("SIRET", "adresse", "code postal", "ville", "pays", "destinataire des factures");
        assertThat(vide.manquePourCertifier()).containsExactly("adresse", "code postal", "ville", "pays", "région");
    }

    @ParameterizedTest
    @ValueSource(strings = {"FR55776999955", "fr 55 776 999 955", "FR23412345678"})
    @DisplayName("RG-OPE-06 : le n° de TVA est FR, une clé de 2 chiffres et le SIREN")
    void rg_ope_06_numero_tva_valide(String saisie) {
        assertThat(NumeroTva.facultatif(saisie).valeur()).matches("FR[0-9]{11}");
        assertThat(NumeroTva.facultatif("  ")).isNull();
    }

    @ParameterizedTest
    @ValueSource(strings = {"FR56776999955", "FR5577699995", "DE55776999955", "FRAB776999955"})
    @DisplayName("RG-OPE-06 : un n° de TVA mal formé ou de clé fausse est refusé")
    void rg_ope_06_numero_tva_invalide(String saisie) {
        assertThatThrownBy(() -> new NumeroTva(saisie))
                .isInstanceOf(RegleEnfreinte.class)
                .extracting(e -> ((RegleEnfreinte) e).regle().orElseThrow()).isEqualTo("RG-OPE-06");
    }

    @Test
    @DisplayName("RG-OPE-06 : un n° de TVA qui ne concorde pas avec le SIRET avertit, sans bloquer")
    void rg_ope_06_numero_tva_discordant() {
        IdentiteOperateur concordante = identite("Durand", adresse("32100"), "41234567800012", "FR23412345678");
        IdentiteOperateur discordante = identite("Durand", adresse("32100"), "77699995500029", "FR23412345678");

        assertThat(concordante.tvaDiscordante()).isFalse();
        assertThat(discordante.tvaDiscordante()).isTrue();
        assertThat(identite("Durand", adresse("32100")).tvaDiscordante()).as("sans n° de TVA").isFalse();
    }

    @Test
    @DisplayName("RG-OPE-05 : changer le type ne change pas le code, qui est signalé non conforme")
    void rg_ope_05_changer_le_type_garde_le_code() {
        Operateur operateur = Operateur.creer(TypeOperateur.PRODUCTEUR, CodeOperateur.de("000001"),
                identite("Durand", adresse("32100")));
        assertThat(operateur.serieAttendueSiCodeNonConforme(true)).isEmpty();

        operateur.modifierIdentite(TypeOperateur.NEGOCIANT, operateur.code(), identite("Durand", adresse("32100")));

        assertThat(operateur.code()).isEqualTo(CodeOperateur.de("000001"));
        assertThat(operateur.serieAttendueSiCodeNonConforme(true)).contains(new SerieCode("N32", 4));
    }

    @Test
    @DisplayName("RG-OPE-05 : changer le code postal d'un négociant rend son code non conforme")
    void rg_ope_05_changer_le_code_postal() {
        Operateur operateur = Operateur.creer(TypeOperateur.NEGOCIANT, CodeOperateur.de("N320001"),
                identite("Durand", adresse("32100")));

        operateur.modifierIdentite(TypeOperateur.NEGOCIANT, operateur.code(), identite("Durand", adresse("40000")));

        assertThat(operateur.serieAttendueSiCodeNonConforme(true)).contains(new SerieCode("N40", 4));
        assertThat(operateur.serieAttendueSiCodeNonConforme(false)).as("sans code postal français : rien à proposer")
                .isEmpty();
    }

    @Test
    @DisplayName("RG-OPE-08 et RG-OPE-09 : raison sociale et groupe comparés à la casse, aux accents et aux espaces près")
    void rg_ope_09_groupe_normalise() {
        IdentiteOperateur a = new IdentiteOperateur(" Maison  Durand ", null, null, null, null, null, null, null,
                "Château Élise", null, null, null, null, null, null, null, null, null, null, false, null);
        IdentiteOperateur b = new IdentiteOperateur("MAISON DURAND", null, null, null, null, null, null, null,
                "chateau  elise", null, null, null, null, null, null, null, null, null, null, false, null);

        assertThat(a.groupeNormalise()).isEqualTo(b.groupeNormalise()).isEqualTo("MAISON DURAND");
        assertThat(a.raisonSocialeNormalisee()).isEqualTo(b.raisonSocialeNormalisee());
        assertThat(identite("Durand", null).groupeNormalise()).isNull();
    }
}
