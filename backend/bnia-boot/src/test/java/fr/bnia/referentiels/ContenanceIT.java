package fr.bnia.referentiels;

import fr.bnia.support.AbstractIT;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.client.RestTestClient;

/** Les volumes sont uniques en base : ce test n'utilise que des volumes au-delà de 9 000 L. */
class ContenanceIT extends AbstractIT {

    private static final String URL = "/api/referentiels/contenances";

    @Autowired
    RestTestClient client;

    @Test
    void creer_modifier_desactiver_une_contenance_identifiee_par_son_volume() {
        client.post().uri(URL).body(Map.of("volume", "9001.5"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.volume").isEqualTo(9001.5)
                .jsonPath("$.libelle").isEqualTo("9001,50 L")
                .jsonPath("$.libelleSaisi").doesNotExist();

        client.put().uri(URL + "/{volume}", "9001.500").body(Map.of("libelle", "Foudre"))
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.libelle").isEqualTo("Foudre").jsonPath("$.libelleSaisi").isEqualTo("Foudre");

        client.post().uri(URL + "/{volume}/desactivation", "9001.5")
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.actif").isEqualTo(false);

        client.get().uri(u -> u.path(URL).queryParam("recherche", "foudre").build())
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.length()").isEqualTo(1);
    }

    @Test
    @DisplayName("RG-REF-10 : un volume déjà présent, quelle que soit son écriture, est refusé en 409")
    void rg_ref_10_volume_en_double() {
        client.post().uri(URL).body(Map.of("volume", "9002.7")).exchange().expectStatus().isCreated();

        client.post().uri(URL).body(Map.of("volume", "9002.700"))
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT)
                .expectBody().jsonPath("$.regle").isEqualTo("RG-REF-10");
    }

    @Test
    @DisplayName("RG-REF-10 : un volume nul ou à plus de trois décimales est refusé en 422")
    void rg_ref_10_volume_invalide() {
        client.post().uri(URL).body(Map.of("volume", "0"))
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT)
                .expectBody().jsonPath("$.regle").isEqualTo("RG-REF-10");

        client.post().uri(URL).body(Map.of("volume", "9003.0001"))
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT);
    }
}
