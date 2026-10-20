package fr.bnia.support;

import static org.assertj.core.api.Assertions.assertThat;

import fr.bnia.authentification.application.Contexte;
import fr.bnia.authentification.application.GererMotDePasse;
import fr.bnia.operateurs.application.GererInvitations;
import fr.bnia.operateurs.application.RattachementRepository;
import fr.bnia.operateurs.application.SaisieAcceptation;
import fr.bnia.operateurs.application.SaisieInvitation;
import fr.bnia.operateurs.application.partage.OperateursLecture;
import fr.bnia.operateurs.domain.Invitation;
import fr.bnia.operateurs.domain.partage.Acteur;
import fr.bnia.operateurs.domain.partage.Role;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.client.RestTestClient;

/**
 * Base des tests d'intégration : application complète sur un port aléatoire, base Testcontainers.
 * Toutes les sous-classes partagent le même contexte Spring, donc le même conteneur.
 *
 * <p>Toute l'API exige une session (ADR-015) : {@link #client} est connecté comme ADMIN du BNIA, qui peut
 * tout ; {@link #connecte(String)} ouvre la session d'un autre utilisateur, créé par {@link #membre}.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"bnia.authentification.code-fixe=000000", "bnia.declarations.detection-retards=-"})
@AutoConfigureRestTestClient
@Import(TestcontainersConfiguration.class)
public abstract class AbstractIT {

    /** Mot de passe de tous les comptes des tests. */
    protected static final String MOT_DE_PASSE = "mot-de-passe-des-tests";
    /** ADMIN du BNIA des tests. */
    protected static final String ADMIN_BNIA = "admin.it@bnia.example";

    /** Une session d'ADMIN du BNIA par contexte Spring (donc par serveur). */
    private static final Map<RestTestClient, RestTestClient> SESSIONS_ADMIN = new java.util.IdentityHashMap<>();

    @Autowired
    private RestTestClient brut;
    @Autowired
    protected CourrielsCaptures courriels;
    @Autowired
    private GererInvitations invitations;
    @Autowired
    private GererMotDePasse motsDePasse;
    @Autowired
    private RattachementRepository rattachements;
    @Autowired
    protected OperateursLecture operateurs;

    /** Connecté comme ADMIN du BNIA. */
    protected RestTestClient client;

    @BeforeEach
    void connecterAdminBnia() {
        synchronized (SESSIONS_ADMIN) {
            client = SESSIONS_ADMIN.computeIfAbsent(brut, b -> {
                membre(operateurs.operateurBnia(), Role.ADMIN, ADMIN_BNIA);
                return connecte(ADMIN_BNIA);
            });
        }
    }

    /**
     * Oublie les sessions d'ADMIN du BNIA, après une recréation de la base qui les a effacées (avec le
     * compte) : le prochain test reconnecte un ADMIN.
     */
    protected static void oublierLesSessions() {
        synchronized (SESSIONS_ADMIN) {
            SESSIONS_ADMIN.clear();
        }
    }

    /** Un client sans session, avec ses propres cookies. */
    protected RestTestClient anonyme() {
        return anonyme(new Navigateur());
    }

    protected RestTestClient anonyme(Navigateur navigateur) {
        RestTestClient nouveau = brut.mutate().requestInterceptor(navigateur).build();
        nouveau.get().uri("/api/authentification/session").exchange().expectStatus().isOk();
        return nouveau;
    }

    /**
     * Ouvre une session : e-mail, mot de passe, puis, si la double authentification est activée (RG-AUT-16),
     * le code, qui vaut 000000 dans les tests (RG-AUT-01).
     */
    protected RestTestClient connecte(String email) {
        RestTestClient session = anonyme();
        String etape = session.post().uri("/api/authentification/connexion")
                .body(Map.of("email", email, "motDePasse", MOT_DE_PASSE))
                .exchange().expectStatus().isOk()
                .expectBody(Map.class).returnResult().getResponseBody().get("etape").toString();
        if (etape.equals("CODE_REQUIS")) {
            session.post().uri("/api/authentification/code").body(Map.of("code", "000000", "faireConfiance", false))
                    .exchange().expectStatus().isOk();
        }
        return session;
    }

    /**
     * Un membre de l'opérateur, avec un compte et le mot de passe {@link #MOT_DE_PASSE}, par les cas d'usage :
     * invitation par l'application, acceptation, premier mot de passe. Déjà membre : rien ne change.
     *
     * @return l'identifiant de l'utilisateur
     */
    protected long membre(long operateur, Role role, String email) {
        Optional<Long> existant = operateurs.utilisateurParEmail(email);
        if (existant.isPresent() && rattachements.de(operateur, existant.get()).isPresent()) {
            return existant.get();
        }
        String local = email.substring(0, email.indexOf('@'));
        var saisie = SaisieInvitation.personne(email, role, nom(local), "Test");
        Invitation.Emise emise = invitations.emettre(null, operateur, saisie);
        if (existant.isPresent()) {
            invitations.accepter(emise.jeton(), Optional.of(new Acteur(existant.get())), null);
            return existant.get();
        }
        long utilisateur = invitations.accepter(emise.jeton(), Optional.empty(),
                new SaisieAcceptation(null, nom(local), "Test")).utilisateur();
        motsDePasse.definirPremierMotDePasse(emise.jeton(), MOT_DE_PASSE, Contexte.inconnu());
        assertThat(utilisateur).isPositive();
        return utilisateur;
    }

    /** Identifiant du rattachement d'un membre, celui des URL {@code /membres/{membre}}. */
    protected long rattachement(long operateur, long utilisateur) {
        return rattachements.de(operateur, utilisateur).orElseThrow().id();
    }

    /** Un producteur au code et à la raison sociale uniques, créé par l'ADMIN du BNIA. */
    protected long nouvelOperateur() {
        var saisie = new java.util.HashMap<String, Object>();
        saisie.put("type", "PRODUCTEUR");
        saisie.put("code", "T" + unique());
        saisie.put("raisonSociale", "Maison " + unique());
        saisie.put("ville", "Condom");
        saisie.put("accordDiffusionGeneral", false);
        return creer("/api/operateurs", saisie);
    }

    /** Huit caractères aléatoires, en majuscules. */
    protected static String unique() {
        return UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
    }

    /** Un e-mail jamais utilisé. */
    protected static String emailUnique(String prefixe) {
        return (prefixe + "." + UUID.randomUUID().toString().substring(0, 8) + "@exemple.fr").toLowerCase(Locale.ROOT);
    }

    /** Crée une ressource par {@code POST}, attend un 201 et renvoie son identifiant. */
    protected long creer(String uri, Map<String, ?> corps) {
        return creer(client, uri, corps);
    }

    protected long creer(RestTestClient session, String uri, Map<String, ?> corps) {
        Map<?, ?> reponse = session.post().uri(uri).body(corps)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Map.class)
                .returnResult()
                .getResponseBody();
        return ((Number) reponse.get("id")).longValue();
    }

    private static String nom(String local) {
        String nom = local.replaceAll("[^A-Za-z]", " ").strip();
        return nom.isEmpty() ? "Test" : Character.toUpperCase(nom.charAt(0)) + nom.substring(1);
    }
}
