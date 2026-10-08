package fr.bnia.operateurs;

import fr.bnia.operateurs.domain.partage.Role;
import fr.bnia.support.AbstractIT;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.client.RestTestClient;

/** « Mon compte » : profil, e-mail, opérateur actif (RG-OPE-33, 47 à 49). */
class MonCompteIT extends AbstractIT {

    @Test
    @DisplayName("RG-OPE-33, 47 et 51 : l'utilisateur modifie ses nom et prénom ; le changement se voit dans tous ses opérateurs")
    void rg_ope_33_47_51_profil() {
        long condom = nouvelOperateur();
        long eauze = nouvelOperateur();
        String email = emailUnique("jeanne");
        long jeanne = membre(condom, Role.OBSERVATEUR, email);
        membre(eauze, Role.AGENT, email);
        RestTestClient session = connecte(email);
        long version = ((Number) session.get().uri("/api/moi").exchange().expectBody(Map.class).returnResult()
                .getResponseBody().get("version")).longValue();

        session.put().uri("/api/moi/profil").body(Map.of("nom", "Durand", "prenom", "Jeanne", "version", version))
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.nomComplet").isEqualTo("Jeanne Durand");
        session.put().uri("/api/moi/profil").body(Map.of("nom", "Dupont", "prenom", "Jeanne", "version", version))
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT)
                .expectBody().jsonPath("$.regle").isEqualTo("RG-OPE-51");
        session.put().uri("/api/moi/profil").body(Map.of("nom", " ", "prenom", "Jeanne"))
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT)
                .expectBody().jsonPath("$.regle").isEqualTo("RG-OPE-27");

        client.get().uri("/api/operateurs/{o}/membres/{m}", eauze, rattachement(eauze, jeanne))
                .exchange()
                .expectBody().jsonPath("$.nomComplet").isEqualTo("Jeanne Durand");
    }

    @Test
    @DisplayName("RG-OPE-48 et RG-AUT-10 : un nouvel e-mail confirmé par le lien devient l'identifiant de connexion")
    void rg_ope_48_changement_email() {
        long operateur = nouvelOperateur();
        String ancien = emailUnique("jeanne");
        membre(operateur, Role.AGENT, ancien);
        String autre = emailUnique("autre");
        membre(operateur, Role.AGENT, autre);
        RestTestClient session = connecte(ancien);

        session.post().uri("/api/moi/changement-email").body(Map.of("email", autre.toUpperCase()))
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT)
                .expectBody().jsonPath("$.regle").isEqualTo("RG-OPE-48");
        String nouveau = emailUnique("jeanne.nouvelle");
        session.post().uri("/api/moi/changement-email").body(Map.of("email", nouveau))
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.email").isEqualTo(ancien).jsonPath("$.nouvelEmail").isEqualTo(nouveau);

        anonyme().post().uri("/api/changements-email/confirmation").body(Map.of("jeton", courriels.jeton(nouveau)))
                .exchange().expectStatus().isNoContent();

        session.get().uri("/api/moi").exchange()
                .expectBody().jsonPath("$.email").isEqualTo(nouveau).jsonPath("$.nouvelEmail").doesNotExist();
        anonyme().post().uri("/api/authentification/connexion").body(Map.of("email", ancien, "motDePasse", MOT_DE_PASSE))
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT)
                .expectBody().jsonPath("$.regle").isEqualTo("RG-AUT-02");
        connecte(nouveau).get().uri("/api/moi").exchange().expectStatus().isOk();
    }

    @Test
    @DisplayName("RG-OPE-49 : l'opérateur actif se choisit parmi ses rattachements actifs, et l'application s'y rouvre")
    void rg_ope_49_operateur_actif() {
        long condom = nouvelOperateur();
        long eauze = nouvelOperateur();
        long ailleurs = nouvelOperateur();
        String email = emailUnique("jeanne");
        membre(condom, Role.ADMIN, email);
        membre(eauze, Role.OBSERVATEUR, email);
        RestTestClient session = connecte(email);

        session.get().uri("/api/moi").exchange()
                .expectBody().jsonPath("$.rattachements.length()").isEqualTo(2);
        session.put().uri("/api/moi/operateur-actif").body(Map.of("operateur", eauze))
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.operateurOuvert").isEqualTo(eauze);
        session.put().uri("/api/moi/operateur-actif").body(Map.of("operateur", ailleurs))
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT)
                .expectBody().jsonPath("$.regle").isEqualTo("RG-OPE-49");

        connecte(email).get().uri("/api/moi").exchange()
                .expectBody().jsonPath("$.operateurOuvert").isEqualTo(eauze);
    }
}
