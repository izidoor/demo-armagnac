package fr.bnia.referentiels;

import fr.bnia.support.AbstractIT;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.client.RestTestClient;

class RegionIT extends AbstractIT {

    private static final String URL = "/api/referentiels/regions";

    @Autowired
    RestTestClient client;

    @Test
    void creer_modifier_desactiver_une_region() {
        String code = "R" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        client.post().uri(URL).body(Map.of("code", code, "libelle", "Gers", "enRegionDelimitee", true))
                .exchange()
                .expectStatus().isCreated()
                .expectBody().jsonPath("$.enRegionDelimitee").isEqualTo(true);

        client.put().uri(URL + "/{code}", code).body(Map.of("libelle", "Gironde", "enRegionDelimitee", false))
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.libelle").isEqualTo("Gironde").jsonPath("$.enRegionDelimitee").isEqualTo(false);

        client.post().uri(URL + "/{code}/desactivation", code)
                .exchange()
                .expectStatus().isOk();

        client.get().uri(u -> u.path(URL).queryParam("actif", false).queryParam("recherche", code).build())
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.length()").isEqualTo(1).jsonPath("$[0].enRegionDelimitee").isEqualTo(false);

        client.post().uri(URL).body(Map.of("code", code.toLowerCase(), "libelle", "Doublon", "enRegionDelimitee", true))
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT);
    }
}
