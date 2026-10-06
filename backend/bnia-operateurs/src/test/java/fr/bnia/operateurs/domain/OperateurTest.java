package fr.bnia.operateurs.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import fr.bnia.commun.domain.RegleEnfreinte;
import fr.bnia.operateurs.domain.partage.TypeOperateur;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class OperateurTest {

    static IdentiteOperateur identite(String raisonSociale, Adresse adresse) {
        return new IdentiteOperateur(null, null, null, null, null, null, null, raisonSociale, null, adresse, null,
                null, null, null, null, null, null, null, false, null);
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
    @DisplayName("RG-OPE-02 : une fiche sans adresse s'enregistre, et nomme ce qui manque")
    void rg_ope_02_fiche_incomplete() {
        Operateur complet = Operateur.creer(TypeOperateur.PRODUCTEUR, CodeOperateur.de("000001"),
                identite("Durand", adresse("32100")));
        Operateur incomplet = Operateur.creer(TypeOperateur.PRODUCTEUR, CodeOperateur.de("000002"),
                identite("Petit", new Adresse(" ", null, null, "", null, null)));

        assertThat(complet.champsManquants()).isEmpty();
        assertThat(incomplet.estActif()).isTrue();
        assertThat(incomplet.champsManquants()).containsExactly("adresse", "code postal", "ville", "pays");
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
        IdentiteOperateur a = new IdentiteOperateur(" Maison  Durand ", null, null, null, null, null, null,
                "Château Élise", null, null, null, null, null, null, null, null, null, null, false, null);
        IdentiteOperateur b = new IdentiteOperateur("MAISON DURAND", null, null, null, null, null, null,
                "chateau  elise", null, null, null, null, null, null, null, null, null, null, false, null);

        assertThat(a.groupeNormalise()).isEqualTo(b.groupeNormalise()).isEqualTo("MAISON DURAND");
        assertThat(a.raisonSocialeNormalisee()).isEqualTo(b.raisonSocialeNormalisee());
        assertThat(identite("Durand", null).groupeNormalise()).isNull();
    }
}
