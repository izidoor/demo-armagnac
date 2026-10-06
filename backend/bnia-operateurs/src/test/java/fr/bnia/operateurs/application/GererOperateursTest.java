package fr.bnia.operateurs.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import fr.bnia.commun.domain.ConflitUnicite;
import fr.bnia.commun.domain.ElementUtilise;
import fr.bnia.commun.domain.RegleEnfreinte;
import fr.bnia.operateurs.domain.CodeOperateur;
import fr.bnia.operateurs.domain.partage.TypeOperateur;
import fr.bnia.referentiels.application.partage.ReferentielsLecture;
import fr.bnia.referentiels.domain.partage.LangueCertificat;
import fr.bnia.referentiels.domain.partage.NiveauDetailCertificat;
import fr.bnia.referentiels.domain.partage.PaysVue;
import fr.bnia.referentiels.domain.partage.ValeurVue;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class GererOperateursTest {

    private static final long FRANCE = 1;
    private static final long BELGIQUE = 2;
    private static final long TITRE_ACTIF = 10;
    private static final long TITRE_INACTIF = 11;

    private final OperateursEnMemoire memoire = new OperateursEnMemoire();
    private final ReferentielsLecture referentiels = mock(ReferentielsLecture.class);
    private final GererOperateurs gerer = new GererOperateurs(memoire, memoire, memoire, referentiels);

    @BeforeEach
    void referentiels() {
        PaysVue france = pays(FRANCE, "FR", "France");
        when(referentiels.pays(FRANCE)).thenReturn(Optional.of(france));
        when(referentiels.pays(BELGIQUE)).thenReturn(Optional.of(pays(BELGIQUE, "BE", "Belgique")));
        when(referentiels.paysActifs()).thenReturn(List.of(france));
        when(referentiels.titre(TITRE_ACTIF)).thenReturn(Optional.of(new ValeurVue(TITRE_ACTIF, "M", "Monsieur", true)));
        when(referentiels.titre(TITRE_INACTIF)).thenReturn(Optional.of(new ValeurVue(TITRE_INACTIF, "ME", "Maître", false)));
    }

    @Test
    @DisplayName("RG-OPE-02 : le pays vaut France par défaut")
    void rg_ope_02_france_par_defaut() {
        var cree = gerer.creer(saisie(TypeOperateur.PRODUCTEUR, null, "Domaine Durand", "32100", null));

        assertThat(cree.identite().adresse().pays()).isEqualTo(FRANCE);
        assertThat(cree.estActif()).isTrue();
    }

    @Test
    @DisplayName("RG-OPE-03 : sans code saisi, le numéro suivant de la série est attribué")
    void rg_ope_03_numero_suivant() {
        gerer.creer(saisie(TypeOperateur.PRODUCTEUR, "000041", "Domaine A", "32100", FRANCE));
        gerer.creer(saisie(TypeOperateur.NEGOCIANT, "N320003", "Négoce B", "32100", FRANCE));

        assertThat(gerer.creer(saisie(TypeOperateur.AUTRE, null, "Courtier C", "40000", FRANCE)).code())
                .isEqualTo(CodeOperateur.de("000042"));
        assertThat(gerer.creer(saisie(TypeOperateur.NEGOCIANT, null, "Négoce D", "32500", FRANCE)).code())
                .isEqualTo(CodeOperateur.de("N320004"));
        assertThat(gerer.proposerCode(TypeOperateur.NEGOCIANT, "40000", FRANCE)).contains(CodeOperateur.de("N400001"));
    }

    @Test
    @DisplayName("RG-OPE-03 : un négociant étranger sans code saisi est refusé, avec la raison")
    void rg_ope_03_negociant_etranger_sans_code() {
        assertThat(gerer.proposerCode(TypeOperateur.NEGOCIANT, "1000", BELGIQUE)).isEmpty();
        assertThatThrownBy(() -> gerer.creer(saisie(TypeOperateur.NEGOCIANT, null, "Négoce belge", "1000", BELGIQUE)))
                .isInstanceOf(RegleEnfreinte.class)
                .hasMessageContaining("code postal français");
    }

    @Test
    @DisplayName("RG-OPE-04 : le code est unique, à la casse près, à la création comme à la modification")
    void rg_ope_04_code_unique() {
        gerer.creer(saisie(TypeOperateur.NEGOCIANT, "N320001", "Négoce A", "32100", FRANCE));
        long autre = gerer.creer(saisie(TypeOperateur.NEGOCIANT, "N320002", "Négoce B", "32100", FRANCE)).id();

        assertThatThrownBy(() -> gerer.creer(saisie(TypeOperateur.NEGOCIANT, " n320001 ", "Négoce C", "32100", FRANCE)))
                .isInstanceOf(ConflitUnicite.class).hasMessageContaining("N320001".toLowerCase());
        assertThatThrownBy(() -> gerer.modifierIdentite(autre, saisie(TypeOperateur.NEGOCIANT, "N320001", "Négoce B", "32100", FRANCE)))
                .isInstanceOf(ConflitUnicite.class)
                .extracting(e -> ((ConflitUnicite) e).regle().orElseThrow()).isEqualTo("RG-OPE-04");
        assertThat(gerer.modifierIdentite(autre, saisie(TypeOperateur.NEGOCIANT, "N320009", "Négoce B", "32100", FRANCE))
                .code()).as("le code se corrige à la main").isEqualTo(CodeOperateur.de("N320009"));
    }

    @Test
    @DisplayName("RG-OPE-05 : attribuer un nouveau code selon le type et le code postal actuels")
    void rg_ope_05_nouveau_code() {
        long id = gerer.creer(saisie(TypeOperateur.PRODUCTEUR, "000001", "Domaine Durand", "32100", FRANCE)).id();
        gerer.modifierIdentite(id, saisie(TypeOperateur.NEGOCIANT, "000001", "Domaine Durand", "40000", FRANCE));

        FicheOperateur fiche = gerer.fiche(id);
        assertThat(fiche.codeConforme()).isFalse();
        assertThat(fiche.codePropose()).contains(CodeOperateur.de("N400001"));

        assertThat(gerer.attribuerNouveauCode(id).code()).isEqualTo(CodeOperateur.de("N400001"));
        assertThat(gerer.fiche(id).codeConforme()).isTrue();
    }

    @Test
    @DisplayName("RG-OPE-06 et RG-OPE-08 : doublons signalés en nommant la fiche, sans bloquer")
    void rg_ope_06_08_doublons() {
        var existant = saisie(TypeOperateur.PRODUCTEUR, "000007", "Domaine du Tariquet", "32800", FRANCE);
        long tariquet = gerer.creer(avecSiret(existant, "41234567800012")).id();

        List<Doublon> doublons = gerer.doublons("domaine du  TARIQUET", null, "412 345 678 00012", null, null);

        assertThat(doublons).extracting(Doublon::champ, Doublon::regle, d -> d.operateur().code()).containsExactly(
                org.assertj.core.groups.Tuple.tuple(Doublon.Champ.RAISON_SOCIALE, "RG-OPE-08", "000007"),
                org.assertj.core.groups.Tuple.tuple(Doublon.Champ.SIRET, "RG-OPE-06", "000007"));
        assertThat(gerer.doublons("Domaine du Tariquet", null, "41234567800012", null, tariquet))
                .as("la fiche modifiée n'est pas son propre doublon").isEmpty();
        assertThat(gerer.doublons(null, null, "4123", null, null)).as("saisie en cours ignorée").isEmpty();
        assertThat(gerer.creer(avecSiret(saisie(TypeOperateur.PRODUCTEUR, "000008", "Domaine du Tariquet", "32800", FRANCE),
                "41234567800012")).id()).as("non bloquant").isNotNull();
    }

    @Test
    @DisplayName("RG-OPE-07 : l'identifiant tiers ODG est unique s'il est renseigné")
    void rg_ope_07_identifiant_odg_unique() {
        gerer.creer(avecOdg(saisie(TypeOperateur.PRODUCTEUR, "000001", "Domaine A", "32100", FRANCE), "ODG-17"));
        gerer.creer(saisie(TypeOperateur.PRODUCTEUR, "000002", "Domaine B", "32100", FRANCE));
        gerer.creer(saisie(TypeOperateur.PRODUCTEUR, "000003", "Domaine C", "32100", FRANCE));

        assertThatThrownBy(() -> gerer.creer(avecOdg(saisie(TypeOperateur.PRODUCTEUR, "000004", "Domaine D", "32100", FRANCE), "odg-17")))
                .isInstanceOf(ConflitUnicite.class)
                .extracting(e -> ((ConflitUnicite) e).regle().orElseThrow()).isEqualTo("RG-OPE-07");
    }

    @Test
    @DisplayName("RG-OPE-09 : la fiche liste les autres fiches de son groupe")
    void rg_ope_09_autres_du_groupe() {
        long condom = gerer.creer(avecGroupe(saisie(TypeOperateur.PRODUCTEUR, "000001", "Chai de Condom", "32100", FRANCE), "Maison Durand")).id();
        gerer.creer(avecGroupe(saisie(TypeOperateur.PRODUCTEUR, "000002", "Chai d'Eauze", "32800", FRANCE), "MAISON  DURAND"));
        gerer.creer(saisie(TypeOperateur.PRODUCTEUR, "000003", "Domaine seul", "32100", FRANCE));

        assertThat(gerer.fiche(condom).autresDuGroupe()).extracting(OperateurResume::code).containsExactly("000002");
    }

    @Test
    @DisplayName("RG-OPE-21 : un titre choisi doit être actif ; celui déjà porté reste accepté")
    void rg_ope_21_reference_active() {
        var avecTitreInactif = avecTitre(saisie(TypeOperateur.PRODUCTEUR, "000001", "Domaine A", "32100", FRANCE), TITRE_INACTIF);
        assertThatThrownBy(() -> gerer.creer(avecTitreInactif))
                .isInstanceOf(RegleEnfreinte.class).hasMessageContaining("Maître")
                .extracting(e -> ((RegleEnfreinte) e).regle().orElseThrow()).isEqualTo("RG-OPE-21");

        long id = gerer.creer(avecTitre(saisie(TypeOperateur.PRODUCTEUR, "000001", "Domaine A", "32100", FRANCE), TITRE_ACTIF)).id();
        when(referentiels.titre(TITRE_ACTIF)).thenReturn(Optional.of(new ValeurVue(TITRE_ACTIF, "M", "Monsieur", false)));

        assertThat(gerer.modifierIdentite(id, avecTitre(saisie(TypeOperateur.PRODUCTEUR, "000001", "Domaine A2", "32100", FRANCE), TITRE_ACTIF))
                .identite().raisonSociale()).isEqualTo("Domaine A2");
    }

    @Test
    @DisplayName("RG-OPE-25 : un opérateur utilisé ne se supprime pas ; on le désactive, et il se réactive")
    void rg_ope_25_suppression() {
        long libre = gerer.creer(saisie(TypeOperateur.PRODUCTEUR, "000001", "Domaine A", "32100", FRANCE)).id();
        long utilise = gerer.creer(saisie(TypeOperateur.PRODUCTEUR, "000002", "Domaine B", "32100", FRANCE)).id();
        memoire.utilises.add(utilise);

        gerer.supprimer(libre);
        assertThat(memoire.parId(libre)).isEmpty();
        assertThatThrownBy(() -> gerer.supprimer(utilise)).isInstanceOf(ElementUtilise.class);

        assertThat(gerer.desactiver(utilise).estActif()).isFalse();
        assertThat(gerer.reactiver(utilise).estActif()).isTrue();
    }

    private static PaysVue pays(long id, String iso, String libelle) {
        return new PaysVue(id, iso, iso, libelle, 1, true, LangueCertificat.FR_EN, NiveauDetailCertificat.CUMUL_DEGRE,
                Optional.empty(), true);
    }

    static SaisieOperateur saisie(TypeOperateur type, String code, String raisonSociale, String codePostal, Long pays) {
        return new SaisieOperateur(type, code, null, null, null, null, null, null, null, raisonSociale, null,
                "1 route d'Auch", null, null, codePostal, "Condom", pays, null, null, null, null, null, null, null,
                null, null, false, null);
    }

    private static SaisieOperateur avecSiret(SaisieOperateur s, String siret) {
        return new SaisieOperateur(s.type(), s.code(), s.groupe(), s.cvi(), siret, s.accise(), s.bureauDouanes(),
                s.identifiantOdg(), s.titre(), s.raisonSociale(), s.nomCommercial(), s.adresseLigne1(),
                s.adresseLigne2(), s.adresseLigne3(), s.codePostal(), s.ville(), s.pays(), s.telephone1(),
                s.telephone2(), s.portable(), s.emailGeneral(), s.siteWeb(), s.siteCommercial(), s.latitude(),
                s.longitude(), s.membreBnia(), s.accordDiffusionGeneral(), s.commentaireInterne());
    }

    private static SaisieOperateur avecOdg(SaisieOperateur s, String odg) {
        return new SaisieOperateur(s.type(), s.code(), s.groupe(), s.cvi(), s.siret(), s.accise(), s.bureauDouanes(),
                odg, s.titre(), s.raisonSociale(), s.nomCommercial(), s.adresseLigne1(),
                s.adresseLigne2(), s.adresseLigne3(), s.codePostal(), s.ville(), s.pays(), s.telephone1(),
                s.telephone2(), s.portable(), s.emailGeneral(), s.siteWeb(), s.siteCommercial(), s.latitude(),
                s.longitude(), s.membreBnia(), s.accordDiffusionGeneral(), s.commentaireInterne());
    }

    private static SaisieOperateur avecGroupe(SaisieOperateur s, String groupe) {
        return new SaisieOperateur(s.type(), s.code(), groupe, s.cvi(), s.siret(), s.accise(), s.bureauDouanes(),
                s.identifiantOdg(), s.titre(), s.raisonSociale(), s.nomCommercial(), s.adresseLigne1(),
                s.adresseLigne2(), s.adresseLigne3(), s.codePostal(), s.ville(), s.pays(), s.telephone1(),
                s.telephone2(), s.portable(), s.emailGeneral(), s.siteWeb(), s.siteCommercial(), s.latitude(),
                s.longitude(), s.membreBnia(), s.accordDiffusionGeneral(), s.commentaireInterne());
    }

    private static SaisieOperateur avecTitre(SaisieOperateur s, long titre) {
        return new SaisieOperateur(s.type(), s.code(), s.groupe(), s.cvi(), s.siret(), s.accise(), s.bureauDouanes(),
                s.identifiantOdg(), titre, s.raisonSociale(), s.nomCommercial(), s.adresseLigne1(),
                s.adresseLigne2(), s.adresseLigne3(), s.codePostal(), s.ville(), s.pays(), s.telephone1(),
                s.telephone2(), s.portable(), s.emailGeneral(), s.siteWeb(), s.siteCommercial(), s.latitude(),
                s.longitude(), s.membreBnia(), s.accordDiffusionGeneral(), s.commentaireInterne());
    }
}
