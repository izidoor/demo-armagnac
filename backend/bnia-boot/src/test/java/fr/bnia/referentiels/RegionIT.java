package fr.bnia.referentiels;

import fr.bnia.support.AbstractIT;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class RegionIT extends AbstractIT {

    private static final String URL = "/api/referentiels/regions";

    @Test
    void creer_modifier_desactiver_supprimer_une_region() {
        String code = "R" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        long id = creer(URL, Map.of("code", code, "libelle", "Gers", "enRegionDelimitee", true));

        client.put().uri(URL + "/{id}", id)
                .body(Map.of("code", code + "-C", "libelle", "Gironde", "enRegionDelimitee", false))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.code").isEqualTo(code + "-C")
                .jsonPath("$.libelle").isEqualTo("Gironde")
                .jsonPath("$.enRegionDelimitee").isEqualTo(false);

        client.post().uri(URL + "/{id}/desactivation", id).exchange().expectStatus().isOk();

        client.get().uri(u -> u.path(URL).queryParam("actif", false).queryParam("recherche", code).build())
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.length()").isEqualTo(1).jsonPath("$[0].enRegionDelimitee").isEqualTo(false);

        client.post().uri(URL).body(Map.of("code", (code + "-C").toLowerCase(), "libelle", "Doublon", "enRegionDelimitee", true))
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT);

        client.delete().uri(URL + "/{id}", id).exchange().expectStatus().isNoContent();
    }
}
