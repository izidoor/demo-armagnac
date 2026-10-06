package fr.bnia.operateurs.domain;

import static fr.bnia.operateurs.domain.OperateurTest.adresse;
import static fr.bnia.operateurs.domain.OperateurTest.identite;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import fr.bnia.commun.domain.RegleEnfreinte;
import fr.bnia.operateurs.domain.partage.AccesPortailAccorde;
import fr.bnia.operateurs.domain.partage.AccesPortailRetire;
import fr.bnia.operateurs.domain.partage.ModulePortail;
import fr.bnia.operateurs.domain.partage.TypeOperateur;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Contacts dans l'agrégat Operateur. Exemple de la spec (§3.2) : Maison Durand à Condom, Mme Durand
 * (administratrice) et M. Petit (maître de chai, sans accès).
 */
class ContactsTest {

    private static final long DURAND = 41;
    private static final long PETIT = 42;

    private Operateur condom;

    @BeforeEach
    void maisonDurand() {
        condom = Operateur.reconstituer(12L, TypeOperateur.PRODUCTEUR, CodeOperateur.de("000012"),
                identite("Maison Durand", adresse("32100")), Declaratif.parDefaut(TypeOperateur.PRODUCTEUR),
                Production.aucune(), DiffusionMillesimes.refus(), false, true, List.of(), null, null);
        condom.ajouterContact(DURAND, contact("Durand", "Jeanne", "jeanne.durand@exemple.fr"));
        condom.ajouterContact(PETIT, contact("Petit", "Paul", null));
        condom.releverFaits();
    }

    static IdentiteContact contact(String nom, String prenom, String email) {
        return new IdentiteContact(null, nom, prenom, null, Set.of(), null, null,
                email == null ? null : Email.de(email, "RG-OPE-28"), null, null, null, null, null);
    }

    private static String regle(Throwable e) {
        return ((RegleEnfreinte) e).regle().orElseThrow();
    }

    private void administrateur(long contact) {
        condom.modifierDroits(contact, new DroitsPortail(true, true, Set.of()), Canal.BACK_OFFICE);
    }

    @Test
    @DisplayName("RG-OPE-27 : le nom du contact est obligatoire")
    void rg_ope_27_nom_obligatoire() {
        assertThatThrownBy(() -> contact(" ", "Jeanne", null)).satisfies(e -> assertThat(regle(e)).isEqualTo("RG-OPE-27"));
    }

    @Test
    @DisplayName("RG-OPE-28 : e-mail unique dans l'opérateur, à la casse près ; le même dans deux opérateurs est permis")
    void rg_ope_28_email_unique() {
        assertThatThrownBy(() -> condom.ajouterContact(43, contact("Martin", null, "JEANNE.durand@exemple.fr")))
                .satisfies(e -> assertThat(regle(e)).isEqualTo("RG-OPE-28"))
                .hasMessageContaining("Jeanne Durand");
        assertThatThrownBy(() -> condom.modifierContact(PETIT, contact("Petit", "Paul", "jeanne.durand@exemple.fr")))
                .satisfies(e -> assertThat(regle(e)).isEqualTo("RG-OPE-28"));
        condom.modifierContact(DURAND, contact("Durand", "Jeanne", "Jeanne.Durand@exemple.fr"));

        Operateur eauze = Operateur.creer(TypeOperateur.PRODUCTEUR, CodeOperateur.de("000013"),
                identite("Chai d'Eauze", adresse("32800")));
        eauze.ajouterContact(50, contact("Durand", "Jeanne", "jeanne.durand@exemple.fr"));
        assertThat(eauze.contacts()).hasSize(1);
    }

    @Test
    @DisplayName("RG-OPE-29 : l'accès exige un e-mail ; l'accorder et le retirer publient un événement")
    void rg_ope_29_acces() {
        assertThatThrownBy(() -> condom.modifierDroits(PETIT, new DroitsPortail(true, false, Set.of()), Canal.BACK_OFFICE))
                .satisfies(e -> assertThat(regle(e)).isEqualTo("RG-OPE-29"));

        condom.modifierDroits(DURAND, new DroitsPortail(true, false, Set.of(ModulePortail.DSM)), Canal.BACK_OFFICE);
        assertThat(condom.releverFaits().evenements())
                .containsExactly(new AccesPortailAccorde(12, DURAND, "jeanne.durand@exemple.fr"));

        assertThatThrownBy(() -> condom.modifierContact(DURAND, contact("Durand", "Jeanne", null)))
                .satisfies(e -> assertThat(regle(e)).isEqualTo("RG-OPE-29"));

        condom.modifierDroits(DURAND, DroitsPortail.aucun(), Canal.BACK_OFFICE);
        assertThat(condom.releverFaits().evenements())
                .containsExactly(new AccesPortailRetire(12, DURAND, "jeanne.durand@exemple.fr"));
    }

    @Test
    @DisplayName("RG-OPE-29 : changer l'e-mail d'un contact qui a l'accès le retire à l'ancienne adresse et l'accorde à la nouvelle")
    void rg_ope_29_email_change() {
        condom.modifierDroits(DURAND, new DroitsPortail(true, false, Set.of()), Canal.BACK_OFFICE);
        condom.releverFaits();

        condom.modifierContact(DURAND, contact("Durand", "Jeanne", "j.durand@exemple.fr"));

        assertThat(condom.releverFaits().evenements()).containsExactly(
                new AccesPortailRetire(12, DURAND, "jeanne.durand@exemple.fr"),
                new AccesPortailAccorde(12, DURAND, "j.durand@exemple.fr"));
    }

    @Test
    @DisplayName("RG-OPE-30 : un administrateur a l'accès et tous les modules ; plusieurs sont possibles")
    void rg_ope_30_administrateur() {
        assertThatThrownBy(() -> new DroitsPortail(false, true, Set.of()))
                .satisfies(e -> assertThat(regle(e)).isEqualTo("RG-OPE-30"));

        administrateur(DURAND);
        condom.modifierContact(PETIT, contact("Petit", "Paul", "paul.petit@exemple.fr"));
        administrateur(PETIT);

        assertThat(condom.contact(DURAND).droits().modules()).isEqualTo(EnumSet.allOf(ModulePortail.class));
        assertThat(condom.contacts()).allMatch(Contact::estAdministrateurActif);
    }

    @Test
    @DisplayName("RG-OPE-30 : au portail, rien ne laisse l'opérateur sans administrateur actif ; au back-office, si")
    void rg_ope_30_dernier_administrateur() {
        administrateur(DURAND);

        for (Runnable action : List.<Runnable>of(
                () -> condom.modifierDroits(DURAND, new DroitsPortail(true, false, Set.of()), Canal.PORTAIL),
                () -> condom.modifierDroits(DURAND, DroitsPortail.aucun(), Canal.PORTAIL),
                () -> condom.desactiverContact(DURAND, Canal.PORTAIL),
                () -> condom.supprimerContact(DURAND, Canal.PORTAIL))) {
            assertThatThrownBy(action::run)
                    .satisfies(e -> assertThat(regle(e)).isEqualTo("RG-OPE-30"))
                    .hasMessageContaining("BNIA");
        }
        assertThat(condom.contact(DURAND).estAdministrateurActif()).isTrue();

        condom.modifierDroits(DURAND, new DroitsPortail(true, false, Set.of()), Canal.BACK_OFFICE);
        assertThat(condom.contact(DURAND).droits().administrateur()).isFalse();
    }

    @Test
    @DisplayName("RG-OPE-30 : au portail, retirer un administrateur est permis s'il en reste un autre")
    void rg_ope_30_autre_administrateur() {
        administrateur(DURAND);
        condom.modifierContact(PETIT, contact("Petit", "Paul", "paul.petit@exemple.fr"));
        administrateur(PETIT);

        condom.desactiverContact(DURAND, Canal.PORTAIL);

        assertThat(condom.contact(PETIT).estAdministrateurActif()).isTrue();
    }

    @Test
    @DisplayName("RG-OPE-31 : des modules seulement avec l'accès ; retirer l'accès les efface")
    void rg_ope_31_modules() {
        condom.modifierDroits(DURAND, new DroitsPortail(true, false, Set.of(ModulePortail.DSM, ModulePortail.FACTURES)),
                Canal.BACK_OFFICE);
        assertThat(condom.contact(DURAND).droits().modules()).containsExactlyInAnyOrder(ModulePortail.DSM, ModulePortail.FACTURES);

        condom.modifierDroits(DURAND, new DroitsPortail(false, false, Set.of(ModulePortail.DSM)), Canal.BACK_OFFICE);
        assertThat(condom.contact(DURAND).droits().modules()).isEmpty();
    }

    @Test
    @DisplayName("RG-OPE-22 : le contact principal est un contact actif ; il cesse de l'être à la désactivation")
    void rg_ope_22_contact_principal() {
        condom.designer(DURAND, null);
        assertThat(condom.contactPrincipal()).contains(DURAND);

        condom.desactiverContact(DURAND, Canal.BACK_OFFICE);
        assertThat(condom.contactPrincipal()).isEmpty();
        assertThatThrownBy(() -> condom.designer(DURAND, null)).satisfies(e -> assertThat(regle(e)).isEqualTo("RG-OPE-22"));
        assertThatThrownBy(() -> condom.designer(99L, null)).isInstanceOf(fr.bnia.commun.domain.ElementIntrouvable.class);
    }

    @Test
    @DisplayName("RG-OPE-23 : destinataire des factures, contact actif avec e-mail, ou adresse saisie")
    void rg_ope_23_destinataire_factures() {
        assertThatThrownBy(() -> condom.designer(null, DestinataireFactures.contact(PETIT)))
                .satisfies(e -> assertThat(regle(e)).isEqualTo("RG-OPE-23")).hasMessageContaining("Paul Petit");

        condom.designer(null, DestinataireFactures.contact(DURAND));
        assertThat(condom.emailFacturation()).contains(Email.de("jeanne.durand@exemple.fr", "RG-OPE-24"));
        assertThatThrownBy(() -> condom.modifierContact(DURAND, contact("Durand", "Jeanne", null)))
                .satisfies(e -> assertThat(regle(e)).isEqualTo("RG-OPE-23"));

        condom.designer(null, DestinataireFactures.adresse(Email.de("compta@maison-durand.fr", "RG-OPE-24")));
        assertThat(condom.emailFacturation()).contains(Email.de("compta@maison-durand.fr", "RG-OPE-24"));
        assertThatThrownBy(() -> new DestinataireFactures(DURAND, Email.de("a@b.fr", "RG-OPE-24")))
                .satisfies(e -> assertThat(regle(e)).isEqualTo("RG-OPE-23"));
    }

    @Test
    @DisplayName("RG-OPE-32 : l'e-mail d'un compte n'est plus modifiable ; il suit le changement d'identifiant")
    void rg_ope_32_email_verrouille() {
        assertThat(condom.verrouillerEmail(Email.de("Jeanne.Durand@exemple.fr", "RG-OPE-32"))).isTrue();
        assertThat(condom.contact(DURAND).emailVerrouille()).isTrue();
        assertThat(condom.contact(PETIT).emailVerrouille()).isFalse();

        assertThatThrownBy(() -> condom.modifierContact(DURAND, contact("Durand", "Jeanne", "autre@exemple.fr")))
                .satisfies(e -> assertThat(regle(e)).isEqualTo("RG-OPE-32")).hasMessageContaining("Mon compte");
        condom.modifierContact(DURAND, contact("Durand-Lafitte", "Jeanne", "jeanne.durand@exemple.fr"));

        condom.remplacerEmail(Email.de("jeanne.durand@exemple.fr", "RG-OPE-32"), Email.de("jeanne@durand.fr", "RG-OPE-32"));
        assertThat(condom.contact(DURAND).identite().email().valeur()).isEqualTo("jeanne@durand.fr");
        assertThat(condom.contact(DURAND).emailVerrouille()).isTrue();
    }

    @Test
    @DisplayName("RG-OPE-33 : désactiver retire l'accès et les désignations ; réactivé, le contact n'a pas d'accès")
    void rg_ope_33_desactiver_reactiver_supprimer() {
        condom.modifierDroits(DURAND, new DroitsPortail(true, false, Set.of()), Canal.BACK_OFFICE);
        condom.designer(DURAND, DestinataireFactures.contact(DURAND));
        condom.releverFaits();

        condom.desactiverContact(DURAND, Canal.BACK_OFFICE);
        assertThat(condom.releverFaits().evenements())
                .containsExactly(new AccesPortailRetire(12, DURAND, "jeanne.durand@exemple.fr"));
        assertThat(condom.destinataireFactures()).isEmpty();
        assertThatThrownBy(() -> condom.modifierDroits(DURAND, new DroitsPortail(true, false, Set.of()), Canal.BACK_OFFICE))
                .satisfies(e -> assertThat(regle(e)).isEqualTo("RG-OPE-33"));

        condom.reactiverContact(DURAND);
        assertThat(condom.contact(DURAND).estActif()).isTrue();
        assertThat(condom.contact(DURAND).droits().acces()).isFalse();

        condom.supprimerContact(PETIT, Canal.BACK_OFFICE);
        assertThat(condom.contacts()).extracting(Contact::id).containsExactly(DURAND);
    }

    @Test
    @DisplayName("RG-OPE-26 : désactiver l'opérateur retire l'accès de tous ses contacts")
    void rg_ope_26_operateur_desactive() {
        administrateur(DURAND);
        condom.releverFaits();

        condom.desactiver();

        assertThat(condom.contact(DURAND).droits().acces()).isFalse();
        assertThat(condom.releverFaits().evenements())
                .containsExactly(new AccesPortailRetire(12, DURAND, "jeanne.durand@exemple.fr"));
        condom.reactiver();
        assertThat(condom.contact(DURAND).droits().acces()).as("l'accès se réaccorde contact par contact").isFalse();
    }

    @Test
    @DisplayName("RG-OPE-34 : chaque action est journalisée, une modification avec ses champs avant et après")
    void rg_ope_34_journal() {
        condom.modifierContact(PETIT, new IdentiteContact(null, "Petit", "Paul", "Maître de chai", Set.of(3L, 1L),
                "05 62 00 00 00", null, null, null, null, null, null, null));
        condom.modifierDroits(DURAND, new DroitsPortail(true, false, Set.of(ModulePortail.DSM)), Canal.BACK_OFFICE);
        condom.modifierDroits(DURAND, new DroitsPortail(true, false, Set.of(ModulePortail.DSM, ModulePortail.FACTURES)),
                Canal.BACK_OFFICE);
        condom.supprimerContact(PETIT, Canal.BACK_OFFICE);

        List<EntreeJournal> journal = condom.releverFaits().journal();

        assertThat(journal).extracting(EntreeJournal::action).containsExactly(ActionContact.MODIFICATION,
                ActionContact.ACCES_ACCORDE, ActionContact.DROITS_MODIFIES, ActionContact.SUPPRESSION);
        assertThat(journal.get(0).changements()).containsExactly(
                new Changement(ChampContact.FONCTION, null, "Maître de chai"),
                new Changement(ChampContact.CENTRES_INTERET, null, "1,3"),
                new Changement(ChampContact.TELEPHONE_PROFESSIONNEL, null, "05 62 00 00 00"));
        assertThat(journal.get(2).changements()).containsExactly(new Changement(ChampContact.MODULES,
                "Déclarations de stocks et mouvements", "Déclarations de stocks et mouvements, Factures"));
        assertThat(journal.get(3).nomContact()).as("le nom survit à la suppression").isEqualTo("Paul Petit");
        assertThat(condom.releverFaits().journal()).as("relevés une seule fois").isEmpty();
    }
}
