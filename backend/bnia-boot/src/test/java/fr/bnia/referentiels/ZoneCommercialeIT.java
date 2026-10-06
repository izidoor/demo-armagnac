package fr.bnia.referentiels;

import fr.bnia.support.AbstractIT;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.client.RestTestClient;

/**
 * Chaîne complète d'une liste simple : REST, cas d'usage, Spring Data JDBC, PostgreSQL.
 * La base est partagée entre les IT : chaque test crée ses propres codes.
 */
class ZoneCommercialeIT extends AbstractIT {

    private static final String URL = "/api/referentiels/zones-commerciales";

    @Autowired
    RestTestClient client;

    @Test
    void creer_consulter_modifier_desactiver_reactiver() {
        String code = codeUnique();

        client.post().uri(URL).body(Map.of("code", code, "libelle", "Europe"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody().jsonPath("$.code").isEqualTo(code).jsonPath("$.actif").isEqualTo(true);

        client.put().uri(URL + "/{code}", code).body(Map.of("libelle", "Europe géographique"))
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.libelle").isEqualTo("Europe géographique");

        client.post().uri(URL + "/{code}/desactivation", code)
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.actif").isEqualTo(false);

        client.get().uri(URL + "/{code}", code)
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.actif").isEqualTo(false);

        client.post().uri(URL + "/{code}/reactivation", code)
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.actif").isEqualTo(true);
    }

    @Test
    @DisplayName("RG-REF-01 : un code déjà pris, à la casse près, est refusé en 409")
    void rg_ref_01_code_en_double() {
        String code = codeUnique();
        creer(code, "Asie");

        client.post().uri(URL).body(Map.of("code", code.toLowerCase(), "libelle", "Asie bis"))
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT)
                .expectBody().jsonPath("$.regle").isEqualTo("RG-REF-01");
    }

    @Test
    @DisplayName("RG-REF-03 : un libellé vide est refusé en 422")
    void rg_ref_03_libelle_vide() {
        client.post().uri(URL).body(Map.of("code", codeUnique(), "libelle", " "))
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT)
                .expectBody().jsonPath("$.regle").isEqualTo("RG-REF-03");
    }

    @Test
    @DisplayName("RG-REF-04 : le filtre actif écarte les valeurs désactivées")
    void rg_ref_04_filtre_actif() {
        String libelle = "Filtre " + codeUnique();
        String actif = creer(codeUnique(), libelle + " actif");
        String inactif = creer(codeUnique(), libelle + " inactif");
        client.post().uri(URL + "/{code}/desactivation", inactif).exchange().expectStatus().isOk();

        client.get().uri(u -> u.path(URL).queryParam("actif", true).queryParam("recherche", libelle).build())
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.length()").isEqualTo(1).jsonPath("$[0].code").isEqualTo(actif);

        client.get().uri(u -> u.path(URL).queryParam("recherche", libelle.toUpperCase()).build())
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.length()").isEqualTo(2);
    }

    @Test
    void une_valeur_inconnue_renvoie_404() {
        client.get().uri(URL + "/{code}", "INCONNU-" + codeUnique())
                .exchange()
                .expectStatus().isNotFound();
    }

    private String creer(String code, String libelle) {
        client.post().uri(URL).body(Map.of("code", code, "libelle", libelle))
                .exchange()
                .expectStatus().isCreated();
        return code;
    }

    private static String codeUnique() {
        return "Z" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
