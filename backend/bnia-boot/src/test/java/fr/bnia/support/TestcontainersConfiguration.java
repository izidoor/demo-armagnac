package fr.bnia.support;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * PostgreSQL de test (ADR-003). Le conteneur est un singleton : partagé par toutes les classes de
 * test, il démarre une fois par exécution de Failsafe. Les e-mails sont retenus, pas envoyés.
 */
@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

    private static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:18");

    @Bean
    @ServiceConnection
    PostgreSQLContainer postgres() {
        return POSTGRES;
    }

    @Bean
    CourrielsCaptures courriels() {
        return new CourrielsCaptures();
    }

    /** À la place de Web Push (spec 11) : préféré à {@code WebPushHttp}, qui n'a pas de clé dans les tests. */
    @Bean
    @Primary
    PushCaptures push() {
        return new PushCaptures();
    }
}
