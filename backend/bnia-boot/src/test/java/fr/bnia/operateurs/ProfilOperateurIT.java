package fr.bnia.operateurs;

import static org.assertj.core.api.Assertions.assertThat;

import fr.bnia.operateurs.domain.partage.Role;
import fr.bnia.support.AbstractIT;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.dhatim.fastexcel.reader.ReadableWorkbook;
import org.dhatim.fastexcel.reader.Row;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.client.RestTestClient;

/**
 * Ce qu'un opérateur fait de sa propre fiche (« Mon entreprise »), la recherche d'un autre opérateur, le
 * premier ADMIN invité à la création, la désactivation et l'export du back-office.
 */
class ProfilOperateurIT extends AbstractIT {

    private static final String OPERATEURS = "/api/operateurs";

    private long operateur;
    private String raisonSociale;
    private String email;
    private RestTestClient admin;

    @BeforeEach
    void operateur() {
        raisonSociale = "Maison " + unique();
        var saisie = new HashMap<String, Object>();
        saisie.put("type", "PRODUCTEUR");
        saisie.put("code", "T" + unique());
        saisie.put("raisonSociale", raisonSociale);
        saisie.put("identifiantOdg", "ODG-" + unique());
        saisie.put("commentaireInterne", "Ne pas montrer");
        saisie.put("ville", "Condom");
        saisie.put("accordDiffusionGeneral", false);
        operateur = creer(OPERATEURS, saisie);
        email = emailUnique("jeanne");
        membre(operateur, Role.ADMIN, email);
        admin = connecte(email);
    }

    @Test
    @DisplayName("RG-OPE-36 : l'ADMIN d'un opérateur ne voit pas le commentaire interne et ne modifie que les champs ouverts")
    void rg_ope_36_profil() {
        admin.get().uri(OPERATEURS + "/{id}", operateur).exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.raisonSociale").isEqualTo(raisonSociale)
                .jsonPath("$.commentaireInterne").doesNotExist()
                .jsonPath("$.droits.personnelBnia").isEqualTo(false);

        var profil = new HashMap<String, Object>();
        profil.put("nomCommercial", "Les Vignes");
        profil.put("ville", "Eauze");
        profil.put("emailGeneral", "contact@vignes.fr");
        profil.put("accordDiffusionGeneral", true);
        admin.put().uri(OPERATEURS + "/{id}/profil", operateur).body(profil).exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.nomCommercial").isEqualTo("Les Vignes")
                .jsonPath("$.ville").isEqualTo("Eauze")
                .jsonPath("$.raisonSociale").isEqualTo(raisonSociale);
        client.get().uri(OPERATEURS + "/{id}", operateur).exchange()
                .expectBody().jsonPath("$.commentaireInterne").isEqualTo("Ne pas montrer");

        admin.put().uri(OPERATEURS + "/{id}/identite", operateur)
                .body(Map.of("type", "PRODUCTEUR", "code", "X" + unique(), "raisonSociale", "Tentative",
                        "accordDiffusionGeneral", false))
                .exchange()
                .expectStatus().isForbidden()
                .expectBody().jsonPath("$.regle").isEqualTo("RG-OPE-36");
        admin.put().uri(OPERATEURS + "/{id}/diffusion", operateur)
                .body(Map.of("niveau", "PARTIEL", "millesimes", List.of(2010), "accesStatistiques", false))
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.millesimesDiffusables").isEqualTo(List.of(2010));
        admin.put().uri(OPERATEURS + "/{id}/diffusion", operateur)
                .body(Map.of("niveau", "TOTAL", "accesStatistiques", true))
                .exchange()
                .expectStatus().isForbidden()
                .expectBody().jsonPath("$.regle").isEqualTo("RG-OPE-36");
    }

    @Test
    @DisplayName("RG-OPE-37 et 39 : la recherche ne montre que les opérateurs actifs, jamais le BNIA, et exige trois caractères")
    void rg_ope_37_39_recherche() {
        admin.get().uri(u -> u.path(OPERATEURS + "/recherche").queryParam("texte", raisonSociale.toLowerCase()).build())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].id").isEqualTo(operateur)
                .jsonPath("$[0].ville").isEqualTo("Condom")
                .jsonPath("$[0].identifiantOdg").doesNotExist();
        admin.get().uri(u -> u.path(OPERATEURS + "/recherche").queryParam("texte", "ab").build())
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT)
                .expectBody().jsonPath("$.regle").isEqualTo("RG-OPE-37");
        assertThat(operateurs.rechercher("BNIA")).noneMatch(r -> r.id() == operateurs.operateurBnia());
    }

    @Test
    @DisplayName("RG-OPE-40 : un ADMIN du BNIA crée un opérateur en invitant son premier ADMIN")
    void rg_ope_40_premier_admin() {
        String premier = emailUnique("premier");
        var saisie = new HashMap<String, Object>();
        saisie.put("type", "NEGOCIANT");
        saisie.put("code", "N" + unique());
        saisie.put("raisonSociale", "Négoce " + unique());
        saisie.put("accordDiffusionGeneral", false);
        saisie.put("premierAdmin", Map.of("email", premier, "nom", "Lafitte", "prenom", "Marc"));
        long cree = creer(OPERATEURS, saisie);

        assertThat(courriels.dernier(premier).getText()).contains("jeton=");
        client.get().uri(OPERATEURS + "/{id}/annuaire", cree)
                .exchange()
                .expectBody()
                .jsonPath("$[0].genre").isEqualTo("INVITATION")
                .jsonPath("$[0].role").isEqualTo("ADMIN");
    }

    @Test
    @DisplayName("RG-OPE-26 : désactivé, l'opérateur gèle les accès de ses membres ; réactivé, il les rend")
    void rg_ope_26_desactivation() {
        admin.post().uri(OPERATEURS + "/{id}/desactivation", operateur)
                .exchange()
                .expectStatus().isForbidden()
                .expectBody().jsonPath("$.regle").isEqualTo("RG-OPE-32");
        client.post().uri(OPERATEURS + "/{id}/desactivation", operateur)
                .exchange().expectStatus().isOk().expectBody().jsonPath("$.actif").isEqualTo(false);

        admin.get().uri("/api/moi").exchange().expectStatus().isUnauthorized();
        client.post().uri(OPERATEURS + "/{id}/reactivation", operateur).exchange().expectStatus().isOk();
        connecte(email).get().uri("/api/moi").exchange().expectStatus().isOk();
    }

    @Test
    @DisplayName("RG-OPE-38 : le listing filtré s'exporte au format Excel, avec ses colonnes")
    void rg_ope_38_export_excel() throws IOException {
        byte[] fichier = client.get().uri(u -> u.path(OPERATEURS + "/export.xlsx").queryParam("recherche", raisonSociale).build())
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueMatches("Content-Disposition", "attachment; filename=\"operateurs-\\d{4}-\\d{2}-\\d{2}\\.xlsx\"")
                .expectBody(byte[].class).returnResult().getResponseBody();

        try (var classeur = new ReadableWorkbook(new ByteArrayInputStream(fichier))) {
            List<Row> lignes = classeur.getFirstSheet().read();
            assertThat(lignes.get(0).getCellText(0)).isEqualTo("Type");
            assertThat(lignes.get(0).getCellText(7)).isEqualTo("E-mail général");
            assertThat(lignes).hasSize(2);
            assertThat(lignes.get(1).getCellText(0)).isEqualTo("Producteur");
            assertThat(lignes.get(1).getCellText(2)).isEqualTo(raisonSociale);
        }
        admin.get().uri(OPERATEURS + "/export.xlsx").exchange().expectStatus().isForbidden();
    }
}
