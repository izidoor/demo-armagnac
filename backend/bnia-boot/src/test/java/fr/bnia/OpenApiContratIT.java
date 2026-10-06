package fr.bnia;

import static org.assertj.core.api.Assertions.assertThat;

import fr.bnia.support.AbstractIT;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.client.RestTestClient;

/**
 * Exporte le contrat OpenAPI généré depuis les controllers vers {@code docs/api/openapi.json}
 * (ADR-007). Le front génère son client depuis ce fichier ; le diff git montre toute évolution.
 */
class OpenApiContratIT extends AbstractIT {

    private static final Path CONTRAT = Path.of("../../docs/api/openapi.json");

    @Autowired
    RestTestClient client;

    @Test
    void exporte_le_contrat_openapi() throws IOException {
        String contrat = client.get().uri("/v3/api-docs")
                .exchange()
                .expectStatus().isOk()
                .returnResult(String.class)
                .getResponseBody();

        assertThat(contrat).contains("\"/api/systeme/info\"");
        Files.createDirectories(CONTRAT.getParent());
        Files.writeString(CONTRAT, contrat.strip() + "\n");
    }
}
