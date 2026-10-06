package fr.bnia.referentiels;

import fr.bnia.support.AbstractIT;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

/** Les volumes sont uniques en base : ce test n'utilise que des volumes au-delà de 9 000 L. */
class ContenanceIT extends AbstractIT {

    private static final String URL = "/api/referentiels/contenances";

    @Test
    void creer_corriger_le_volume_desactiver_une_contenance() {
        long id = creer(URL, Map.of("volume", "9001.05"));

        client.get().uri(URL + "/{id}", id)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.libelle").isEqualTo("9001,05 L")
                .jsonPath("$.libelleSaisi").doesNotExist();

        client.put().uri(URL + "/{id}", id).body(Map.of("volume", "9001.5", "libelle", "Foudre"))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(id)
                .jsonPath("$.volume").isEqualTo(9001.5)
                .jsonPath("$.libelle").isEqualTo("Foudre");

        client.post().uri(URL + "/{id}/desactivation", id)
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.actif").isEqualTo(false);

        client.get().uri(u -> u.path(URL).queryParam("recherche", "foudre").build())
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.length()").isEqualTo(1);

        client.delete().uri(URL + "/{id}", id).exchange().expectStatus().isNoContent();
    }

    @Test
    @DisplayName("RG-REF-10 : un volume déjà présent, quelle que soit son écriture, est refusé en 409")
    void rg_ref_10_volume_en_double() {
        creer(URL, Map.of("volume", "9002.7"));
        long autre = creer(URL, Map.of("volume", "9002.8"));

        client.post().uri(URL).body(Map.of("volume", "9002.700"))
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT)
                .expectBody().jsonPath("$.regle").isEqualTo("RG-REF-10");

        client.put().uri(URL + "/{id}", autre).body(Map.of("volume", "9002.70"))
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT);
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
