package fr.bnia.messagerie;

import static org.assertj.core.api.Assertions.assertThat;

import fr.bnia.operateurs.domain.partage.Role;
import fr.bnia.support.AbstractIT;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.client.RestTestClient;

/** Lots 2 et 3 de la spec 15 : la conversation d'une DSM, de bout en bout, des deux côtés. */
class ConversationsObjetsIT extends AbstractIT {

    private long operateur;
    private RestTestClient agent;
    private RestTestClient observateur;
    private String conversation;
    private long dsm;

    @BeforeEach
    void negociantAvecDsm() {
        Map<String, Object> saisie = new HashMap<>();
        saisie.put("type", "NEGOCIANT");
        saisie.put("code", "M" + unique());
        saisie.put("raisonSociale", "Négoce " + unique());
        saisie.put("ville", "Condom");
        saisie.put("accordDiffusionGeneral", false);
        operateur = creer("/api/operateurs", saisie);
        creer("/api/operateurs/" + operateur + "/point-de-depart", Map.of("debut",
                LocalDate.now().withDayOfMonth(1).minusMonths(4), "lignes", List.of(
                        Map.of("cle", "NB", "denomination", "XO", "compte", "12", "volume", new BigDecimal("1500")))));
        String email = emailUnique("agent");
        membre(operateur, Role.AGENT, email);
        agent = connecte(email);
        String lecteur = emailUnique("observateur");
        membre(operateur, Role.OBSERVATEUR, lecteur);
        observateur = connecte(lecteur);
        dsm = ((Number) agent.post().uri("/api/operateurs/" + operateur + "/dsm").exchange().expectStatus().isCreated()
                .expectBody(Map.class).returnResult().getResponseBody().get("id")).longValue();
        conversation = "/api/messagerie/objets/DSM/" + dsm;
    }

    private Map<?, ?> ecrire(RestTestClient session, String texte) {
        return session.post().uri(conversation + "/messages").body(Map.of("texte", texte)).exchange()
                .expectStatus().isCreated().expectBody(Map.class).returnResult().getResponseBody();
    }

    @Test
    @DisplayName("RG-MSG-01, 03, 04 : la conversation naît au premier message ; chaque côté écrit ; l'observateur lit")
    void rg_msg_01_03_04_conversation_de_la_dsm() {
        agent.get().uri(conversation).exchange().expectStatus().isOk()
                .expectBody().jsonPath("$.id").doesNotExist()
                .jsonPath("$.objet.libelle").value(l -> assertThat((String) l).startsWith("DSM "))
                .jsonPath("$.peutEcrire").isEqualTo(true)
                .jsonPath("$.messages.length()").isEqualTo(0);

        assertThat(ecrire(agent, "Voici les DAE").get("cote")).isEqualTo("OPERATEUR");
        assertThat(ecrire(client, "Reçus, merci").get("cote")).isEqualTo("BNIA");

        observateur.get().uri(conversation).exchange().expectStatus().isOk()
                .expectBody().jsonPath("$.peutEcrire").isEqualTo(false)
                .jsonPath("$.lectureSeule").isNotEmpty()
                .jsonPath("$.operateur.id").isEqualTo(operateur)
                .jsonPath("$.messages[*].texte").isEqualTo(List.of("Voici les DAE", "Reçus, merci"));
        observateur.post().uri(conversation + "/messages").body(Map.of("texte", "Note")).exchange()
                .expectStatus().isForbidden()
                .expectBody().jsonPath("$.regle").isEqualTo("RG-MSG-01");
        connecte(emailUniqueSansRattachement()).get().uri(conversation).exchange().expectStatus().isForbidden();
        agent.get().uri("/api/messagerie/objets/DSM/999999999").exchange().expectStatus().isNotFound();
    }

    @Test
    @DisplayName("RG-MSG-05, 06 : un message vide est refusé ; l'auteur seul modifie ou supprime ; conversation vide supprimée")
    void rg_msg_05_06_ecrire_modifier_supprimer() {
        agent.post().uri(conversation + "/messages").body(Map.of("texte", "  ")).exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT)
                .expectBody().jsonPath("$.regle").isEqualTo("RG-MSG-05");
        Object id = ecrire(agent, "Les DAE arrivent demain").get("id");
        client.put().uri("/api/messagerie/messages/{id}", id).body(Map.of("texte", "Autre")).exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT)
                .expectBody().jsonPath("$.regle").isEqualTo("RG-MSG-06");
        agent.put().uri("/api/messagerie/messages/{id}", id).body(Map.of("texte", "Les DAE arrivent lundi")).exchange()
                .expectStatus().isOk().expectBody().jsonPath("$.modifieLe").isNotEmpty();
        agent.delete().uri("/api/messagerie/messages/{id}", id).exchange().expectStatus().isNoContent();
        agent.get().uri(conversation).exchange().expectStatus().isOk().expectBody().jsonPath("$.id").doesNotExist();
    }

    @Test
    @DisplayName("RG-MSG-07 : supprimer le brouillon supprime sa conversation")
    void rg_msg_07_suppression_avec_la_dsm() {
        ecrire(agent, "Brouillon à jeter");
        agent.delete().uri("/api/operateurs/" + operateur + "/dsm/{id}", dsm).exchange().expectStatus().isNoContent();
        agent.get().uri(conversation).exchange().expectStatus().isNotFound();
    }

    @Test
    @DisplayName("RG-MSG-09, 10 : non lu pour l'autre côté, lu à l'affichage pour tout ce côté ; marquer non lu")
    void rg_msg_09_10_lecture() {
        Object id = ecrire(client, "Merci de joindre le DAE du 12").get("id");
        observateur.get().uri(conversation).exchange().expectStatus().isOk()
                .expectBody().jsonPath("$.messages[0].nonLu").isEqualTo(true);
        agent.get().uri(conversation).exchange().expectStatus().isOk()
                .expectBody().jsonPath("$.messages[0].nonLu").isEqualTo(false);
        agent.post().uri("/api/messagerie/messages/{id}/non-lu", id).exchange().expectStatus().isNoContent();
        observateur.get().uri(conversation).exchange().expectStatus().isOk()
                .expectBody().jsonPath("$.messages[0].nonLu").isEqualTo(true);
        client.get().uri(conversation).exchange().expectStatus().isOk()
                .expectBody().jsonPath("$.messages[0].nonLu").isEqualTo(false);
    }

    /** Un utilisateur connecté, rattaché à un autre opérateur seulement. */
    private String emailUniqueSansRattachement() {
        String email = emailUnique("autre");
        membre(nouvelOperateur(), Role.ADMIN, email);
        return email;
    }
}
