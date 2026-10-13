package fr.bnia.declarations;

import static org.assertj.core.api.Assertions.assertThat;

import fr.bnia.operateurs.domain.partage.Role;
import fr.bnia.support.AbstractIT;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.client.RestTestClient;

/**
 * Lot 2 de la spec 05 : envoi, instruction, validation, commentaires, listing du BNIA, DSM suivante. Les
 * événements publiés sont vérifiés par les tests des cas d'usage (le serveur des IT tourne sur ses fils).
 */
class InstructionDsmIT extends AbstractIT {

    /** Le premier du mois, quatre mois avant le mois courant : la période suivante est déjà commencée. */
    private static final LocalDate DEBUT = LocalDate.now().withDayOfMonth(1).minusMonths(4);

    private long operateur;
    private RestTestClient agent;
    private String dsms;

    /** Un négociant, son point de départ, un agent connecté ; renvoie la DSM initiée. */
    private Map<?, ?> negociantAvecDsm() {
        Map<String, Object> saisie = new HashMap<>();
        saisie.put("type", "NEGOCIANT");
        saisie.put("code", "T" + unique());
        saisie.put("raisonSociale", "Négoce " + unique());
        saisie.put("ville", "Condom");
        saisie.put("accordDiffusionGeneral", false);
        operateur = creer("/api/operateurs", saisie);
        creer("/api/operateurs/" + operateur + "/point-de-depart", Map.of("debut", DEBUT, "lignes", List.of(
                Map.of("cle", "NB", "denomination", "XO", "compte", "12", "volume", new BigDecimal("1500")),
                Map.of("cle", "MB", "millesime", 2010, "volume", new BigDecimal("640.25")))));
        String email = emailUnique("agent");
        membre(operateur, Role.AGENT, email);
        agent = connecte(email);
        dsms = "/api/operateurs/" + operateur + "/dsm";
        return corps(agent.post().uri(dsms).exchange().expectStatus().isCreated());
    }

    private static Map<?, ?> corps(RestTestClient.ResponseSpec reponse) {
        return reponse.expectBody(Map.class).returnResult().getResponseBody();
    }

    private static Object id(Map<?, ?> objet) {
        return objet.get("id");
    }

    /** La saisie qui reprend la DSM, la première ligne recevant les volumes donnés (colonne → litres). */
    @SuppressWarnings("unchecked")
    private static Map<String, Object> saisie(Map<?, ?> dsm, Map<Integer, String> premiereLigne) {
        List<Map<String, Object>> lignes = new ArrayList<>();
        for (Map<String, Object> ligne : (List<Map<String, Object>>) dsm.get("lignes")) {
            List<Object> volumes = new ArrayList<>((List<Object>) ligne.get("volumes"));
            volumes.set(0, null);
            volumes.set(25, null);
            if (lignes.isEmpty()) {
                premiereLigne.forEach((colonne, volume) -> volumes.set(colonne - 1, new BigDecimal(volume)));
            }
            Map<String, Object> s = new HashMap<>();
            s.put("id", ligne.get("id"));
            s.put("cle", ligne.get("cle"));
            s.put("millesime", ligne.get("millesime"));
            s.put("denomination", ligne.get("denomination"));
            s.put("compte", ligne.get("compte"));
            s.put("volumes", volumes);
            lignes.add(s);
        }
        Map<String, Object> corps = new HashMap<>();
        corps.put("lignes", lignes);
        corps.put("version", dsm.get("version"));
        return corps;
    }

    private Map<?, ?> transition(RestTestClient session, Object dsm, String action, Map<String, ?> corps) {
        return corps(session.post().uri(dsms + "/{id}/" + action, dsm).body(corps).exchange().expectStatus().isOk());
    }

    @Test
    @DisplayName("RG-DEC-20 : envoi, prise en instruction, compléments avec motif, renvoi, validation")
    void rg_dec_20_cycle_complet() {
        Map<?, ?> dsm = negociantAvecDsm();
        Object id = id(dsm);
        assertThat(dsm.get("actions")).isEqualTo(List.of("ENREGISTRER", "ENVOYER", "SUPPRIMER", "COMMENTER"));

        Map<?, ?> envoyee = transition(agent, id, "envoi", Map.of("version", 0));
        assertThat(envoyee.get("etat")).isEqualTo("ENVOYEE");
        assertThat(envoyee.get("envoyeeLe")).isNotNull();
        assertThat(envoyee.get("libelleEtat")).isEqualTo("Envoyée");
        assertThat(envoyee.get("actions")).isEqualTo(List.of("COMMENTER"));
        agent.put().uri(dsms + "/{id}", id).body(saisie(envoyee, Map.of())).exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT)
                .expectBody().jsonPath("$.regle").isEqualTo("RG-DEC-21");
        client.get().uri(dsms + "/{id}", id).exchange().expectStatus().isOk()
                .expectBody().jsonPath("$.actions").isEqualTo(List.of("PRENDRE_EN_INSTRUCTION", "COMMENTER",
                        "COMMENTER_INTERNE"));

        transition(client, id, "prise-en-instruction", Map.of("version", 1));
        client.post().uri(dsms + "/{id}/complements", id).body(Map.of("motif", " ", "version", 2)).exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT)
                .expectBody().jsonPath("$.regle").isEqualTo("RG-DEC-20");
        Map<?, ?> complements = transition(client, id, "complements",
                Map.of("motif", "DAE manquants pour l'export", "version", 2));
        assertThat(complements.get("etat")).isEqualTo("COMPLEMENTS_DEMANDES");
        Map<?, ?> recus = transition(agent, id, "envoi", Map.of("version", 3));
        assertThat(recus.get("etat")).isEqualTo("COMPLEMENTS_RECUS");
        assertThat(recus.get("libelleEtat")).isEqualTo("Compléments envoyés");
        client.get().uri(dsms + "/{id}", id).exchange().expectStatus().isOk()
                .expectBody().jsonPath("$.libelleEtat").isEqualTo("Compléments reçus");
        Map<?, ?> validee = transition(client, id, "validation", Map.of("version", 4));
        assertThat(validee.get("etat")).isEqualTo("VALIDEE");
        assertThat(validee.get("valideePar")).isNotNull();

        client.post().uri(dsms + "/{id}/validation", id).body(Map.of()).exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT)
                .expectBody().jsonPath("$.regle").isEqualTo("RG-DEC-20");
        agent.get().uri(dsms + "/{id}/commentaires", id).exchange().expectStatus().isOk()
                .expectBody().jsonPath("$[*].texte").isEqualTo(List.of("DAE manquants pour l'export"))
                .jsonPath("$[0].motif").isEqualTo(true);
        agent.get().uri(dsms + "/{id}/journal", id).exchange().expectStatus().isOk()
                .expectBody().jsonPath("$[*].action").isEqualTo(List.of("DSM_VALIDEE", "DSM_ENVOYEE",
                        "COMPLEMENTS_DEMANDES", "DSM_PRISE_EN_INSTRUCTION", "DSM_ENVOYEE", "DSM_INITIEE"));
    }

    @Test
    @DisplayName("RG-DEC-01, 20 : l'opérateur ne s'instruit pas ; le BNIA n'envoie pas ; une anomalie bloque l'envoi ; 409")
    void rg_dec_01_et_20_refus() {
        Map<?, ?> dsm = negociantAvecDsm();
        Object id = id(dsm);
        client.post().uri(dsms + "/{id}/envoi", id).body(Map.of()).exchange().expectStatus().isForbidden();
        Map<?, ?> desequilibree = corps(agent.put().uri(dsms + "/{id}", id).body(saisie(dsm, Map.of(6, "10")))
                .exchange().expectStatus().isOk());
        agent.post().uri(dsms + "/{id}/envoi", id).body(Map.of("version", desequilibree.get("version"))).exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT)
                .expectBody().jsonPath("$.regle").isEqualTo("RG-DEC-20");
        agent.put().uri(dsms + "/{id}", id).body(saisie(desequilibree, Map.of(6, "0"))).exchange().expectStatus().isOk();
        agent.post().uri(dsms + "/{id}/envoi", id).body(Map.of("version", 0)).exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT);
        transition(agent, id, "envoi", Map.of());
        agent.post().uri(dsms + "/{id}/prise-en-instruction", id).body(Map.of()).exchange().expectStatus().isForbidden();
    }

    @Test
    @DisplayName("RG-DEC-24, 25 : commentaires sur la DSM ou une ligne ; interne invisible au portail ; l'auteur seul modifie")
    void rg_dec_24_25_commentaires() {
        Map<?, ?> dsm = negociantAvecDsm();
        Object id = id(dsm);
        String commentaires = dsms + "/" + id + "/commentaires";
        Object ligne = ((Map<?, ?>) ((List<?>) dsm.get("lignes")).getFirst()).get("id");
        Map<String, Object> surLigne = new HashMap<>();
        surLigne.put("ligne", ligne);
        surLigne.put("texte", "Transfert vers Eauze");
        Map<?, ?> deLAgent = corps(agent.post().uri(commentaires).body(surLigne).exchange().expectStatus().isCreated());
        assertThat(deLAgent.get("cote")).isEqualTo("OPERATEUR");
        assertThat(deLAgent.get("deLUtilisateur")).isEqualTo(true);
        agent.post().uri(commentaires).body(Map.of("texte", "Interne ?", "interne", true)).exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT)
                .expectBody().jsonPath("$.regle").isEqualTo("RG-DEC-24");
        Map<?, ?> interne = corps(client.post().uri(commentaires).body(Map.of("texte", "À vérifier", "interne", true))
                .exchange().expectStatus().isCreated());

        agent.get().uri(commentaires).exchange().expectStatus().isOk()
                .expectBody().jsonPath("$[*].texte").isEqualTo(List.of("Transfert vers Eauze"));
        client.get().uri(commentaires).exchange().expectStatus().isOk()
                .expectBody().jsonPath("$.length()").isEqualTo(2)
                .jsonPath("$[0].deLUtilisateur").isEqualTo(false);

        client.put().uri(commentaires + "/{c}", id(deLAgent)).body(Map.of("texte", "Autre")).exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT)
                .expectBody().jsonPath("$.regle").isEqualTo("RG-DEC-25");
        agent.put().uri(commentaires + "/{c}", id(deLAgent)).body(Map.of("texte", "Transfert vers Eauze, 12 juin"))
                .exchange().expectStatus().isOk()
                .expectBody().jsonPath("$.modifieLe").isNotEmpty();
        client.delete().uri(commentaires + "/{c}", id(interne)).exchange().expectStatus().isNoContent();
        client.get().uri(dsms + "/{id}/journal", id).exchange().expectStatus().isOk()
                .expectBody().jsonPath("$[0].action").isEqualTo("COMMENTAIRE_SUPPRIME");

        String email = emailUnique("observateur");
        membre(operateur, Role.OBSERVATEUR, email);
        connecte(email).post().uri(commentaires).body(Map.of("texte", "Note")).exchange().expectStatus().isForbidden();
    }

    @Test
    @DisplayName("RG-DEC-14 : la DSM suivante reprend le stock de fin de la DSM validée ; le listing du BNIA la montre")
    void rg_dec_14_dsm_suivante_et_listing() {
        Map<?, ?> dsm = negociantAvecDsm();
        Object id = id(dsm);
        // XO vendu en entier : la ligne n'est pas reprise.
        agent.put().uri(dsms + "/{id}", id).body(saisie(dsm, Map.of(17, "1500"))).exchange().expectStatus().isOk();
        transition(agent, id, "envoi", Map.of());

        client.get().uri("/api/declarations/dsm?etats=BROUILLON,ENVOYEE&operateur={op}", operateur).exchange().expectStatus().isOk()
                .expectBody().jsonPath("$[*].id").isEqualTo(List.of(((Number) id).intValue()))
                .jsonPath("$[0].codeOperateur").isNotEmpty()
                .jsonPath("$[0].modifieeLe").isNotEmpty()
                .jsonPath("$[0].prendreEnInstruction").isEqualTo(true);
        client.get().uri("/api/declarations/dsm?mois={m}&operateur={op}", DEBUT.plusMonths(1).toString().substring(0, 7),
                operateur).exchange().expectStatus().isOk().expectBody().jsonPath("$.length()").isEqualTo(0);
        agent.get().uri("/api/declarations/dsm").exchange().expectStatus().isForbidden();

        transition(client, id, "prise-en-instruction", Map.of());
        transition(client, id, "validation", Map.of());
        Map<?, ?> suivante = corps(agent.post().uri(dsms).exchange().expectStatus().isCreated());
        assertThat(((Map<?, ?>) suivante.get("periode")).get("debut")).isEqualTo(DEBUT.plusMonths(1).toString());
        List<?> lignes = (List<?>) suivante.get("lignes");
        assertThat(lignes).hasSize(1);
        assertThat(((Map<?, ?>) lignes.getFirst()).get("cle")).isEqualTo("MB");
        assertThat(((List<?>) ((Map<?, ?>) lignes.getFirst()).get("volumes")).getFirst()).isEqualTo(640.25);
    }
}
