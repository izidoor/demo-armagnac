package fr.bnia.operateurs;

import static org.assertj.core.api.Assertions.assertThat;

import fr.bnia.operateurs.domain.partage.Role;
import fr.bnia.support.AbstractIT;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.client.RestTestClient;

/** Contacts, fiches professionnelles et désignations : chaîne complète, droits vérifiés par les cas d'usage. */
class ContactIT extends AbstractIT {

    private static final String OPERATEURS = "/api/operateurs";

    private long operateur;
    private String contacts;
    private String fiches;

    @BeforeEach
    void operateur() {
        operateur = nouvelOperateur();
        contacts = OPERATEURS + "/" + operateur + "/contacts";
        fiches = OPERATEURS + "/" + operateur + "/fiches";
    }

    @Test
    @DisplayName("RG-OPE-27, 28, 30, 34 et 45 : déclarer un contact et sa fiche, le modifier, le supprimer ; le journal survit")
    void rg_ope_27_28_30_34_45_cycle_de_vie() {
        String email = emailUnique("jeanne");
        var saisie = contact("Durand", email);
        saisie.put("fonction", "Maître de chai");
        long durand = creer(contacts, saisie);

        Map<?, ?> lu = lire(client, contacts + "/" + durand);
        Map<?, ?> fiche = (Map<?, ?>) lu.get("fiche");
        assertThat(fiche.get("type")).isEqualTo("CONTACT");
        assertThat(fiche.get("fonction")).isEqualTo("Maître de chai");

        client.post().uri(contacts).body(contact("Martin", email.toUpperCase()))
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT)
                .expectBody().jsonPath("$.regle").isEqualTo("RG-OPE-28");
        client.post().uri(contacts).body(contact(" ", null))
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT)
                .expectBody().jsonPath("$.regle").isEqualTo("RG-OPE-27");

        client.put().uri(fiches + "/{id}", fiche.get("id"))
                .body(Map.of("fonction", "Comptable", "telephoneProfessionnel", "05 62 00 00 00"))
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.fonction").isEqualTo("Comptable").jsonPath("$.modifieePar").isNotEmpty();
        client.put().uri(contacts + "/{id}", durand).body(Map.of("nom", "Durand-Lafitte", "prenom", "Jeanne"))
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.nomComplet").isEqualTo("Jeanne Durand-Lafitte");

        client.delete().uri(contacts + "/{id}", durand).exchange().expectStatus().isNoContent();
        client.get().uri(contacts + "/{id}", durand).exchange().expectStatus().isNotFound();
        client.get().uri(fiches + "/{id}", fiche.get("id")).exchange().expectStatus().isNotFound();

        client.get().uri(OPERATEURS + "/{id}/journal", operateur)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[*].action").isEqualTo(List.of("CONTACT_SUPPRIME", "CONTACT_MODIFIE", "FICHE_MODIFIEE",
                        "CONTACT_CREE"))
                .jsonPath("$[2].changements[?(@.champ == 'fonction')].apres").isEqualTo(List.of("Comptable"))
                .jsonPath("$[0].nomCible").isEqualTo("Jeanne Durand-Lafitte")
                .jsonPath("$[0].acteur").isNotEmpty();
    }

    @Test
    @DisplayName("RG-OPE-51 : de deux modifications concurrentes d'un contact ou d'une fiche, la seconde est refusée (409)")
    void rg_ope_51_version() {
        long durand = creer(contacts, contact("Durand", null));
        Map<?, ?> lu = lire(client, contacts + "/" + durand);
        long version = ((Number) lu.get("version")).longValue();
        Map<?, ?> fiche = (Map<?, ?>) lu.get("fiche");
        long versionFiche = ((Number) fiche.get("version")).longValue();

        client.put().uri(contacts + "/{id}", durand).body(Map.of("nom", "Durand", "prenom", "Paul", "version", version))
                .exchange().expectStatus().isOk();
        client.put().uri(contacts + "/{id}", durand).body(Map.of("nom", "Durand", "prenom", "Pierre", "version", version))
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT)
                .expectBody().jsonPath("$.regle").isEqualTo("RG-OPE-51");

        client.put().uri(fiches + "/{id}", fiche.get("id")).body(Map.of("fonction", "Caviste", "version", versionFiche))
                .exchange().expectStatus().isOk();
        client.put().uri(fiches + "/{id}", fiche.get("id")).body(Map.of("fonction", "Chef", "version", versionFiche))
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT)
                .expectBody().jsonPath("$.regle").isEqualTo("RG-OPE-51");
    }

    @Test
    @DisplayName("RG-OPE-22 et 23 : désignations par fiche ; un destinataire sans e-mail est refusé ; elles tombent à la suppression")
    void rg_ope_22_23_designations() {
        long durand = creer(contacts, contact("Durand", emailUnique("durand")));
        long petit = creer(contacts, contact("Petit", null));
        long ficheDurand = fiche(durand);
        long fichePetit = fiche(petit);

        client.put().uri(OPERATEURS + "/{id}/designations", operateur)
                .body(Map.of("contactPrincipal", fichePetit, "destinataireFacturesFiche", fichePetit))
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT)
                .expectBody().jsonPath("$.regle").isEqualTo("RG-OPE-23");
        client.put().uri(OPERATEURS + "/{id}/designations", operateur)
                .body(Map.of("contactPrincipal", fichePetit, "destinataireFacturesFiche", ficheDurand))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.contactPrincipal").isEqualTo(fichePetit)
                .jsonPath("$.destinataireFacturesFiche").isEqualTo(ficheDurand)
                .jsonPath("$.emailFacturation").isNotEmpty();

        client.put().uri(contacts + "/{id}", durand).body(Map.of("nom", "Durand"))
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT)
                .expectBody().jsonPath("$.regle").isEqualTo("RG-OPE-23");
        client.get().uri(contacts + "/{id}", petit).exchange()
                .expectBody().jsonPath("$.contactPrincipal").isEqualTo(true);

        client.delete().uri(contacts + "/{id}", petit).exchange().expectStatus().isNoContent();
        client.get().uri(OPERATEURS + "/{id}", operateur)
                .exchange()
                .expectBody()
                .jsonPath("$.contactPrincipal").doesNotExist()
                .jsonPath("$.destinataireFacturesFiche").isEqualTo(ficheDurand);
    }

    @Test
    @DisplayName("RG-OPE-45 : supprimer un contact annule l'invitation en attente partie de lui")
    void rg_ope_45_suppression_annule_l_invitation() {
        String email = emailUnique("paul");
        long petit = creer(contacts, contact("Petit", email));
        long invitation = creer(OPERATEURS + "/" + operateur + "/invitations",
                Map.of("contact", petit, "email", email, "role", "AGENT"));

        client.delete().uri(contacts + "/{id}", petit).exchange().expectStatus().isNoContent();

        client.get().uri(OPERATEURS + "/{o}/invitations/{i}", operateur, invitation)
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.statut").isEqualTo("ANNULEE");
        anonyme().get().uri("/api/invitations/{jeton}", courriels.jeton(email)).exchange()
                .expectStatus().is4xxClientError();
    }

    @Test
    @DisplayName("RG-OPE-28 : l'e-mail d'un membre ou d'une invitation en attente avertit sans bloquer")
    void rg_ope_28_avertissements() {
        String membre = emailUnique("membre");
        membre(operateur, Role.AGENT, membre);
        String invite = emailUnique("invite");
        client.post().uri(OPERATEURS + "/{id}/invitations", operateur)
                .body(Map.of("email", invite, "role", "OBSERVATEUR", "nom", "Lafitte", "prenom", "Marc"))
                .exchange().expectStatus().isCreated();

        client.get().uri(u -> u.path(contacts + "/avertissements").queryParam("email", membre.toUpperCase()).build())
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.length()").isEqualTo(1);
        client.get().uri(u -> u.path(contacts + "/avertissements").queryParam("email", invite).build())
                .exchange()
                .expectBody().jsonPath("$.length()").isEqualTo(1);
        creer(contacts, contact("Membre", membre));
    }

    @Test
    @DisplayName("RG-OPE-31, 33 et 46 : un OBSERVATEUR lit les contacts, ne les gère pas, et modifie sa propre fiche")
    void rg_ope_31_33_46_droits() {
        long durand = creer(contacts, contact("Durand", null));
        String email = emailUnique("observateur");
        long observateur = membre(operateur, Role.OBSERVATEUR, email);
        RestTestClient session = connecte(email);

        session.get().uri(contacts + "/{id}", durand).exchange().expectStatus().isOk();
        session.post().uri(contacts).body(contact("Petit", null))
                .exchange()
                .expectStatus().isForbidden()
                .expectBody().jsonPath("$.regle").isEqualTo("RG-OPE-31");
        session.put().uri(fiches + "/{id}", fiche(durand)).body(Map.of("fonction", "Caviste"))
                .exchange()
                .expectStatus().isForbidden()
                .expectBody().jsonPath("$.regle").isEqualTo("RG-OPE-46");

        long saFiche = ((Number) ((Map<?, ?>) lire(session, OPERATEURS + "/" + operateur + "/membres/"
                + rattachement(operateur, observateur)).get("fiche")).get("id")).longValue();
        session.put().uri(fiches + "/{id}", saFiche).body(Map.of("fonction", "Caviste"))
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.fonction").isEqualTo("Caviste");

        long autre = nouvelOperateur();
        session.get().uri(OPERATEURS + "/{id}/annuaire", autre)
                .exchange()
                .expectStatus().isForbidden()
                .expectBody().jsonPath("$.regle").isEqualTo("RG-OPE-31");
    }

    private long fiche(long contact) {
        return ((Number) ((Map<?, ?>) lire(client, contacts + "/" + contact).get("fiche")).get("id")).longValue();
    }

    private static Map<?, ?> lire(RestTestClient session, String uri) {
        return session.get().uri(uri).exchange().expectStatus().isOk().expectBody(Map.class).returnResult()
                .getResponseBody();
    }

    private static Map<String, Object> contact(String nom, String email) {
        var saisie = new HashMap<String, Object>();
        saisie.put("nom", nom);
        saisie.put("prenom", "Jeanne");
        saisie.put("email", email);
        return saisie;
    }
}
