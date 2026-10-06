package fr.bnia.boot;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Horloge injectée dans les cas d'usage qui dépendent de la date du jour : les tests leur passent une
 * horloge fixe.
 */
@Configuration(proxyBeanMethods = false)
class HorlogeConfiguration {

    @Bean
    Clock horloge() {
        return Clock.systemDefaultZone();
    }
}
