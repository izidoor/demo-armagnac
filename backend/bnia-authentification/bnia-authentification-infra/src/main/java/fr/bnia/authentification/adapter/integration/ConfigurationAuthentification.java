package fr.bnia.authentification.adapter.integration;

import fr.bnia.authentification.application.ParametresAuthentification;
import fr.bnia.authentification.domain.Politique;
import java.time.Duration;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Durées et seuils de l'authentification, lus dans {@code application*.yml} (RG-AUT-14). */
@Configuration(proxyBeanMethods = false)
class ConfigurationAuthentification {

    @Bean
    ParametresAuthentification parametresAuthentification(
            @Value("${bnia.url-publique}") String urlPublique,
            @Value("${bnia.authentification.validite-code}") Duration validiteCode,
            @Value("${bnia.authentification.validite-lien}") Duration validiteLien,
            @Value("${bnia.authentification.validite-confiance}") Duration validiteConfiance,
            @Value("${bnia.authentification.echecs-avant-blocage}") int echecsAvantBlocage,
            @Value("${bnia.authentification.duree-blocage}") Duration dureeBlocage,
            @Value("${bnia.authentification.longueur-mot-de-passe}") int longueurMotDePasse,
            @Value("${bnia.authentification.code-fixe:}") String codeFixe) {
        return new ParametresAuthentification(new Politique(validiteCode, validiteLien, validiteConfiance,
                echecsAvantBlocage, dureeBlocage, longueurMotDePasse,
                Optional.of(codeFixe).filter(c -> !c.isBlank())), urlPublique);
    }
}
