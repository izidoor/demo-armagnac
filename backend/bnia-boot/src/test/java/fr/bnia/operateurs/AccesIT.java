package fr.bnia.operateurs;

import static org.assertj.core.api.Assertions.assertThat;

import fr.bnia.operateurs.domain.partage.Role;
import fr.bnia.support.AbstractIT;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.client.RestTestClient;

/** Module Accès : invitations, acceptation par le lien de l'e-mail, annuaire, membres, dernier ADMIN. */
class AccesIT extends AbstractIT {

    private static final String OPERATEURS = "/api/operateurs";

    private long operateur;
    private String base;

    @BeforeEach
    void operateur() {
        operateur = nouvelOperateur();
        base = OPERATEURS + "/" + operateur;
    }

    @Test
    @DisplayName("RG-OPE-40, 42 et RG-AUT-11, 15 : une personne nouvelle accepte par le lien, pose son mot de passe, et la session s'ouvre")
    void rg_ope_40_42_personne_nouvelle() {
        String email = emailUnique("marc");
        client.post().uri(base + "/invitations")
                .body(Map.of("email", email, "role", "AGENT", "nom", "Lafitte", "prenom", "Marc", "fonction", "Caviste"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody().jsonPath("$.statut").isEqualTo("EN_ATTENTE").jsonPath("$.jeton").doesNotExist();
        client.post().uri(base + "/invitations").body(Map.of("email", email, "role", "AGENT", "nom", "Lafitte", "prenom", "M"))
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT)
                .expectBody().jsonPath("$.regle").isEqualTo("RG-OPE-40");

        String jeton = courriels.jeton(email);
        RestTestClient navigateur = anonyme();
        navigateur.get().uri("/api/invitations/{jeton}", jeton)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.role").isEqualTo("AGENT")
                .jsonPath("$.nom").isEqualTo("Lafitte")
                .jsonPath("$.compteExistant").isEqualTo(false);
        navigateur.post().uri("/api/invitations/{jeton}/acceptation", jeton).body(Map.of("nom", "Lafitte", "prenom", "Marc"))
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.operateur").isEqualTo(operateur).jsonPath("$.nouveauCompte").isEqualTo(true);
        navigateur.post().uri("/api/authentification/premier-mot-de-passe")
                .body(Map.of("jeton", jeton, "motDePasse", "court"))
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT)
                .expectBody().jsonPath("$.regle").isEqualTo("RG-AUT-07");
        navigateur.post().uri("/api/authentification/premier-mot-de-passe")
                .body(Map.of("jeton", jeton, "motDePasse", MOT_DE_PASSE))
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.etape").isEqualTo("CONNECTE");

        navigateur.get().uri("/api/moi")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.email").isEqualTo(email)
                .jsonPath("$.operateurOuvert").isEqualTo(operateur);
        navigateur.get().uri(base + "/mes-droits")
                .exchange()
                .expectBody().jsonPath("$.role").isEqualTo("AGENT").jsonPath("$.acces").isEqualTo("LECTURE");
        navigateur.get().uri("/api/invitations/{jeton}", jeton).exchange().expectStatus().is4xxClientError();
        connecte(email);
    }

    @Test
    @DisplayName("RG-OPE-42 : un compte existant se connecte pour accepter ; l'invitation d'un contact en fait un membre, même fiche")
    void rg_ope_30_42_compte_existant() {
        String email = emailUnique("jeanne");
        long jeanne = membre(nouvelOperateur(), Role.ADMIN, email);
        long contact = creer(base + "/contacts", Map.of("nom", "Durand", "prenom", "Jeanne"));
        long fiche = ((Number) ((Map<?, ?>) client.get().uri(base + "/contacts/{id}", contact).exchange()
                .expectBody(Map.class).returnResult().getResponseBody().get("fiche")).get("id")).longValue();

        client.post().uri(base + "/invitations").body(Map.of("contact", contact, "email", email, "role", "OBSERVATEUR"))
                .exchange().expectStatus().isCreated();
        String jeton = courriels.jeton(email);

        anonyme().get().uri("/api/invitations/{jeton}", jeton)
                .exchange().expectStatus().isOk().expectBody().jsonPath("$.compteExistant").isEqualTo(true);
        anonyme().post().uri("/api/invitations/{jeton}/acceptation", jeton).body(Map.of())
                .exchange()
                .expectStatus().isForbidden()
                .expectBody().jsonPath("$.regle").isEqualTo("RG-OPE-42");
        connecte(email).post().uri("/api/invitations/{jeton}/acceptation", jeton).body(Map.of())
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.nouveauCompte").isEqualTo(false);

        client.get().uri(base + "/contacts/{id}", contact).exchange().expectStatus().isNotFound();
        client.get().uri(base + "/membres/{id}", rattachement(operateur, jeanne))
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.role").isEqualTo("OBSERVATEUR").jsonPath("$.fiche.id").isEqualTo(fiche)
                .jsonPath("$.fiche.type").isEqualTo("RATTACHEMENT");
    }

    @Test
    @DisplayName("RG-OPE-41 : relancer remplace le lien ; annuler ferme l'invitation")
    void rg_ope_41_relance_et_annulation() {
        String email = emailUnique("paul");
        long invitation = creer(base + "/invitations", Map.of("email", email, "role", "AGENT", "nom", "Petit", "prenom", "Paul"));
        String ancien = courriels.jeton(email);

        client.post().uri(base + "/invitations/{id}/relance", invitation).exchange().expectStatus().isOk();
        String nouveau = courriels.jeton(email);
        assertThat(nouveau).isNotEqualTo(ancien);
        anonyme().get().uri("/api/invitations/{jeton}", ancien).exchange().expectStatus().is4xxClientError();
        anonyme().get().uri("/api/invitations/{jeton}", nouveau).exchange().expectStatus().isOk();

        client.post().uri(base + "/invitations/{id}/annulation", invitation)
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.statut").isEqualTo("ANNULEE");
        anonyme().post().uri("/api/invitations/{jeton}/acceptation", nouveau).body(Map.of("nom", "Petit", "prenom", "Paul"))
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT)
                .expectBody().jsonPath("$.regle").isEqualTo("RG-OPE-42");
        client.post().uri(base + "/invitations/{id}/relance", invitation)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT)
                .expectBody().jsonPath("$.regle").isEqualTo("RG-OPE-41");
    }

    @Test
    @DisplayName("RG-OPE-52 : l'annuaire liste contacts, membres et personnes nouvelles invitées, avec leurs mentions")
    void rg_ope_52_annuaire() {
        creer(base + "/contacts", Map.of("nom", "Durand", "prenom", "Paul"));
        String email = emailUnique("sophie");
        long sophie = membre(operateur, Role.AGENT, email);
        client.post().uri(base + "/membres/{id}/suspension", rattachement(operateur, sophie)).exchange().expectStatus().isOk();
        creer(base + "/invitations", Map.of("email", emailUnique("marc"), "role", "OBSERVATEUR", "nom", "Lafitte", "prenom", "Marc"));

        client.get().uri(base + "/annuaire")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(3)
                .jsonPath("$[?(@.genre == 'CONTACT')].nomComplet").isEqualTo(List.of("Paul Durand"))
                .jsonPath("$[?(@.genre == 'MEMBRE')].statut").isEqualTo(List.of("SUSPENDU"))
                .jsonPath("$[?(@.genre == 'MEMBRE')].email").isEqualTo(List.of(email))
                .jsonPath("$[?(@.genre == 'INVITATION')].role").isEqualTo(List.of("OBSERVATEUR"));
    }

    @Test
    @DisplayName("RG-OPE-35 et 43 : le dernier ADMIN ne se retire, ne se suspend ni ne se rétrograde ; un ADMIN du BNIA le peut")
    void rg_ope_35_43_dernier_admin() {
        String emailJeanne = emailUnique("jeanne");
        long jeanne = rattachement(operateur, membre(operateur, Role.ADMIN, emailJeanne));
        String emailSophie = emailUnique("sophie");
        long sophie = rattachement(operateur, membre(operateur, Role.AGENT, emailSophie));
        RestTestClient session = connecte(emailJeanne);

        session.put().uri(base + "/membres/{id}/role", jeanne).body(Map.of("role", "AGENT"))
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT)
                .expectBody().jsonPath("$.regle").isEqualTo("RG-OPE-35");
        session.post().uri(base + "/membres/{id}/suspension", jeanne)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT)
                .expectBody().jsonPath("$.regle").isEqualTo("RG-OPE-35");
        session.get().uri(base + "/membres/{id}", jeanne)
                .exchange().expectBody().jsonPath("$.dernierAdmin").isEqualTo(true);

        session.put().uri(base + "/membres/{id}/role", sophie).body(Map.of("role", "ADMIN"))
                .exchange().expectStatus().isOk().expectBody().jsonPath("$.role").isEqualTo("ADMIN");
        session.put().uri(base + "/membres/{id}/role", jeanne).body(Map.of("role", "OBSERVATEUR"))
                .exchange().expectStatus().isOk();
        session.put().uri(base + "/membres/{id}/role", sophie).body(Map.of("role", "AGENT"))
                .exchange()
                .expectStatus().isForbidden()
                .expectBody().jsonPath("$.regle").isEqualTo("RG-OPE-31");

        client.put().uri(base + "/membres/{id}/role", sophie).body(Map.of("role", "AGENT"))
                .exchange().expectStatus().isOk();
        client.get().uri(base + "/journal")
                .exchange()
                .expectBody().jsonPath("$[0].action").isEqualTo("ROLE_CHANGE");
    }

    @Test
    @DisplayName("RG-OPE-35 : deux ADMIN qui se rétrogradent l'un l'autre en même temps : l'opérateur garde un ADMIN")
    void rg_ope_35_concurrence() throws Exception {
        String emailA = emailUnique("admin-a");
        long a = rattachement(operateur, membre(operateur, Role.ADMIN, emailA));
        String emailB = emailUnique("admin-b");
        long b = rattachement(operateur, membre(operateur, Role.ADMIN, emailB));
        RestTestClient sessionA = connecte(emailA);
        RestTestClient sessionB = connecte(emailB);

        CountDownLatch depart = new CountDownLatch(1);
        List<Callable<Integer>> taches = List.of(
                () -> retrograder(sessionA, b, depart),
                () -> retrograder(sessionB, a, depart));
        ExecutorService executeur = Executors.newFixedThreadPool(2);
        try {
            List<Future<Integer>> resultats = new ArrayList<>();
            for (Callable<Integer> tache : taches) {
                resultats.add(executeur.submit(tache));
            }
            depart.countDown();
            List<Integer> statuts = new ArrayList<>();
            for (Future<Integer> resultat : resultats) {
                statuts.add(resultat.get());
            }
            // Le second, rétrogradé entre-temps, n'a plus le droit (403), ou se heurte au dernier ADMIN (422).
            assertThat(statuts).containsOnlyOnce(200).anyMatch(s -> s == 403 || s == 422);
        } finally {
            executeur.shutdownNow();
        }

        List<?> admins = client.get().uri(base + "/annuaire").exchange().expectBody(List.class).returnResult()
                .getResponseBody().stream().filter(p -> "ADMIN".equals(((Map<?, ?>) p).get("role"))).toList();
        assertThat(admins).hasSize(1);
    }

    @Test
    @DisplayName("RG-OPE-44, RG-OPE-25 et RG-AUT-11 : retirer le dernier accès en gardant le contact supprime l'utilisateur et son compte")
    void rg_ope_44_retrait() {
        String email = emailUnique("pierre");
        long pierre = membre(operateur, Role.OBSERVATEUR, email);

        client.post().uri(base + "/membres/{id}/retrait", rattachement(operateur, pierre)).body(Map.of("conserverContact", true))
                .exchange().expectStatus().isNoContent();

        client.get().uri(base + "/annuaire")
                .exchange()
                .expectBody()
                .jsonPath("$[0].genre").isEqualTo("CONTACT")
                .jsonPath("$[0].email").isEqualTo(email);
        assertThat(operateurs.utilisateurParEmail(email)).isEmpty();
        anonyme().post().uri("/api/authentification/connexion").body(Map.of("email", email, "motDePasse", MOT_DE_PASSE))
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT)
                .expectBody().jsonPath("$.regle").isEqualTo("RG-AUT-02");
    }

    @Test
    @DisplayName("RG-OPE-43 et RG-AUT-09 : suspendu dans son seul opérateur, le membre perd sa session à la requête suivante")
    void rg_ope_43_suspension_coupe_la_session() {
        String email = emailUnique("sophie");
        long sophie = membre(operateur, Role.AGENT, email);
        RestTestClient session = connecte(email);
        session.get().uri("/api/moi").exchange().expectStatus().isOk();

        client.post().uri(base + "/membres/{id}/suspension", rattachement(operateur, sophie))
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.statut").isEqualTo("SUSPENDU");

        session.get().uri("/api/moi").exchange().expectStatus().isUnauthorized();
        anonyme().post().uri("/api/authentification/connexion").body(Map.of("email", email, "motDePasse", MOT_DE_PASSE))
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT)
                .expectBody().jsonPath("$.regle").isEqualTo("RG-AUT-04");
    }

    @Test
    @DisplayName("RG-OPE-31 et 32 : le Paramétrage et le listing des opérateurs sont refusés à un ADMIN d'un opérateur")
    void rg_ope_31_32_reserve_au_bnia() {
        String email = emailUnique("jeanne");
        membre(operateur, Role.ADMIN, email);
        RestTestClient session = connecte(email);

        session.post().uri("/api/referentiels/centres-interet").body(Map.of("code", "X" + unique(), "libelle", "Refusé " + unique()))
                .exchange()
                .expectStatus().isForbidden()
                .expectBody().jsonPath("$.regle").isEqualTo("RG-OPE-31");
        session.get().uri(OPERATEURS).exchange()
                .expectStatus().isForbidden()
                .expectBody().jsonPath("$.regle").isEqualTo("RG-OPE-32");
        session.get().uri(base + "/mes-droits")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.contacts").isEqualTo("GESTION")
                .jsonPath("$.acces").isEqualTo("GESTION")
                .jsonPath("$.parametrage").isEqualTo("AUCUN")
                .jsonPath("$.personnelBnia").isEqualTo(false);
    }

    private int retrograder(RestTestClient session, long membre, CountDownLatch depart) throws InterruptedException {
        depart.await();
        return session.put().uri(base + "/membres/{m}/role", membre).body(Map.of("role", "AGENT"))
                .exchange().returnResult(String.class).getStatus().value();
    }
}
