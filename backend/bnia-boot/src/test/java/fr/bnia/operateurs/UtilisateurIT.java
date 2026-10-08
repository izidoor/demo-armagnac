package fr.bnia.operateurs;

import static org.assertj.core.api.Assertions.assertThat;

import fr.bnia.operateurs.domain.partage.Role;
import fr.bnia.support.AbstractIT;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.client.RestTestClient;

/** Utilisateurs, au back-office : liste par le personnel du BNIA, blocage par un ADMIN du BNIA. */
class UtilisateurIT extends AbstractIT {

    @Test
    @DisplayName("RG-OPE-50 et RG-AUT-09 : bloqué par le BNIA, l'utilisateur perd sa session et ne se connecte plus ; débloqué, il revient")
    void rg_ope_50_blocage() {
        long operateur = nouvelOperateur();
        String email = emailUnique("pierre");
        long pierre = membre(operateur, Role.ADMIN, email);
        String admin = emailUnique("admin");
        membre(operateur, Role.ADMIN, admin);
        RestTestClient session = connecte(email);

        client.post().uri("/api/utilisateurs/{id}/blocage", pierre).body(Map.of("motif", " "))
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT)
                .expectBody().jsonPath("$.regle").isEqualTo("RG-OPE-50");
        client.post().uri("/api/utilisateurs/{id}/blocage", pierre).body(Map.of("motif", "Départ de l'entreprise"))
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.statut").isEqualTo("BLOQUE");

        session.get().uri("/api/moi").exchange().expectStatus().isUnauthorized();
        anonyme().post().uri("/api/authentification/connexion").body(Map.of("email", email, "motDePasse", MOT_DE_PASSE))
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT)
                .expectBody().jsonPath("$.regle").isEqualTo("RG-AUT-04");
        assertThat(courriels.dernier(email).getSubject()).isNotBlank();
        assertThat(courriels.envoyesA(admin)).isNotEmpty();
        client.get().uri("/api/operateurs/{o}/membres/{m}", operateur, rattachement(operateur, pierre))
                .exchange()
                .expectBody().jsonPath("$.bloqueParBnia").isEqualTo(true);

        client.post().uri("/api/utilisateurs/{id}/deblocage", pierre)
                .exchange().expectStatus().isOk().expectBody().jsonPath("$.statut").isEqualTo("ACTIF");
        connecte(email);
    }

    @Test
    @DisplayName("RG-OPE-32 et 53 : le personnel du BNIA liste les utilisateurs ; un ADMIN d'opérateur ne le peut pas, ni bloquer")
    void rg_ope_32_53_liste() {
        long operateur = nouvelOperateur();
        String email = emailUnique("liste");
        long jeanne = membre(operateur, Role.ADMIN, email);
        String observateurBnia = emailUnique("claire");
        membre(operateurs.operateurBnia(), Role.OBSERVATEUR, observateurBnia);

        connecte(observateurBnia).get().uri(u -> u.path("/api/utilisateurs").queryParam("recherche", email).build())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].id").isEqualTo(jeanne)
                .jsonPath("$[0].rattachements[0].operateur").isEqualTo(operateur)
                .jsonPath("$[0].rattachements[0].role").isEqualTo("ADMIN");
        connecte(observateurBnia).post().uri("/api/utilisateurs/{id}/blocage", jeanne).body(Map.of("motif", "Essai"))
                .exchange()
                .expectStatus().isForbidden()
                .expectBody().jsonPath("$.regle").isEqualTo("RG-OPE-32");

        RestTestClient session = connecte(email);
        session.get().uri("/api/utilisateurs").exchange()
                .expectStatus().isForbidden()
                .expectBody().jsonPath("$.regle").isEqualTo("RG-OPE-32");
        session.get().uri(u -> u.path("/api/authentification/comptes").queryParam("utilisateurs", jeanne).build())
                .exchange().expectStatus().isForbidden();
        client.get().uri(u -> u.path("/api/authentification/comptes").queryParam("utilisateurs", jeanne).build())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].motDePasseDefini").isEqualTo(true)
                .jsonPath("$[0].derniereConnexion").isNotEmpty();
    }
}
