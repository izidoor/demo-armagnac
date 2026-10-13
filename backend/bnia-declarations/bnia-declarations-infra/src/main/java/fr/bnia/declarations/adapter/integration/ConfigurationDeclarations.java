package fr.bnia.declarations.adapter.integration;

import fr.bnia.declarations.application.ParametresDeclarations;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Valeurs de configuration du contexte, fixes, lues dans {@code application.yml}. */
@Configuration(proxyBeanMethods = false)
class ConfigurationDeclarations {

    @Bean
    ParametresDeclarations parametresDeclarations(@Value("${bnia.declarations.classe-tva-cvo}") String classeTvaCvo) {
        return new ParametresDeclarations(classeTvaCvo);
    }
}
