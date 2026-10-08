package fr.bnia.authentification;

import static org.assertj.core.api.Assertions.assertThat;

import fr.bnia.operateurs.domain.partage.Role;
import fr.bnia.support.AbstractIT;
import fr.bnia.support.Navigateur;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.client.RestTestClient;

/** Connexion unique, code par e-mail, blocage, mots de passe, session, journal (spec 03). */
class AuthentificationIT extends AbstractIT {

    private static final String AUTH = "/api/authentification";

    @Autowired
    private RestTestClient brut;

    private long operateur;
    private String email;
    private long utilisateur;

    @BeforeEach
    void unMembre() {
        operateur = nouvelOperateur();
        email = emailUnique("jeanne");
        utilisateur = membre(operateur, Role.AGENT, email);
    }

    @Test
    @DisplayName("RG-AUT-01, 02 et 09 : messages explicites, code par e-mail, puis session ; déconnexion")
    void rg_aut_01_02_09_connexion() {
        RestTestClient session = anonyme();
        session.get().uri("/api/moi").exchange().expectStatus().isUnauthorized();

        session.post().uri(AUTH + "/connexion").body(Map.of("email", emailUnique("inconnu"), "motDePasse", MOT_DE_PASSE))
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT)
                .expectBody().jsonPath("$.regle").isEqualTo("RG-AUT-02").jsonPath("$.detail").isEqualTo("Aucun compte pour cet e-mail.");
        session.post().uri(AUTH + "/connexion").body(Map.of("email", email, "motDePasse", "mauvais-mot-de-passe"))
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT)
                .expectBody().jsonPath("$.detail").isEqualTo("Mot de passe incorrect.");

        session.post().uri(AUTH + "/code").body(Map.of("code", "000000", "faireConfiance", false))
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT)
                .expectBody().jsonPath("$.regle").isEqualTo("RG-AUT-01");
        session.post().uri(AUTH + "/connexion").body(Map.of("email", email.toUpperCase(), "motDePasse", MOT_DE_PASSE))
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.etape").isEqualTo("CODE_REQUIS");
        assertThat(courriels.dernier(email).getText()).contains("000000");
        session.get().uri(AUTH + "/session").exchange()
                .expectBody().jsonPath("$.connecte").isEqualTo(false).jsonPath("$.codeAttendu").isEqualTo(true);
        session.get().uri("/api/moi").exchange().expectStatus().isUnauthorized();

        session.post().uri(AUTH + "/code").body(Map.of("code", "000000", "faireConfiance", false))
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.etape").isEqualTo("CONNECTE");
        session.get().uri(AUTH + "/session").exchange()
                .expectBody().jsonPath("$.connecte").isEqualTo(true).jsonPath("$.utilisateur").isEqualTo(utilisateur);
        session.get().uri("/api/moi").exchange().expectStatus().isOk();

        session.post().uri(AUTH + "/deconnexion").exchange().expectStatus().isNoContent();
        session.get().uri("/api/moi").exchange().expectStatus().isUnauthorized();
    }

    @Test
    @DisplayName("RG-AUT-03 : trois échecs bloquent le compte, même au bon mot de passe ; la réinitialisation le débloque (RG-AUT-08)")
    void rg_aut_03_08_blocage_et_reinitialisation() {
        RestTestClient session = anonyme();
        for (int i = 0; i < 2; i++) {
            session.post().uri(AUTH + "/connexion").body(Map.of("email", email, "motDePasse", "mauvais-mot-de-passe"))
                    .exchange().expectBody().jsonPath("$.regle").isEqualTo("RG-AUT-02");
        }
        session.post().uri(AUTH + "/connexion").body(Map.of("email", email, "motDePasse", "mauvais-mot-de-passe"))
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT)
                .expectBody().jsonPath("$.regle").isEqualTo("RG-AUT-03");
        session.post().uri(AUTH + "/connexion").body(Map.of("email", email, "motDePasse", MOT_DE_PASSE))
                .exchange()
                .expectBody().jsonPath("$.regle").isEqualTo("RG-AUT-03");

        session.post().uri(AUTH + "/mot-de-passe-oublie").body(Map.of("email", emailUnique("inconnu")))
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT)
                .expectBody().jsonPath("$.regle").isEqualTo("RG-AUT-08");
        session.post().uri(AUTH + "/mot-de-passe-oublie").body(Map.of("email", email)).exchange().expectStatus().isNoContent();
        String jeton = courriels.jeton(email);
        String nouveau = "un-tout-nouveau-mot-de-passe";
        session.post().uri(AUTH + "/reinitialisation").body(Map.of("jeton", jeton, "motDePasse", nouveau))
                .exchange().expectStatus().isNoContent();
        session.post().uri(AUTH + "/reinitialisation").body(Map.of("jeton", jeton, "motDePasse", nouveau))
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT)
                .expectBody().jsonPath("$.regle").isEqualTo("RG-AUT-08");

        session.post().uri(AUTH + "/connexion").body(Map.of("email", email, "motDePasse", nouveau))
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.etape").isEqualTo("CODE_REQUIS");
    }

    @Test
    @DisplayName("RG-AUT-06 : un navigateur de confiance se passe de code ; changer de mot de passe retire la confiance (RG-AUT-07)")
    void rg_aut_06_07_navigateur_de_confiance() {
        Navigateur navigateur = new Navigateur();
        RestTestClient session = anonyme(navigateur);
        session.post().uri(AUTH + "/connexion").body(Map.of("email", email, "motDePasse", MOT_DE_PASSE)).exchange()
                .expectStatus().isOk();
        session.post().uri(AUTH + "/code").body(Map.of("code", "000000", "faireConfiance", true)).exchange()
                .expectStatus().isOk();
        assertThat(navigateur.cookie("BNIA_CONFIANCE")).isPresent();
        session.post().uri(AUTH + "/deconnexion").exchange().expectStatus().isNoContent();

        RestTestClient retour = anonyme(navigateur);
        retour.post().uri(AUTH + "/connexion").body(Map.of("email", email, "motDePasse", MOT_DE_PASSE))
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.etape").isEqualTo("CONNECTE");

        String nouveau = "encore-un-autre-mot-de-passe";
        retour.put().uri(AUTH + "/mot-de-passe").body(Map.of("actuel", "faux-mot-de-passe", "nouveau", nouveau))
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT)
                .expectBody().jsonPath("$.regle").isEqualTo("RG-AUT-07");
        retour.put().uri(AUTH + "/mot-de-passe").body(Map.of("actuel", MOT_DE_PASSE, "nouveau", "court"))
                .exchange()
                .expectBody().jsonPath("$.regle").isEqualTo("RG-AUT-07");
        retour.put().uri(AUTH + "/mot-de-passe").body(Map.of("actuel", MOT_DE_PASSE, "nouveau", nouveau))
                .exchange().expectStatus().isNoContent();
        retour.post().uri(AUTH + "/deconnexion").exchange().expectStatus().isNoContent();

        anonyme(navigateur).post().uri(AUTH + "/connexion").body(Map.of("email", email, "motDePasse", nouveau))
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.etape").isEqualTo("CODE_REQUIS");
    }

    @Test
    @DisplayName("A-AUT-3 : sans jeton CSRF, une écriture est refusée ; sans session, l'API répond 401")
    void csrf_et_401() {
        brut.post().uri(AUTH + "/connexion").body(Map.of("email", email, "motDePasse", MOT_DE_PASSE))
                .exchange().expectStatus().isForbidden();
        brut.get().uri("/api/operateurs/{id}", operateur).exchange().expectStatus().isUnauthorized();
        anonyme().get().uri("/api/operateurs/{id}", operateur).exchange().expectStatus().isUnauthorized();
    }

    @Test
    @DisplayName("RG-AUT-12 et 13 : chacun lit ses connexions ; le journal complet est au personnel du BNIA")
    void rg_aut_12_13_journal() {
        anonyme().post().uri(AUTH + "/connexion").body(Map.of("email", email, "motDePasse", "mauvais-mot-de-passe"))
                .exchange();
        RestTestClient session = connecte(email);

        session.get().uri(AUTH + "/mes-connexions")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].evenement").isEqualTo("CONNEXION_REUSSIE")
                .jsonPath("$[?(@.evenement == 'ECHEC_MOT_DE_PASSE')].identifiantSaisi").isEqualTo(java.util.List.of(email));
        session.get().uri(AUTH + "/journal")
                .exchange()
                .expectStatus().isForbidden()
                .expectBody().jsonPath("$.regle").isEqualTo("RG-AUT-13");
        client.get().uri(u -> u.path(AUTH + "/journal").queryParam("utilisateur", utilisateur)
                        .queryParam("evenement", "ECHEC_MOT_DE_PASSE").build())
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.length()").isEqualTo(1).jsonPath("$[0].motDePasse").doesNotExist();
    }

    @Test
    @DisplayName("RG-AUT-15 : le premier mot de passe ne se pose qu'une fois, avec le jeton de l'invitation acceptée")
    void rg_aut_15_premier_mot_de_passe_une_fois() {
        String nouveau = emailUnique("paul");
        client.post().uri("/api/operateurs/{id}/invitations", operateur)
                .body(Map.of("email", nouveau, "role", "OBSERVATEUR", "nom", "Petit", "prenom", "Paul"))
                .exchange().expectStatus().isCreated();
        String jeton = courriels.jeton(nouveau);

        anonyme().post().uri(AUTH + "/premier-mot-de-passe").body(Map.of("jeton", jeton, "motDePasse", MOT_DE_PASSE))
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT)
                .expectBody().jsonPath("$.regle").isEqualTo("RG-AUT-15");
        anonyme().post().uri("/api/invitations/{jeton}/acceptation", jeton).body(Map.of("nom", "Petit", "prenom", "Paul"))
                .exchange().expectStatus().isOk();
        anonyme().post().uri(AUTH + "/premier-mot-de-passe").body(Map.of("jeton", jeton, "motDePasse", MOT_DE_PASSE))
                .exchange().expectStatus().isOk();
        anonyme().post().uri(AUTH + "/premier-mot-de-passe").body(Map.of("jeton", jeton, "motDePasse", MOT_DE_PASSE))
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT)
                .expectBody().jsonPath("$.regle").isEqualTo("RG-AUT-15");
    }
}
