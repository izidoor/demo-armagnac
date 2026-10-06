package fr.bnia.support;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.client.RestTestClient;

/**
 * Base des tests d'intégration : application complète sur un port aléatoire, base Testcontainers.
 * Toutes les sous-classes partagent le même contexte Spring, donc le même conteneur.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
@Import(TestcontainersConfiguration.class)
public abstract class AbstractIT {

    @Autowired
    protected RestTestClient client;

    /** Crée une ressource par {@code POST}, attend un 201 et renvoie son identifiant. */
    protected long creer(String uri, Map<String, ?> corps) {
        Map<?, ?> reponse = client.post().uri(uri).body(corps)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Map.class)
                .returnResult()
                .getResponseBody();
        return ((Number) reponse.get("id")).longValue();
    }
}
