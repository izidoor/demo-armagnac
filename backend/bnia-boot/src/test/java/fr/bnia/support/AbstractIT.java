package fr.bnia.support;

import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

/**
 * Base des tests d'intégration : application complète sur un port aléatoire, base Testcontainers.
 * Toutes les sous-classes partagent le même contexte Spring, donc le même conteneur.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
@Import(TestcontainersConfiguration.class)
public abstract class AbstractIT {
}
