package fr.bnia.operateurs;

import static org.assertj.core.api.Assertions.assertThat;

import fr.bnia.support.AbstractIT;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.dhatim.fastexcel.reader.ReadableWorkbook;
import org.dhatim.fastexcel.reader.Row;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

/** Portail (« Mon entreprise », contacts, recherche) et export Excel du back-office. */
class PortailOperateurIT extends AbstractIT {

    private static final String BO = "/api/operateurs";
    private static final String PORTAIL = "/api/portail/operateurs";

    private long operateur;
    private String raisonSociale;

    @BeforeEach
    void operateur() {
        raisonSociale = "Maison " + suffixe();
        var saisie = new HashMap<String, Object>();
        saisie.put("type", "PRODUCTEUR");
        saisie.put("code", "T" + suffixe());
        saisie.put("raisonSociale", raisonSociale);
        saisie.put("identifiantOdg", "ODG-" + suffixe());
        saisie.put("commentaireInterne", "Ne pas montrer");
        saisie.put("ville", "Condom");
        saisie.put("accordDiffusionGeneral", false);
        operateur = creer(BO, saisie);
    }

    @Test
    @DisplayName("RG-OPE-36 : le profil du portail masque le commentaire interne ; seuls les champs ouverts changent")
    void rg_ope_36_profil() {
        client.get().uri(PORTAIL + "/{id}/profil", operateur).exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.raisonSociale").isEqualTo(raisonSociale)
                .jsonPath("$.commentaireInterne").doesNotExist();

        var profil = new HashMap<String, Object>();
        profil.put("nomCommercial", "Les Vignes");
        profil.put("ville", "Eauze");
        profil.put("emailGeneral", "contact@vignes.fr");
        profil.put("accordDiffusionGeneral", true);
        profil.put("raisonSociale", "Tentative");
        client.put().uri(PORTAIL + "/{id}/profil", operateur).body(profil).exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.nomCommercial").isEqualTo("Les Vignes")
                .jsonPath("$.ville").isEqualTo("Eauze")
                .jsonPath("$.raisonSociale").isEqualTo(raisonSociale);
        client.get().uri(BO + "/{id}", operateur).exchange()
                .expectBody().jsonPath("$.commentaireInterne").isEqualTo("Ne pas montrer");

        client.put().uri(PORTAIL + "/{id}/diffusion", operateur).body(Map.of("niveau", "PARTIEL", "millesimes", List.of(2010)))
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.millesimesDiffusables").isEqualTo(List.of(2010));
    }

    @Test
    @DisplayName("RG-OPE-30 et RG-OPE-35 : au portail, le dernier administrateur reste ; l'opérateur inactif est fermé")
    void rg_ope_30_35_contacts_au_portail() {
        String contacts = PORTAIL + "/" + operateur + "/contacts";
        long durand = creer(contacts, Map.of("nom", "Durand", "email", "jeanne." + suffixe().toLowerCase() + "@exemple.fr"));
        client.put().uri(BO + "/{o}/contacts/{c}/portail", operateur, durand).body(Map.of("acces", true, "administrateur", true))
                .exchange().expectStatus().isOk();

        client.post().uri(contacts + "/{id}/desactivation", durand)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT)
                .expectBody().jsonPath("$.regle").isEqualTo("RG-OPE-30");
        client.get().uri(contacts + "/{id}/journal", durand).exchange()
                .expectStatus().isOk().expectBody().jsonPath("$[1].auteur").isEqualTo("Portail");

        client.post().uri(BO + "/{id}/desactivation", operateur).exchange().expectStatus().isOk();
        client.get().uri(PORTAIL + "/{id}/profil", operateur)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT)
                .expectBody().jsonPath("$.regle").isEqualTo("RG-OPE-35");
        client.post().uri(contacts).body(Map.of("nom", "Petit"))
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT)
                .expectBody().jsonPath("$.regle").isEqualTo("RG-OPE-35");
    }

    @Test
    @DisplayName("RG-OPE-37 : recherche du portail, refusée sous trois caractères")
    void rg_ope_37_recherche() {
        client.get().uri(u -> u.path(PORTAIL + "/recherche").queryParam("texte", raisonSociale.toLowerCase()).build())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].id").isEqualTo(operateur)
                .jsonPath("$[0].ville").isEqualTo("Condom")
                .jsonPath("$[0].identifiantOdg").doesNotExist();
        client.get().uri(u -> u.path(PORTAIL + "/recherche").queryParam("texte", "ab").build())
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT)
                .expectBody().jsonPath("$.regle").isEqualTo("RG-OPE-37");
    }

    @Test
    void sans_profil_demo_le_portail_n_a_pas_d_operateur() {
        client.get().uri("/api/portail/operateur-demo").exchange().expectStatus().isNotFound();
    }

    @Test
    @DisplayName("RG-OPE-38 : le listing filtré s'exporte au format Excel, avec ses colonnes")
    void rg_ope_38_export_excel() throws IOException {
        byte[] fichier = client.get().uri(u -> u.path(BO + "/export.xlsx").queryParam("raisonSociale", raisonSociale).build())
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
    }

    private static String suffixe() {
        return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
