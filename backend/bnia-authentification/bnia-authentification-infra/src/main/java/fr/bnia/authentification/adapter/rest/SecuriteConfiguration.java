package fr.bnia.authentification.adapter.rest;

import fr.bnia.authentification.application.ConsulterConnexions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

/**
 * Spring Security seul (décision du 2026-10-08, ADR-015) : toute l'API exige une session, sauf la
 * connexion, le mot de passe oublié, le premier mot de passe et les liens reçus par e-mail. La session
 * est serveur, en base (Spring Session JDBC, A-AUT-3), protégée par un jeton CSRF lu par le front dans
 * un cookie. Aucun droit ici : les cas d'usage les vérifient (ADR-016).
 */
@Configuration(proxyBeanMethods = false)
class SecuriteConfiguration {

    /** Sans session. */
    private static final String[] PUBLIQUES_EN_ECRITURE = {
            "/api/authentification/connexion", "/api/authentification/code", "/api/authentification/code/renvoi",
            "/api/authentification/mot-de-passe-oublie", "/api/authentification/reinitialisation",
            "/api/authentification/premier-mot-de-passe", "/api/invitations/*/acceptation",
            "/api/changements-email/confirmation"};

    private static final String[] PUBLIQUES_EN_LECTURE = {"/api/authentification/session", "/api/invitations/*"};

    /**
     * Aucune connexion par nom d'utilisateur : la preuve d'identité passe par les cas d'usage du contexte. Ce
     * service vide évite l'utilisateur par défaut de Spring Boot.
     */
    @Bean
    UserDetailsService aucunUtilisateurSpring() {
        return nom -> {
            throw new UsernameNotFoundException("Connexion par la page de connexion seulement.");
        };
    }

    @Bean
    SecurityContextRepository depotContexteSecurite() {
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    SecurityFilterChain api(HttpSecurity http, ConsulterConnexions connexions, SecurityContextRepository depot)
            throws Exception {
        http.securityMatcher("/api/**")
                .authorizeHttpRequests(regles -> regles
                        .requestMatchers(HttpMethod.POST, PUBLIQUES_EN_ECRITURE).permitAll()
                        .requestMatchers(HttpMethod.GET, PUBLIQUES_EN_LECTURE).permitAll()
                        .anyRequest().authenticated())
                .securityContext(contexte -> contexte.securityContextRepository(depot))
                .csrf(csrf -> csrf.spa())
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .requestCache(AbstractHttpConfigurer::disable)
                .exceptionHandling(erreurs -> erreurs.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .addFilterBefore(new FiltreSessionValide(connexions), AuthorizationFilter.class);
        return http.build();
    }
}
