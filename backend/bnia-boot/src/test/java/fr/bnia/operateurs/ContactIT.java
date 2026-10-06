package fr.bnia.operateurs;

import static org.assertj.core.api.Assertions.assertThat;

import fr.bnia.operateurs.application.GererContacts;
import fr.bnia.operateurs.application.SaisieContact;
import fr.bnia.operateurs.application.SaisieDroits;
import fr.bnia.operateurs.domain.Canal;
import fr.bnia.operateurs.domain.partage.AccesPortailAccorde;
import fr.bnia.operateurs.domain.partage.AccesPortailRetire;
import fr.bnia.operateurs.domain.partage.CompteCree;
import fr.bnia.operateurs.domain.partage.IdentifiantCompteModifie;
import fr.bnia.support.AbstractIT;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Contacts : chaîne complète. Les événements sont observés en appelant le cas d'usage depuis le test
 * ({@link RecordApplicationEvents} n'enregistre que ceux du fil du test), et ceux de {@code acces},
 * qui n'existe pas encore, sont publiés par le test.
 */
@RecordApplicationEvents
class ContactIT extends AbstractIT {

    private static final String OPERATEURS = "/api/operateurs";

    @Autowired GererContacts gererContacts;
    @Autowired ApplicationEventPublisher publication;
    @Autowired TransactionTemplate transaction;
    @Autowired ApplicationEvents evenements;

    private long operateur;
    private String contacts;

    @BeforeEach
    void operateur() {
        var saisie = new HashMap<String, Object>();
        saisie.put("type", "PRODUCTEUR");
        saisie.put("code", "T" + suffixe());
        saisie.put("raisonSociale", "Maison Durand " + suffixe());
        saisie.put("accordDiffusionGeneral", false);
        operateur = creer(OPERATEURS, saisie);
        contacts = OPERATEURS + "/" + operateur + "/contacts";
    }

    @Test
    @DisplayName("RG-OPE-27, 28 et 34 : créer, modifier, supprimer un contact ; le journal survit")
    void rg_ope_27_28_34_cycle_de_vie() {
        String email = email();
        long durand = creer(contacts, contact("Durand", email));

        client.post().uri(contacts).body(contact("Martin", email.toUpperCase()))
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT)
                .expectBody().jsonPath("$.regle").isEqualTo("RG-OPE-28");
        client.post().uri(contacts).body(contact(" ", null))
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT)
                .expectBody().jsonPath("$.regle").isEqualTo("RG-OPE-27");

        var modification = contact("Durand", email);
        modification.put("fonction", "Comptable");
        client.put().uri(contacts + "/{id}", durand).body(modification)
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.fonction").isEqualTo("Comptable");
        client.delete().uri(contacts + "/{id}", durand).exchange().expectStatus().isNoContent();

        client.get().uri(contacts + "/{id}/journal", durand)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[*].action").isEqualTo(List.of("SUPPRESSION", "MODIFICATION", "CREATION"))
                .jsonPath("$[1].changements[0].champ").isEqualTo("fonction")
                .jsonPath("$[1].changements[0].apres").isEqualTo("Comptable")
                .jsonPath("$[0].auteur").isEqualTo("BNIA")
                .jsonPath("$[0].nomContact").isEqualTo("Jeanne Durand");
        client.get().uri(contacts + "/{id}", durand).exchange().expectStatus().isNotFound();
    }

    @Test
    @DisplayName("RG-OPE-29 à 31 : droits au portail ; l'identifiant d'un contact reste stable quand l'opérateur est réécrit")
    void rg_ope_29_31_droits() {
        long durand = creer(contacts, contact("Durand", email()));
        long petit = creer(contacts, contact("Petit", null));

        client.put().uri(contacts + "/{id}/portail", petit).body(Map.of("acces", true, "administrateur", false))
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT)
                .expectBody().jsonPath("$.regle").isEqualTo("RG-OPE-29");
        client.put().uri(contacts + "/{id}/portail", durand)
                .body(Map.of("acces", true, "administrateur", false, "modules", List.of("DSM", "FACTURES")))
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.modules").isEqualTo(List.of("DSM", "FACTURES"));
        client.put().uri(contacts + "/{id}/portail", durand).body(Map.of("acces", false, "administrateur", true))
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT)
                .expectBody().jsonPath("$.regle").isEqualTo("RG-OPE-30");
        client.put().uri(contacts + "/{id}/portail", durand).body(Map.of("acces", true, "administrateur", true))
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.modules.length()").isEqualTo(12);

        client.get().uri(contacts + "/{id}", durand).exchange()
                .expectStatus().isOk().expectBody().jsonPath("$.administrateur").isEqualTo(true);
        client.get().uri(OPERATEURS + "/{id}", operateur).exchange()
                .expectBody().jsonPath("$.contacts[*].id").isEqualTo(List.of((int) durand, (int) petit));
    }

    @Test
    @DisplayName("RG-OPE-22, 23 et 33 : désignations, qui tombent à la désactivation ; l'opérateur se supprime avec ses contacts")
    void rg_ope_22_23_33_designations() {
        long durand = creer(contacts, contact("Durand", email()));
        long petit = creer(contacts, contact("Petit", null));

        client.put().uri(OPERATEURS + "/{id}/designations", operateur)
                .body(Map.of("contactPrincipal", petit, "destinataireFacturesContact", petit))
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT)
                .expectBody().jsonPath("$.regle").isEqualTo("RG-OPE-23");
        client.put().uri(OPERATEURS + "/{id}/designations", operateur)
                .body(Map.of("contactPrincipal", petit, "destinataireFacturesContact", durand))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.contactPrincipal").isEqualTo(petit)
                .jsonPath("$.emailFacturation").isNotEmpty();

        // Contact principal et destinataire survivent à la réécriture des contacts (clés différées).
        client.put().uri(contacts + "/{id}", durand).body(contact("Durand-Lafitte", null))
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT)
                .expectBody().jsonPath("$.regle").isEqualTo("RG-OPE-23");
        client.put().uri(contacts + "/{id}", petit).body(contact("Petit-Martin", null)).exchange().expectStatus().isOk();

        client.post().uri(contacts + "/{id}/desactivation", petit)
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.actif").isEqualTo(false).jsonPath("$.contactPrincipal").isEqualTo(false);
        client.get().uri(OPERATEURS + "/{id}", operateur)
                .exchange()
                .expectBody()
                .jsonPath("$.contactPrincipal").doesNotExist()
                .jsonPath("$.destinataireFacturesContact").isEqualTo(durand);
        client.post().uri(contacts + "/{id}/reactivation", petit).exchange()
                .expectStatus().isOk().expectBody().jsonPath("$.acces").isEqualTo(false);

        client.delete().uri(OPERATEURS + "/{id}", operateur).exchange().expectStatus().isNoContent();
    }

    @Test
    @DisplayName("RG-OPE-26 et RG-OPE-29 : accès accordé et retiré publiés en événements, dans la transaction")
    void rg_ope_26_29_evenements() {
        String email = email();
        long durand = gererContacts.creer(operateur, saisie("Durand", email), Canal.BACK_OFFICE).id();

        gererContacts.modifierDroits(operateur, durand, new SaisieDroits(true, false, Set.of()), Canal.BACK_OFFICE);
        assertThat(evenements.stream(AccesPortailAccorde.class))
                .containsExactly(new AccesPortailAccorde(operateur, durand, email));

        client.post().uri(OPERATEURS + "/{id}/desactivation", operateur).exchange().expectStatus().isOk();
        client.get().uri(contacts + "/{id}", durand).exchange()
                .expectBody().jsonPath("$.acces").isEqualTo(false);
        gererContacts.modifierDroits(operateur, durand, new SaisieDroits(true, false, Set.of()), Canal.BACK_OFFICE);
        gererContacts.modifierDroits(operateur, durand, new SaisieDroits(false, false, Set.of()), Canal.BACK_OFFICE);
        assertThat(evenements.stream(AccesPortailRetire.class))
                .containsExactly(new AccesPortailRetire(operateur, durand, email));
    }

    @Test
    @DisplayName("RG-OPE-32 : « compte créé » verrouille l'e-mail ; « identifiant modifié » le reporte")
    void rg_ope_32_evenements_de_acces() {
        String email = email();
        long durand = creer(contacts, contact("Durand", email));

        transaction.executeWithoutResult(t -> publication.publishEvent(new CompteCree(email.toUpperCase())));

        client.get().uri(contacts + "/{id}", durand).exchange()
                .expectBody().jsonPath("$.emailVerrouille").isEqualTo(true);
        client.put().uri(contacts + "/{id}", durand).body(contact("Durand", "autre-" + email))
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT)
                .expectBody().jsonPath("$.regle").isEqualTo("RG-OPE-32");

        String nouveau = "nouveau-" + email;
        transaction.executeWithoutResult(t -> publication.publishEvent(new IdentifiantCompteModifie(email, nouveau)));

        client.get().uri(contacts + "/{id}", durand).exchange()
                .expectBody().jsonPath("$.email").isEqualTo(nouveau).jsonPath("$.emailVerrouille").isEqualTo(true);
        client.get().uri(contacts + "/{id}/journal", durand).exchange()
                .expectBody().jsonPath("$[0].auteur").isEqualTo("Mon compte");
    }

    private static Map<String, Object> contact(String nom, String email) {
        var saisie = new HashMap<String, Object>();
        saisie.put("nom", nom);
        saisie.put("prenom", "Jeanne");
        saisie.put("email", email);
        return saisie;
    }

    private static SaisieContact saisie(String nom, String email) {
        return new SaisieContact(null, nom, "Jeanne", null, Set.of(), null, null, email, null, null, null, null, null);
    }

    private static String email() {
        return "jeanne." + suffixe().toLowerCase() + "@exemple.fr";
    }

    private static String suffixe() {
        return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
