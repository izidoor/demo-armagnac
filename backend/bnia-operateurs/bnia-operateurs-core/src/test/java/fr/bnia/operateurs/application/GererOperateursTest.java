package fr.bnia.operateurs.application;

import static fr.bnia.operateurs.application.Memoire.FRANCE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import fr.bnia.commun.domain.AccesRefuse;
import fr.bnia.commun.domain.ConflitUnicite;
import fr.bnia.commun.domain.ElementUtilise;
import fr.bnia.commun.domain.ModificationConcurrente;
import fr.bnia.commun.domain.RegleEnfreinte;
import fr.bnia.operateurs.domain.ActionJournal;
import fr.bnia.operateurs.domain.CodeOperateur;
import fr.bnia.operateurs.domain.Contact;
import fr.bnia.operateurs.domain.Operateur;
import fr.bnia.operateurs.domain.StatutInvitation;
import fr.bnia.operateurs.domain.partage.Acteur;
import fr.bnia.operateurs.domain.partage.NiveauDiffusion;
import fr.bnia.operateurs.domain.partage.PeriodiciteDsm;
import fr.bnia.operateurs.domain.partage.Role;
import fr.bnia.operateurs.domain.partage.StatutDeclarationStock;
import fr.bnia.operateurs.domain.partage.TypeOperateur;
import fr.bnia.operateurs.domain.partage.UtilisateurSupprime;
import fr.bnia.referentiels.domain.partage.LangueCertificat;
import fr.bnia.referentiels.domain.partage.NiveauDetailCertificat;
import fr.bnia.referentiels.domain.partage.PaysVue;
import fr.bnia.referentiels.domain.partage.RegionVue;
import fr.bnia.referentiels.domain.partage.ValeurVue;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class GererOperateursTest {

    private static final long BELGIQUE = 2;
    private static final long TITRE_ACTIF = 10;
    private static final long TITRE_INACTIF = 11;

    private final Memoire memoire = new Memoire();
    private final GererOperateurs gerer = memoire.gererOperateurs();
    private Acteur anne;
    private Acteur luc;
    private Acteur claire;

    @BeforeEach
    void personnel() {
        when(memoire.referentiels.pays(BELGIQUE)).thenReturn(Optional.of(new PaysVue(BELGIQUE, "BE", "BE", "Belgique",
                1, true, LangueCertificat.FR_EN, NiveauDetailCertificat.CUMUL_DEGRE, Optional.empty(), true)));
        when(memoire.referentiels.titre(TITRE_INACTIF)).thenReturn(Optional.of(new ValeurVue(TITRE_INACTIF, "ME", "Maître", false)));
        anne = memoire.membre(memoire.bnia, Role.ADMIN, "Anne", "Lamarque");
        luc = memoire.membre(memoire.bnia, Role.AGENT, "Luc", "Dubos");
        claire = memoire.membre(memoire.bnia, Role.OBSERVATEUR, "Claire", "Sentex");
    }

    @Test
    @DisplayName("RG-OPE-02 : le pays vaut France par défaut")
    void rg_ope_02_france_par_defaut() {
        var cree = gerer.creer(luc, saisie(TypeOperateur.PRODUCTEUR, null, "Domaine Durand", "32100", null), null);

        assertThat(cree.identite().adresse().pays()).isEqualTo(FRANCE);
        assertThat(cree.estActif()).isTrue();
    }

    @Test
    @DisplayName("RG-OPE-03 : sans code saisi, le numéro suivant de la série est attribué")
    void rg_ope_03_numero_suivant() {
        gerer.creer(luc, saisie(TypeOperateur.PRODUCTEUR, "000041", "Domaine A", "32100", FRANCE), null);
        gerer.creer(luc, saisie(TypeOperateur.NEGOCIANT, "N320003", "Négoce B", "32100", FRANCE), null);

        assertThat(gerer.creer(luc, saisie(TypeOperateur.AUTRE, null, "Courtier C", "40000", FRANCE), null).code())
                .isEqualTo(CodeOperateur.de("000042"));
        assertThat(gerer.creer(luc, saisie(TypeOperateur.NEGOCIANT, null, "Négoce D", "32500", FRANCE), null).code())
                .isEqualTo(CodeOperateur.de("N320004"));
        assertThat(gerer.proposerCode(luc, TypeOperateur.NEGOCIANT, "40000", FRANCE)).contains(CodeOperateur.de("N400001"));
        assertThat(gerer.proposerCode(luc, TypeOperateur.BNIA, "32000", FRANCE)).as("RG-OPE-39").isEmpty();
    }

    @Test
    @DisplayName("RG-OPE-03 : un négociant étranger sans code saisi est refusé, avec la raison")
    void rg_ope_03_negociant_etranger_sans_code() {
        assertThat(gerer.proposerCode(luc, TypeOperateur.NEGOCIANT, "1000", BELGIQUE)).isEmpty();
        assertThatThrownBy(() -> gerer.creer(luc, saisie(TypeOperateur.NEGOCIANT, null, "Négoce belge", "1000", BELGIQUE), null))
                .isInstanceOf(RegleEnfreinte.class)
                .hasMessageContaining("code postal français");
    }

    @Test
    @DisplayName("RG-OPE-04 : le code est unique, à la casse près, à la création comme à la modification")
    void rg_ope_04_code_unique() {
        gerer.creer(luc, saisie(TypeOperateur.NEGOCIANT, "N320001", "Négoce A", "32100", FRANCE), null);
        long autre = gerer.creer(luc, saisie(TypeOperateur.NEGOCIANT, "N320002", "Négoce B", "32100", FRANCE), null).id();

        assertThatThrownBy(() -> gerer.creer(luc, saisie(TypeOperateur.NEGOCIANT, " n320001 ", "Négoce C", "32100", FRANCE), null))
                .isInstanceOf(ConflitUnicite.class).hasMessageContaining("n320001");
        assertThatThrownBy(() -> gerer.modifierIdentite(luc, autre, saisie(TypeOperateur.NEGOCIANT, "N320001", "Négoce B", "32100", FRANCE)))
                .isInstanceOf(ConflitUnicite.class)
                .extracting(e -> ((ConflitUnicite) e).regle().orElseThrow()).isEqualTo("RG-OPE-04");
        assertThat(gerer.modifierIdentite(luc, autre, saisie(TypeOperateur.NEGOCIANT, "N320009", "Négoce B", "32100", FRANCE))
                .code()).as("le code se corrige à la main").isEqualTo(CodeOperateur.de("N320009"));
    }

    @Test
    @DisplayName("RG-OPE-05 : attribuer un nouveau code selon le type et le code postal actuels")
    void rg_ope_05_nouveau_code() {
        long id = gerer.creer(luc, saisie(TypeOperateur.PRODUCTEUR, "000001", "Domaine Durand", "32100", FRANCE), null).id();
        gerer.modifierIdentite(luc, id, saisie(TypeOperateur.NEGOCIANT, "000001", "Domaine Durand", "40000", FRANCE));

        FicheOperateur fiche = gerer.fiche(luc, id);
        assertThat(fiche.codeConforme()).isFalse();
        assertThat(fiche.codePropose()).contains(CodeOperateur.de("N400001"));

        assertThat(gerer.attribuerNouveauCode(luc, id).code()).isEqualTo(CodeOperateur.de("N400001"));
        assertThat(gerer.fiche(luc, id).codeConforme()).isTrue();
    }

    @Test
    @DisplayName("RG-OPE-06 et RG-OPE-08 : doublons signalés en nommant la fiche, sans bloquer")
    void rg_ope_06_08_doublons() {
        var existant = saisie(TypeOperateur.PRODUCTEUR, "000007", "Domaine du Tariquet", "32800", FRANCE);
        long tariquet = gerer.creer(luc, avecSiret(existant, "41234567800012"), null).id();

        List<Doublon> doublons = gerer.doublons(claire, "domaine du  TARIQUET", null, "412 345 678 00012", null, null);

        assertThat(doublons).extracting(Doublon::champ, Doublon::regle, d -> d.operateur().code()).containsExactly(
                org.assertj.core.groups.Tuple.tuple(Doublon.Champ.RAISON_SOCIALE, "RG-OPE-08", "000007"),
                org.assertj.core.groups.Tuple.tuple(Doublon.Champ.SIRET, "RG-OPE-06", "000007"));
        assertThat(gerer.doublons(claire, "Domaine du Tariquet", null, "41234567800012", null, tariquet))
                .as("la fiche modifiée n'est pas son propre doublon").isEmpty();
        assertThat(gerer.doublons(claire, null, null, "4123", null, null)).as("saisie en cours ignorée").isEmpty();
        assertThat(gerer.creer(luc, avecSiret(saisie(TypeOperateur.PRODUCTEUR, "000008", "Domaine du Tariquet", "32800", FRANCE),
                "41234567800012"), null).id()).as("non bloquant").isNotNull();
    }

    @Test
    @DisplayName("RG-OPE-07 : l'identifiant tiers ODG est unique s'il est renseigné")
    void rg_ope_07_identifiant_odg_unique() {
        gerer.creer(luc, avecOdg(saisie(TypeOperateur.PRODUCTEUR, "000001", "Domaine A", "32100", FRANCE), "ODG-17"), null);
        gerer.creer(luc, saisie(TypeOperateur.PRODUCTEUR, "000002", "Domaine B", "32100", FRANCE), null);

        assertThatThrownBy(() -> gerer.creer(luc, avecOdg(saisie(TypeOperateur.PRODUCTEUR, "000004", "Domaine D", "32100", FRANCE), "odg-17"), null))
                .isInstanceOf(ConflitUnicite.class)
                .extracting(e -> ((ConflitUnicite) e).regle().orElseThrow()).isEqualTo("RG-OPE-07");
    }

    @Test
    @DisplayName("RG-OPE-09 : la fiche liste les autres fiches de son groupe")
    void rg_ope_09_autres_du_groupe() {
        long condom = gerer.creer(luc, avecGroupe(saisie(TypeOperateur.PRODUCTEUR, "000001", "Chai de Condom", "32100", FRANCE), "Maison Durand"), null).id();
        gerer.creer(luc, avecGroupe(saisie(TypeOperateur.PRODUCTEUR, "000002", "Chai d'Eauze", "32800", FRANCE), "MAISON  DURAND"), null);
        gerer.creer(luc, saisie(TypeOperateur.PRODUCTEUR, "000003", "Domaine seul", "32100", FRANCE), null);

        assertThat(gerer.fiche(claire, condom).autresDuGroupe()).extracting(OperateurResume::code).containsExactly("000002");
    }

    @Test
    @DisplayName("RG-OPE-21 : un titre choisi doit être actif ; celui déjà porté reste accepté")
    void rg_ope_21_reference_active() {
        var avecTitreInactif = avecTitre(saisie(TypeOperateur.PRODUCTEUR, "000001", "Domaine A", "32100", FRANCE), TITRE_INACTIF);
        assertThatThrownBy(() -> gerer.creer(luc, avecTitreInactif, null))
                .isInstanceOf(RegleEnfreinte.class).hasMessageContaining("Maître")
                .extracting(e -> ((RegleEnfreinte) e).regle().orElseThrow()).isEqualTo("RG-OPE-21");

        long id = gerer.creer(luc, avecTitre(saisie(TypeOperateur.PRODUCTEUR, "000001", "Domaine A", "32100", FRANCE), TITRE_ACTIF), null).id();
        when(memoire.referentiels.titre(TITRE_ACTIF)).thenReturn(Optional.of(new ValeurVue(TITRE_ACTIF, "M", "Monsieur", false)));

        assertThat(gerer.modifierIdentite(luc, id, avecTitre(saisie(TypeOperateur.PRODUCTEUR, "000001", "Domaine A2", "32100", FRANCE), TITRE_ACTIF))
                .identite().raisonSociale()).isEqualTo("Domaine A2");
    }

    @Test
    @DisplayName("RG-OPE-21 : la région choisie à l'onglet Production doit être active")
    void rg_ope_21_region_active() {
        long id = gerer.creer(luc, saisie(TypeOperateur.PRODUCTEUR, "000001", "Domaine A", "32100", FRANCE), null).id();
        when(memoire.referentiels.region(5)).thenReturn(Optional.of(new RegionVue(5, "IDF", "Île-de-France", false, false)));
        var production = new SaisieProduction(PeriodiciteDsm.ANNUELLE, StatutDeclarationStock.OUI, 5L, Set.of(), List.of(), null);

        assertThatThrownBy(() -> gerer.modifierProduction(luc, id, production))
                .isInstanceOf(RegleEnfreinte.class).hasMessageContaining("Île-de-France")
                .extracting(e -> ((RegleEnfreinte) e).regle().orElseThrow()).isEqualTo("RG-OPE-21");
    }

    @Test
    @DisplayName("RG-OPE-23 : le destinataire des factures est une fiche de l'opérateur dont la personne a un e-mail")
    void rg_ope_23_destinataire() {
        long id = memoire.operateur("000001", "Domaine A");
        Contact sansEmail = memoire.contact(id, "Paul", "Petit", null);
        Contact avecEmail = memoire.contact(id, "Sophie", "Martin", "sophie@domaine-a.fr");

        assertThatThrownBy(() -> gerer.designer(luc, id, new SaisieDesignations(null, sansEmail.fiche(), null, null)))
                .hasMessageContaining("Paul Petit")
                .extracting(e -> ((RegleEnfreinte) e).regle().orElseThrow()).isEqualTo("RG-OPE-23");
        gerer.designer(luc, id, new SaisieDesignations(sansEmail.fiche(), avecEmail.fiche(), null, null));

        FicheOperateur fiche = gerer.fiche(luc, id);
        assertThat(fiche.operateur().contactPrincipal()).contains(sansEmail.fiche());
        assertThat(fiche.emailFacturation()).contains("sophie@domaine-a.fr");
    }

    @Test
    @DisplayName("RG-OPE-25 et RG-OPE-44 : un opérateur inutilisé se supprime avec ses personnes ; ses membres sans autre accès sont supprimés")
    void rg_ope_25_suppression() {
        long libre = memoire.operateur("000001", "Domaine A");
        long utilise = memoire.operateur("000002", "Domaine B");
        Acteur seul = memoire.membre(libre, Role.ADMIN, "Marc", "Lafitte");
        Acteur ailleurs = memoire.membre(libre, Role.AGENT, "Sophie", "Martin");
        memoire.membre(utilise, Role.AGENT, "Sophie", "Martin");
        memoire.operateurs.utilises.add(utilise);

        gerer.supprimer(anne, libre);

        assertThat(memoire.operateurs.parId(libre)).isEmpty();
        assertThat(memoire.utilisateurs.parId(seul.utilisateur())).isEmpty();
        assertThat(memoire.utilisateurs.parId(ailleurs.utilisateur())).as("encore membre ailleurs").isPresent();
        assertThat(memoire.evenements.publies).containsExactly(new UtilisateurSupprime(seul.utilisateur()));
        assertThatThrownBy(() -> gerer.supprimer(anne, utilise)).isInstanceOf(ElementUtilise.class);
        assertThatThrownBy(() -> gerer.supprimer(anne, memoire.bnia)).hasMessageContaining("BNIA");
    }

    @Test
    @DisplayName("RG-OPE-26 : désactiver gèle les rattachements et annule les invitations en attente ; réactiver les rend")
    void rg_ope_26_desactivation() {
        long id = memoire.operateur("000001", "Domaine A");
        Acteur jeanne = memoire.membre(id, Role.ADMIN, "Jeanne", "Durand");
        memoire.gererInvitations().inviter(jeanne, id, SaisieInvitation.personne("marc@lafitte.fr", Role.AGENT, "Lafitte", "Marc"));

        gerer.desactiver(anne, id);

        assertThat(memoire.habilitations().droits(jeanne, id).aucunAcces()).isTrue();
        assertThat(memoire.invitations.tous()).extracting(i -> i.statut()).containsExactly(StatutInvitation.ANNULEE);
        assertThat(memoire.rattachements.deLOperateur(id)).as("les rattachements restent").hasSize(1);
        gerer.reactiver(anne, id);
        assertThat(memoire.habilitations().droits(jeanne, id).aucunAcces()).isFalse();
    }

    @Test
    @DisplayName("RG-OPE-32 : lister, créer, désactiver et supprimer sont réservés au BNIA, et la fin d'un opérateur à un ADMIN BNIA")
    void rg_ope_32_refus() {
        long id = memoire.operateur("000001", "Domaine A");
        Acteur jeanne = memoire.membre(id, Role.ADMIN, "Jeanne", "Durand");

        assertThatThrownBy(() -> gerer.lister(jeanne, FiltreOperateurs.aucun())).isInstanceOf(AccesRefuse.class);
        assertThatThrownBy(() -> gerer.creer(claire, saisie(TypeOperateur.PRODUCTEUR, "000009", "X", "32100", FRANCE), null))
                .isInstanceOf(AccesRefuse.class).hasMessageContaining("Contacts");
        assertThatThrownBy(() -> gerer.desactiver(luc, id)).isInstanceOf(AccesRefuse.class).hasMessageContaining("ADMIN du BNIA");
        assertThatThrownBy(() -> gerer.supprimer(jeanne, id)).isInstanceOf(AccesRefuse.class);
        assertThatThrownBy(() -> gerer.fiche(memoire.membre(memoire.operateur("000002", "B"), Role.ADMIN, "Paul", "Roy"), id))
                .as("RG-OPE-31 : un autre opérateur ne lit pas cette fiche").isInstanceOf(AccesRefuse.class);
        assertThat(gerer.lister(claire, FiltreOperateurs.aucun())).as("RG-OPE-39 : sans le BNIA")
                .extracting(OperateurLigne::code).containsExactly("000001", "000002");
    }

    @Test
    @DisplayName("RG-OPE-36 : un opérateur modifie son profil et sa diffusion, pas les champs réservés au BNIA")
    void rg_ope_36_profil() {
        var saisie = avecOdg(saisie(TypeOperateur.PRODUCTEUR, "000001", "Domaine A", "32100", FRANCE), "ODG-1");
        long id = gerer.creer(luc, saisie, null).id();
        gerer.modifierDiffusion(luc, id, new SaisieDiffusion(NiveauDiffusion.REFUS, List.of(), true, null));
        Acteur jeanne = memoire.membre(id, Role.AGENT, "Jeanne", "Durand");

        var profil = new SaisieProfilOperateur(TITRE_ACTIF, "Les Vignes", "2 rue du Moulin", null, null, "32100", "Condom",
                FRANCE, "05 62 00 00 00", null, null, "contact@vignes.fr", "https://vignes.fr", null, null, null, true, null);
        var modifie = gerer.modifierProfil(jeanne, id, profil);
        gerer.modifierDiffusion(jeanne, id, new SaisieDiffusion(NiveauDiffusion.PARTIEL, List.of(2005), true, null));

        assertThat(modifie.identite().nomCommercial()).isEqualTo("Les Vignes");
        assertThat(modifie.identite().raisonSociale()).isEqualTo("Domaine A");
        assertThat(modifie.identite().identifiantOdg()).isEqualTo("ODG-1");
        Operateur relu = memoire.operateurs.parId(id).orElseThrow();
        assertThat(relu.accesStatistiques()).isTrue();
        assertThat(relu.diffusion().niveau()).isEqualTo(NiveauDiffusion.PARTIEL);
        assertThatThrownBy(() -> gerer.modifierDiffusion(jeanne, id, new SaisieDiffusion(NiveauDiffusion.PARTIEL, List.of(2005), false, null)))
                .isInstanceOf(AccesRefuse.class).hasMessageContaining("statistiques");
        assertThatThrownBy(() -> gerer.modifierIdentite(jeanne, id, saisie))
                .isInstanceOf(AccesRefuse.class).extracting(e -> ((AccesRefuse) e).regle().orElseThrow()).isEqualTo("RG-OPE-36");
        assertThat(gerer.fiche(jeanne, id).droits().personnelBnia()).as("commentaire interne masqué à l'écran").isFalse();
    }

    @Test
    @DisplayName("RG-OPE-37 : la recherche exige trois caractères")
    void rg_ope_37_trois_caracteres() {
        assertThatThrownBy(() -> gerer.rechercher(claire, " ab "))
                .extracting(e -> ((RegleEnfreinte) e).regle().orElseThrow()).isEqualTo("RG-OPE-37");
        assertThatThrownBy(() -> gerer.rechercher(new Acteur(999), "Durand")).isInstanceOf(AccesRefuse.class);
    }

    @Test
    @DisplayName("RG-OPE-40 : un ADMIN du BNIA crée un opérateur en invitant son premier ADMIN, dans la même transaction")
    void rg_ope_40_premier_admin() {
        var premier = SaisieInvitation.personne("marc@lafitte.fr", Role.OBSERVATEUR, "Lafitte", "Marc");
        assertThatThrownBy(() -> gerer.creer(luc, saisie(TypeOperateur.AUTRE, "000010", "Distillerie Lafitte", "32250", FRANCE), premier))
                .as("un AGENT du BNIA crée, mais n'invite pas").isInstanceOf(AccesRefuse.class);

        Operateur cree = gerer.creer(anne, saisie(TypeOperateur.AUTRE, "000010", "Distillerie Lafitte", "32250", FRANCE), premier);

        assertThat(memoire.invitations.enAttente(cree.id())).singleElement()
                .satisfies(i -> assertThat(i.role()).as("toujours ADMIN").isEqualTo(Role.ADMIN));
        assertThat(memoire.courriels.envoyes).singleElement().satisfies(c -> {
            assertThat(c.destinataire()).isEqualTo("marc@lafitte.fr");
            assertThat(c.texte()).contains("https://bnia.example/invitation?jeton=");
        });
        assertThat(memoire.journal.derniere().action()).isEqualTo(ActionJournal.INVITATION_ENVOYEE);
        assertThat(memoire.rattachements.deLOperateur(cree.id())).as("aucun membre avant l'acceptation").isEmpty();
    }

    @Test
    @DisplayName("RG-OPE-51 : une fiche d'opérateur modifiée entre-temps n'est pas écrasée")
    void rg_ope_51_conflit() {
        long id = gerer.creer(luc, saisie(TypeOperateur.PRODUCTEUR, "000001", "Domaine A", "32100", FRANCE), null).id();
        long lue = gerer.fiche(luc, id).operateur().version();
        gerer.modifierIdentite(luc, id, avecVersion(saisie(TypeOperateur.PRODUCTEUR, "000001", "Domaine A1", "32100", FRANCE), lue));

        assertThatThrownBy(() -> gerer.modifierIdentite(luc, id,
                avecVersion(saisie(TypeOperateur.PRODUCTEUR, "000001", "Domaine A2", "32100", FRANCE), lue)))
                .isInstanceOf(ModificationConcurrente.class);
    }

    static SaisieOperateur saisie(TypeOperateur type, String code, String raisonSociale, String codePostal, Long pays) {
        return new SaisieOperateur(type, code, null, null, null, null, null, null, null, raisonSociale, null,
                "1 route d'Auch", null, null, codePostal, "Condom", pays, null, null, null, null, null, null, null,
                null, null, false, null, null);
    }

    private static SaisieOperateur copie(SaisieOperateur s, String groupe, String siret, String odg, Long titre, Long version) {
        return new SaisieOperateur(s.type(), s.code(), groupe, s.cvi(), siret, s.accise(), s.bureauDouanes(),
                odg, titre, s.raisonSociale(), s.nomCommercial(), s.adresseLigne1(),
                s.adresseLigne2(), s.adresseLigne3(), s.codePostal(), s.ville(), s.pays(), s.telephone1(),
                s.telephone2(), s.portable(), s.emailGeneral(), s.siteWeb(), s.siteCommercial(), s.latitude(),
                s.longitude(), s.membreBnia(), s.accordDiffusionGeneral(), s.commentaireInterne(), version);
    }

    private static SaisieOperateur avecSiret(SaisieOperateur s, String siret) {
        return copie(s, s.groupe(), siret, s.identifiantOdg(), s.titre(), s.version());
    }

    private static SaisieOperateur avecOdg(SaisieOperateur s, String odg) {
        return copie(s, s.groupe(), s.siret(), odg, s.titre(), s.version());
    }

    private static SaisieOperateur avecGroupe(SaisieOperateur s, String groupe) {
        return copie(s, groupe, s.siret(), s.identifiantOdg(), s.titre(), s.version());
    }

    private static SaisieOperateur avecTitre(SaisieOperateur s, long titre) {
        return copie(s, s.groupe(), s.siret(), s.identifiantOdg(), titre, s.version());
    }

    private static SaisieOperateur avecVersion(SaisieOperateur s, long version) {
        return copie(s, s.groupe(), s.siret(), s.identifiantOdg(), s.titre(), version);
    }
}
