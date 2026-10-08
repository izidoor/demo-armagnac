package fr.bnia.commun.adapter.rest;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * En-tête du contrat OpenAPI, généré depuis les controllers (ADR-007).
 */
@Configuration(proxyBeanMethods = false)
class OpenApiConfiguration {

    @Bean
    OpenAPI bniaOpenApi() {
        return new OpenAPI().info(new Info()
                .title("API BNIA")
                .description("Back-office BNIA et portail opérateur")
                .version("v1"))
                // Même origine : le front appelle /api via son proxy. Fixé pour que le contrat
                // exporté ne dépende pas du port de l'instance qui l'a produit.
                .servers(List.of(new Server().url("/")));
    }
}
