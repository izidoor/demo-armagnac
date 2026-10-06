package fr.bnia.systeme;

import static org.assertj.core.api.Assertions.assertThat;

import fr.bnia.support.AbstractIT;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.client.RestTestClient;

class SystemeIT extends AbstractIT {

    @Autowired
    RestTestClient client;

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
