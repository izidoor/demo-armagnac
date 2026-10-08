package fr.bnia.operateurs.adapter.integration;

import fr.bnia.operateurs.application.ParametresOperateurs;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Valeurs de configuration du contexte (décision du 2026-10-08 : fixes, non modifiables à l'écran), lues
 * dans {@code application.yml}.
 */
@Configuration(proxyBeanMethods = false)
class ConfigurationOperateurs {

    @Bean
    ParametresOperateurs parametresOperateurs(
            @Value("${bnia.url-publique}") String urlPublique,
            @Value("${bnia.operateurs.validite-invitation}") Duration validiteInvitation,
            @Value("${bnia.operateurs.validite-changement-email}") Duration validiteChangementEmail,
            @Value("${bnia.operateurs.delai-premier-mot-de-passe}") Duration delaiPremierMotDePasse) {
        return new ParametresOperateurs(urlPublique, validiteInvitation, validiteChangementEmail, delaiPremierMotDePasse);
    }
}
