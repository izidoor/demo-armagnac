package fr.bnia.contratsarmagnac.adapter.integration;

import fr.bnia.contratsarmagnac.application.ParametresContrats;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Paramètres du contexte, tirés de la configuration. */
@Configuration(proxyBeanMethods = false)
class ConfigurationContrats {

    @Bean
    ParametresContrats parametresContrats(@Value("${bnia.url-publique}") String urlPublique) {
        return new ParametresContrats(urlPublique);
    }
}
