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

/** Lot 1 de la spec 05 : point de départ, initiation, saisie, anomalies, suppression, journal. */
class DsmIT extends AbstractIT {

    /** Le premier du mois, quatre mois avant le mois courant : une période déjà commencée. */
    private static final LocalDate DEBUT = LocalDate.now().withDayOfMonth(1).minusMonths(4);

    private long nouveauNegociant() {
        Map<String, Object> saisie = new HashMap<>();
        saisie.put("type", "NEGOCIANT");
        saisie.put("code", "T" + unique());
        saisie.put("raisonSociale", "Négoce " + unique());
        saisie.put("ville", "Condom");
        saisie.put("accordDiffusionGeneral", false);
        return creer("/api/operateurs", saisie);
    }

    private static Map<String, Object> ligneStock(String cle, Object millesimeOuDenomination, String compte, String volume) {
        Map<String, Object> ligne = new HashMap<>();
        ligne.put("cle", cle);
        ligne.put(millesimeOuDenomination instanceof Integer ? "millesime" : "denomination", millesimeOuDenomination);
        ligne.put("compte", compte);
        ligne.put("volume", new BigDecimal(volume));
        return ligne;
    }

    private static List<Map<String, Object>> stock() {
        return List.of(ligneStock("NB", "XO", "12", "1500"), ligneStock("MB", 2010, null, "640.25"));
    }

    private long fixer(long operateur) {
        return creer("/api/operateurs/" + operateur + "/point-de-depart", Map.of("debut", DEBUT, "lignes", stock()));
    }

    /** Un agent de l'opérateur, connecté. */
    private RestTestClient agent(long operateur) {
        String email = emailUnique("agent");
        membre(operateur, Role.AGENT, email);
        return connecte(email);
    }

    /** La saisie qui reprend la DSM telle quelle, la première ligne recevant les volumes donnés. */
    @SuppressWarnings("unchecked")
    private static Map<String, Object> saisie(Map<?, ?> dsm, Map<Integer, String> premiereLigne) {
        List<Map<String, Object>> lignes = new ArrayList<>();
        boolean premiere = true;
        for (Map<String, Object> ligne : (List<Map<String, Object>>) dsm.get("lignes")) {
            List<Object> volumes = new ArrayList<>((List<Object>) ligne.get("volumes"));
            volumes.set(0, null);
            volumes.set(25, null);
            if (premiere) {
                premiereLigne.forEach((colonne, volume) -> volumes.set(colonne - 1, new BigDecimal(volume)));
                premiere = false;
            }
            Map<String, Object> saisie = new HashMap<>();
            saisie.put("id", ligne.get("id"));
            saisie.put("cle", ligne.get("cle"));
            saisie.put("millesime", ligne.get("millesime"));
            saisie.put("denomination", ligne.get("denomination"));
            saisie.put("compte", ligne.get("compte"));
            saisie.put("volumes", volumes);
            lignes.add(saisie);
        }
        Map<String, Object> corps = new HashMap<>();
        corps.put("lignes", lignes);
        corps.put("version", dsm.get("version"));
        return corps;
    }

    private static Map<?, ?> corps(RestTestClient.ResponseSpec reponse) {
        return reponse.expectBody(Map.class).returnResult().getResponseBody();
    }

    @Test
    @DisplayName("RG-DEC-13, 17, 18, 23 : le BNIA fixe le point de départ ; l'opérateur initie, saisit, voit ses anomalies, enregistre")
    void scenario_du_lot_1() {
        long operateur = nouveauNegociant();
        long point = fixer(operateur);
        RestTestClient agent = agent(operateur);
        String dsms = "/api/operateurs/" + operateur + "/dsm";

        agent.get().uri(dsms + "/situation").exchange().expectStatus().isOk()
                .expectBody().jsonPath("$.initiable").isEqualTo(true)
                .jsonPath("$.periode.debut").isEqualTo(DEBUT.toString());
        Map<?, ?> dsm = corps(agent.post().uri(dsms).exchange().expectStatus().isCreated());
        long id = ((Number) dsm.get("id")).longValue();
        assertThat(dsm.get("etat")).isEqualTo("BROUILLON");
        assertThat((List<?>) dsm.get("lignes")).hasSize(2);
        assertThat(((List<?>) dsm.get("totaux")).getFirst()).isEqualTo(2140.25);
        assertThat((List<?>) dsm.get("colonnes")).hasSize(26);

        Map<String, Object> negatif = saisie(dsm, Map.of(17, "1600", 6, "10"));
        corps(agent.post().uri(dsms + "/{id}/apercu", id).body(negatif).exchange().expectStatus().isOk());
        agent.post().uri(dsms + "/{id}/apercu", id).body(negatif).exchange().expectStatus().isOk()
                .expectBody()
                .jsonPath("$.anomalies[*].type").isEqualTo(List.of("STOCK_FIN_NEGATIF", "ASSEMBLAGES_DESEQUILIBRES"))
                .jsonPath("$.lignes[0].volumes[25]").isEqualTo(-90.0)
                .jsonPath("$.version").isEqualTo(0);

        Map<?, ?> enregistree = corps(agent.put().uri(dsms + "/{id}", id).body(negatif).exchange().expectStatus().isOk());
        assertThat((List<?>) enregistree.get("anomalies")).hasSize(2);
        assertThat(((Number) enregistree.get("version")).intValue()).isEqualTo(1);
        List<?> lignesAvant = ((List<?>) dsm.get("lignes")).stream().map(l -> ((Map<?, ?>) l).get("id")).toList();
        List<?> lignesApres = ((List<?>) enregistree.get("lignes")).stream().map(l -> ((Map<?, ?>) l).get("id")).toList();
        assertThat(lignesApres).as("identifiants de lignes stables").isEqualTo(lignesAvant);

        agent.get().uri(dsms + "/{id}/journal", id).exchange().expectStatus().isOk()
                .expectBody().jsonPath("$[*].action").isEqualTo(List.of("DSM_ENREGISTREE", "DSM_INITIEE"))
                .jsonPath("$[0].periode").isNotEmpty();
        agent.get().uri(dsms).exchange().expectStatus().isOk()
                .expectBody().jsonPath("$[*].id").isEqualTo(List.of((int) id));
        client.get().uri("/api/operateurs/{op}/declarations/journal", operateur).exchange().expectStatus().isOk()
                .expectBody().jsonPath("$[*].action").isEqualTo(List.of("DSM_ENREGISTREE", "DSM_INITIEE",
                        "POINT_DE_DEPART_FIXE"));
        agent.get().uri("/api/operateurs/{op}/declarations/denominations", operateur).exchange().expectStatus().isOk()
                .expectBody().jsonPath("$").isEqualTo(List.of("XO"));
        assertThat(point).isPositive();
    }

    @Test
    @DisplayName("RG-DEC-01 : le BNIA fixe le point de départ, l'opérateur saisit ; ni l'un à la place de l'autre")
    void rg_dec_01_droits() {
        long operateur = nouveauNegociant();
        RestTestClient agent = agent(operateur);
        String observateurEmail = emailUnique("observateur");
        membre(operateur, Role.OBSERVATEUR, observateurEmail);
        RestTestClient observateur = connecte(observateurEmail);
        RestTestClient etranger = agent(nouveauNegociant());

        agent.post().uri("/api/operateurs/{op}/point-de-depart", operateur)
                .body(Map.of("debut", DEBUT, "lignes", stock()))
                .exchange().expectStatus().isForbidden()
                .expectBody().jsonPath("$.regle").isEqualTo("RG-DEC-01");
        fixer(operateur);
        client.post().uri("/api/operateurs/{op}/dsm", operateur).exchange().expectStatus().isForbidden();
        observateur.post().uri("/api/operateurs/{op}/dsm", operateur).exchange().expectStatus().isForbidden();
        Map<?, ?> dsm = corps(agent.post().uri("/api/operateurs/{op}/dsm", operateur).exchange().expectStatus().isCreated());
        observateur.get().uri("/api/operateurs/{op}/dsm/{id}", operateur, dsm.get("id")).exchange().expectStatus().isOk();
        observateur.put().uri("/api/operateurs/{op}/dsm/{id}", operateur, dsm.get("id")).body(saisie(dsm, Map.of()))
                .exchange().expectStatus().isForbidden();
        client.put().uri("/api/operateurs/{op}/dsm/{id}", operateur, dsm.get("id")).body(saisie(dsm, Map.of()))
                .exchange().expectStatus().isForbidden();
        etranger.get().uri("/api/operateurs/{op}/dsm/{id}", operateur, dsm.get("id")).exchange().expectStatus().isForbidden();
        etranger.get().uri("/api/operateurs/{op}/point-de-depart", operateur).exchange().expectStatus().isForbidden();
        observateur.delete().uri("/api/operateurs/{op}/dsm/{id}", operateur, dsm.get("id")).exchange()
                .expectStatus().isForbidden();
    }

    @Test
    @DisplayName("RG-DEC-06, 15, 16 : début, volume ou colonne grisée refusés avec la règle (422)")
    void regles_enfreintes() {
        long negociant = nouveauNegociant();
        client.post().uri("/api/operateurs/{op}/point-de-depart", negociant)
                .body(Map.of("debut", DEBUT.plusDays(3), "lignes", stock()))
                .exchange().expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT)
                .expectBody().jsonPath("$.regle").isEqualTo("RG-DEC-06");
        fixer(negociant);
        RestTestClient agent = agent(negociant);
        Map<?, ?> dsm = corps(agent.post().uri("/api/operateurs/{op}/dsm", negociant).exchange().expectStatus().isCreated());
        agent.put().uri("/api/operateurs/{op}/dsm/{id}", negociant, dsm.get("id")).body(saisie(dsm, Map.of(17, "-1")))
                .exchange().expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT)
                .expectBody().jsonPath("$.regle").isEqualTo("RG-DEC-15");

        long producteur = nouvelOperateur();
        LocalDate avril = LocalDate.of(LocalDate.now().getYear() - (LocalDate.now().getMonthValue() < 4 ? 1 : 0), 4, 1);
        creer("/api/operateurs/" + producteur + "/point-de-depart", Map.of("debut", avril, "lignes", stock()));
        RestTestClient agentProducteur = agent(producteur);
        Map<?, ?> annuelle = corps(agentProducteur.post().uri("/api/operateurs/{op}/dsm", producteur).exchange()
                .expectStatus().isCreated());
        assertThat(((Map<?, ?>) ((List<?>) annuelle.get("colonnes")).get(18)).get("grisee")).isEqualTo(true);
        agentProducteur.put().uri("/api/operateurs/{op}/dsm/{id}", producteur, annuelle.get("id"))
                .body(saisie(annuelle, Map.of(19, "5")))
                .exchange().expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT)
                .expectBody().jsonPath("$.regle").isEqualTo("RG-DEC-16");
    }

    @Test
    @DisplayName("RG-DEC-05, 07 : un second point de départ, une seconde DSM non validée, une version périmée : 409")
    void conflits() {
        long operateur = nouveauNegociant();
        fixer(operateur);
        client.post().uri("/api/operateurs/{op}/point-de-depart", operateur).body(Map.of("debut", DEBUT, "lignes", stock()))
                .exchange().expectStatus().isEqualTo(HttpStatus.CONFLICT);
        RestTestClient agent = agent(operateur);
        Map<?, ?> dsm = corps(agent.post().uri("/api/operateurs/{op}/dsm", operateur).exchange().expectStatus().isCreated());
        agent.post().uri("/api/operateurs/{op}/dsm", operateur).exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT)
                .expectBody().jsonPath("$.regle").isEqualTo("RG-DEC-05");
        agent.put().uri("/api/operateurs/{op}/dsm/{id}", operateur, dsm.get("id")).body(saisie(dsm, Map.of()))
                .exchange().expectStatus().isOk();
        agent.put().uri("/api/operateurs/{op}/dsm/{id}", operateur, dsm.get("id")).body(saisie(dsm, Map.of()))
                .exchange().expectStatus().isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    @DisplayName("RG-DEC-08 : la DSM en cours suit le stock initial ; la date de début reste tant qu'elle est en cours")
    void rg_dec_08_point_de_depart_modifie() {
        long operateur = nouveauNegociant();
        long point = fixer(operateur);
        RestTestClient agent = agent(operateur);
        Map<?, ?> dsm = corps(agent.post().uri("/api/operateurs/{op}/dsm", operateur).exchange().expectStatus().isCreated());
        String url = "/api/operateurs/" + operateur + "/point-de-depart/" + point;
        client.put().uri(url).body(Map.of("debut", DEBUT, "lignes", List.of(ligneStock("NB", "XO", "12", "1600")),
                        "version", 0))
                .exchange().expectStatus().isOk()
                .expectBody().jsonPath("$.lignes.length()").isEqualTo(1).jsonPath("$.fige").isEqualTo(false);
        agent.get().uri("/api/operateurs/{op}/dsm/{id}", operateur, dsm.get("id")).exchange().expectStatus().isOk()
                .expectBody()
                .jsonPath("$.lignes[0].volumes[0]").isEqualTo(1600.0)
                .jsonPath("$.lignes[1].origine").isEqualTo("AJOUT")
                .jsonPath("$.lignes[1].volumes[0]").isEqualTo(0.0);
        client.put().uri(url).body(Map.of("debut", DEBUT.minusMonths(1), "lignes", stock()))
                .exchange().expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT)
                .expectBody().jsonPath("$.regle").isEqualTo("RG-DEC-08");
    }

    @Test
    @DisplayName("RG-DEC-22 : le BNIA supprime un brouillon ; son journal part avec lui, la suppression reste tracée")
    void rg_dec_22_suppression() {
        long operateur = nouveauNegociant();
        fixer(operateur);
        RestTestClient agent = agent(operateur);
        Map<?, ?> dsm = corps(agent.post().uri("/api/operateurs/{op}/dsm", operateur).exchange().expectStatus().isCreated());
        client.delete().uri("/api/operateurs/{op}/dsm/{id}", operateur, dsm.get("id")).exchange().expectStatus().isNoContent();
        agent.get().uri("/api/operateurs/{op}/dsm/{id}", operateur, dsm.get("id")).exchange().expectStatus().isNotFound();
        agent.get().uri("/api/operateurs/{op}/declarations/journal", operateur).exchange().expectStatus().isOk()
                .expectBody().jsonPath("$[*].action").isEqualTo(List.of("BROUILLON_SUPPRIME", "POINT_DE_DEPART_FIXE"));
        agent.get().uri("/api/operateurs/{op}/dsm/situation", operateur).exchange().expectStatus().isOk()
                .expectBody().jsonPath("$.initiable").isEqualTo(true);
    }
}
