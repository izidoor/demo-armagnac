package fr.bnia.systeme;

import static org.assertj.core.api.Assertions.assertThat;

import fr.bnia.support.AbstractIT;
import org.junit.jupiter.api.Test;

class SystemeIT extends AbstractIT {

    @Test
    void la_sonde_traverse_toute_la_chaine_jusqu_a_postgresql() {
        client.get().uri("/api/systeme/info")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.application").isEqualTo("BNIA")
                .jsonPath("$.version").isNotEmpty()
                .jsonPath("$.versionBase").value(String.class,
                        version -> assertThat(version).startsWith("PostgreSQL 18"));
    }
}
