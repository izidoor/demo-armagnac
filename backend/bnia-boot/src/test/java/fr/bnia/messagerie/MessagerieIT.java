package fr.bnia.messagerie;

import static org.assertj.core.api.Assertions.assertThat;

import fr.bnia.operateurs.domain.partage.Role;
import fr.bnia.support.AbstractIT;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * Lots 3 à 5 de la spec 15 : conversations libres et leurs pièces jointes, nouveaux messages des deux tableaux de
 * bord, « Mes conversations » et la recherche.
 */
class MessagerieIT extends AbstractIT {

    private static final byte[] PDF = "%PDF-1.7 attestation".getBytes(StandardCharsets.US_ASCII);
    private static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 1, 2};

    private long operateur;
    private RestTestClient agent;
    private RestTestClient admin;
    private String portail;

    @BeforeEach
    void negociant() {
        Map<String, Object> saisie = new HashMap<>();
        saisie.put("type", "NEGOCIANT");
        saisie.put("code", "L" + unique());
        saisie.put("raisonSociale", "Négoce " + unique());
        saisie.put("ville", "Condom");
        saisie.put("accordDiffusionGeneral", false);
        operateur = creer("/api/operateurs", saisie);
        String email = emailUnique("agent");
        membre(operateur, Role.AGENT, email);
        agent = connecte(email);
        String emailAdmin = emailUnique("admin");
        membre(operateur, Role.ADMIN, emailAdmin);
        admin = connecte(emailAdmin);
        portail = "/api/operateurs/" + operateur + "/messagerie";
    }

    private static HttpEntity<ByteArrayResource> fichier(String nom, byte[] contenu) {
        HttpHeaders entete = new HttpHeaders();
        entete.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        return new HttpEntity<>(new ByteArrayResource(contenu) {
            @Override
            public String getFilename() {
                return nom;
            }
        }, entete);
    }

    private RestTestClient.ResponseSpec ouvrir(RestTestClient session, String titre, String texte,
            HttpEntity<?>... pieces) {
        MultiValueMap<String, Object> corps = new LinkedMultiValueMap<>();
        corps.add("titre", titre);
        corps.add("texte", texte);
        for (HttpEntity<?> piece : pieces) {
            corps.add("pieces", piece);
        }
        return session.post().uri(portail + "/conversations").contentType(MediaType.MULTIPART_FORM_DATA).body(corps)
                .exchange();
    }

    private Map<?, ?> corps(RestTestClient.ResponseSpec reponse) {
        return reponse.expectBody(Map.class).returnResult().getResponseBody();
    }

    private RestTestClient.ResponseSpec repondre(RestTestClient session, Object conversation, String texte) {
        MultiValueMap<String, Object> corps = new LinkedMultiValueMap<>();
        corps.add("texte", texte);
        return session.post().uri("/api/messagerie/conversations/{id}/messages", conversation)
                .contentType(MediaType.MULTIPART_FORM_DATA).body(corps).exchange();
    }

    @Test
    @DisplayName("RG-MSG-12, RG-OPE-31 : l'observateur lit la Messagerie sans la gérer ; il n'ouvre pas de conversation libre")
    void rg_msg_12_observateur() {
        String email = emailUnique("observateur");
        membre(operateur, Role.OBSERVATEUR, email);
        RestTestClient observateur = connecte(email);

        observateur.get().uri("/api/operateurs/{id}/mes-droits", operateur).exchange().expectStatus().isOk()
                .expectBody().jsonPath("$.messagerie").isEqualTo("LECTURE");
        agent.get().uri("/api/operateurs/{id}/mes-droits", operateur).exchange().expectStatus().isOk()
                .expectBody().jsonPath("$.messagerie").isEqualTo("GESTION");
        ouvrir(observateur, "Accès aux statistiques", "Mon rôle le permet-il ?").expectStatus().isForbidden()
                .expectBody().jsonPath("$.regle").isEqualTo("RG-MSG-12");
    }

    @Test
    @DisplayName("RG-MSG-02, 12, 14 : l'agent ouvre une conversation libre avec ses pièces ; son ADMIN ne la voit pas ; le BNIA répond")
    void rg_msg_02_12_14_conversation_libre() {
        Map<?, ?> ouverte = corps(ouvrir(agent, "Import de la matrice refusé", "Voici ma matrice ; où est l'erreur ?",
                fichier("attestation.pdf", PDF), fichier("ecran.png", PNG)).expectStatus().isCreated());
        Object id = ouverte.get("id");
        assertThat(ouverte.get("titre")).isEqualTo("Import de la matrice refusé");
        List<?> pieces = (List<?>) ((Map<?, ?>) ((List<?>) ouverte.get("messages")).getFirst()).get("pieces");
        assertThat(pieces.stream().<Object>map(p -> ((Map<?, ?>) p).get("type")).toList())
                .containsExactly("application/pdf", "image/png");
        Object cle = ((Map<?, ?>) pieces.getFirst()).get("cle");

        admin.get().uri("/api/messagerie/conversations/{id}", id).exchange().expectStatus().isForbidden()
                .expectBody().jsonPath("$.regle").isEqualTo("RG-MSG-02");
        admin.get().uri("/api/messagerie/conversations/{id}/pieces/{cle}", id, cle).exchange().expectStatus()
                .isForbidden();
        client.get().uri("/api/messagerie/conversations/{id}/pieces/{cle}", id, cle).exchange().expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_PDF)
                .expectBody(byte[].class).isEqualTo(PDF);
        repondre(client, id, "Bonjour, la colonne 15 est grisée : laissez-la vide.").expectStatus().isCreated()
                .expectBody().jsonPath("$.cote").isEqualTo("BNIA")
                .jsonPath("$.texte").isEqualTo("Bonjour, la colonne 15 est grisée : laissez-la vide.");

        client.post().uri(portail + "/conversations").contentType(MediaType.MULTIPART_FORM_DATA)
                .body(new LinkedMultiValueMap<>(Map.of("titre", List.of("BNIA"), "texte", List.of("Bonjour"))))
                .exchange().expectStatus().isForbidden();
        ouvrir(agent, " ", "Texte").expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT)
                .expectBody().jsonPath("$.regle").isEqualTo("RG-MSG-12");
        ouvrir(agent, "Exécutable", "Texte", fichier("outil.exe", new byte[] {'M', 'Z', 0, 0}))
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT);
    }

    @Test
    @DisplayName("RG-MSG-13 : l'auteur retiré, la conversation passe en lecture seule pour le BNIA")
    void rg_msg_13_auteur_parti() {
        String email = emailUnique("partant");
        long partant = membre(operateur, Role.AGENT, email);
        Object id = corps(ouvrir(connecte(email), "Départ", "Bonjour").expectStatus().isCreated()).get("id");
        client.post().uri("/api/operateurs/{o}/membres/{r}/suspension", operateur, rattachement(operateur, partant))
                .exchange().expectStatus().isOk();
        client.get().uri("/api/messagerie/conversations/{id}", id).exchange().expectStatus().isOk()
                .expectBody().jsonPath("$.auteurParti").isEqualTo(true)
                .jsonPath("$.peutEcrire").isEqualTo(false);
        repondre(client, id, "Réponse").expectStatus().isForbidden()
                .expectBody().jsonPath("$.regle").isEqualTo("RG-MSG-13");
    }

    @Test
    @DisplayName("RG-MSG-11 : le tableau de bord compte les non lus de mon côté, sans les conversations libres d'un autre")
    void rg_msg_11_nouveaux_messages() {
        Object deLAgent = corps(ouvrir(agent, "Question de l'agent", "Bonjour").expectStatus().isCreated()).get("id");
        Object deLAdmin = corps(ouvrir(admin, "Question de l'admin", "Bonjour").expectStatus().isCreated()).get("id");
        repondre(client, deLAgent, "Réponse à l'agent\nDeuxième ligne").expectStatus().isCreated();
        repondre(client, deLAdmin, "Réponse à l'admin").expectStatus().isCreated();

        agent.get().uri(portail + "/nouveaux").exchange().expectStatus().isOk()
                .expectBody().jsonPath("$.nombre").isEqualTo(1)
                .jsonPath("$.messages[0].intitule").isEqualTo("Question de l'agent")
                .jsonPath("$.messages[0].debut").isEqualTo("Réponse à l'agent…")
                .jsonPath("$.messages[0].auteur").isNotEmpty();
        admin.get().uri(portail + "/nouveaux").exchange().expectStatus().isOk()
                .expectBody().jsonPath("$.nombre").isEqualTo(1);
        Map<?, ?> duBnia = corps(client.get().uri("/api/messagerie/nouveaux").exchange().expectStatus().isOk());
        assertThat(((List<?>) duBnia.get("messages")).stream().<Object>map(m -> ((Map<?, ?>) m).get("conversation"))
                .toList())
                .contains(deLAgent, deLAdmin);

        agent.get().uri("/api/messagerie/conversations/{id}", deLAgent).exchange().expectStatus().isOk();
        agent.get().uri(portail + "/nouveaux").exchange().expectStatus().isOk()
                .expectBody().jsonPath("$.nombre").isEqualTo(0);
        agent.get().uri("/api/messagerie/nouveaux").exchange().expectStatus().isForbidden();
        client.get().uri(portail + "/nouveaux").exchange().expectStatus().isForbidden();
    }

    @Test
    @DisplayName("RG-MSG-15, RG-MSG-16 : mes conversations, filtrées, avec leurs non lus ; la recherche ignore casse et accents")
    void rg_msg_15_16_conversations_et_recherche() {
        Object debit = corps(ouvrir(agent, "Débit de la cotisation", "Le prélèvement a été fait deux fois")
                .expectStatus().isCreated()).get("id");
        ouvrir(agent, "Autre sujet", "Rien à voir").expectStatus().isCreated();
        repondre(client, debit, "Nous remboursons le second DÉBIT").expectStatus().isCreated();

        agent.get().uri(portail + "/conversations").exchange().expectStatus().isOk()
                .expectBody().jsonPath("$.length()").isEqualTo(2)
                .jsonPath("$[0].id").isEqualTo(debit)
                .jsonPath("$[0].nonLus").isEqualTo(1)
                .jsonPath("$[0].messages").isEqualTo(2);
        agent.get().uri(u -> u.path(portail + "/conversations").queryParam("nonLues", true).build()).exchange()
                .expectStatus().isOk().expectBody().jsonPath("$[*].id").isEqualTo(List.of(debit));
        agent.get().uri(u -> u.path(portail + "/conversations").queryParam("recherche", "debit").build()).exchange()
                .expectStatus().isOk().expectBody().jsonPath("$[*].id").isEqualTo(List.of(debit))
                .jsonPath("$[0].extrait").isEqualTo("Nous remboursons le second DÉBIT");
        agent.get().uri(u -> u.path(portail + "/conversations").queryParam("recherche", "prelevement").build())
                .exchange().expectStatus().isOk().expectBody()
                .jsonPath("$[0].extrait").isEqualTo("Le prélèvement a été fait deux fois");
        agent.get().uri(u -> u.path(portail + "/conversations").queryParam("recherche", "100%").build()).exchange()
                .expectStatus().isOk().expectBody().jsonPath("$.length()").isEqualTo(0);
        admin.get().uri(portail + "/conversations").exchange().expectStatus().isOk()
                .expectBody().jsonPath("$.length()").isEqualTo(0);

        client.get().uri(u -> u.path("/api/messagerie/conversations").queryParam("operateur", operateur)
                        .queryParam("libres", true).build()).exchange().expectStatus().isOk()
                .expectBody().jsonPath("$.length()").isEqualTo(2)
                .jsonPath("$[0].operateur.id").isEqualTo(operateur)
                // Le BNIA a répondu sans afficher la conversation : le message de l'agent reste non lu pour lui.
                .jsonPath("$[0].nonLus").isEqualTo(1);
        client.get().uri(u -> u.path("/api/messagerie/conversations").queryParam("operateur", operateur)
                        .queryParam("type", "DSM").build()).exchange().expectStatus().isOk()
                .expectBody().jsonPath("$.length()").isEqualTo(0);
    }
}
