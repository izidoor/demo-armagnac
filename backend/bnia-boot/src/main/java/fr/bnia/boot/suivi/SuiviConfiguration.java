package fr.bnia.boot.suivi;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Suivi de l'utilisation du prototype, pour son auteur seul : écrans vus, sections de l'offre lues, temps passé,
 * fichiers téléchargés et importés ({@link SuiviFichiers}), par e-mail et appareil. Rien n'en paraît dans
 * l'application ; la lecture passe par {@code suivi.sh}, avec un jeton.
 *
 * <p>Le schéma {@code suivi} est hors de Flyway et de {@code V1__schema.sql} : {@code flyway.clean()} ne vide que les
 * schémas de {@code spring.flyway.schemas}, le suivi survit donc aux remises à zéro de la démonstration. Les
 * identifiants d'utilisateur étant réattribués après une remise à zéro, chaque événement garde une copie de l'e-mail.
 */
@Configuration(proxyBeanMethods = false)
class SuiviConfiguration implements WebMvcConfigurer {

    private final SuiviFichiers fichiers;

    SuiviConfiguration(SuiviFichiers fichiers) {
        this.fichiers = fichiers;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registre) {
        registre.addInterceptor(fichiers).addPathPatterns("/api/**").excludePathPatterns("/api/suivi/**");
    }

    @Bean
    InitializingBean schemaSuivi(JdbcClient jdbc) {
        return () -> {
            jdbc.sql("create schema if not exists suivi").update();
            jdbc.sql("""
                    create table if not exists suivi.evenement (
                        id        bigint       generated always as identity primary key,
                        instant   timestamptz  not null,
                        session   text         not null,
                        email     text         not null,
                        appareil  text         not null check (appareil in ('ORDINATEUR', 'TABLETTE', 'MOBILE')),
                        type      text         not null,
                        lieu      text         not null,
                        detail    text,
                        statut    integer
                    )""").update();
            // Table créée par une version précédente : colonnes et types d'événement ajoutés depuis.
            jdbc.sql("alter table suivi.evenement add column if not exists detail text").update();
            jdbc.sql("alter table suivi.evenement add column if not exists statut integer").update();
            jdbc.sql("alter table suivi.evenement drop constraint if exists evenement_type_check").update();
            jdbc.sql("""
                    alter table suivi.evenement add constraint evenement_type_check
                        check (type in ('ECRAN', 'OFFRE', 'PRESENCE', 'TELECHARGEMENT', 'IMPORT'))""").update();
            jdbc.sql("create index if not exists evenement_session on suivi.evenement (session, instant)").update();
        };
    }

    @Bean
    JetonSuivi jetonSuivi(@Value("${bnia.suivi.jeton:}") String jeton) {
        return new JetonSuivi(jeton);
    }

    /**
     * L'export ne passe pas par une session : il exige le jeton ({@link SuiviController}). Cette chaîne précède celle
     * de toute l'API ({@code SecuriteConfiguration}, dans {@code authentification}), qui reste inchangée.
     */
    @Bean
    @Order(1)
    SecurityFilterChain exportSuivi(HttpSecurity http) throws Exception {
        http.securityMatcher("/api/suivi/export/**")
                .authorizeHttpRequests(regles -> regles.anyRequest().permitAll())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .requestCache(AbstractHttpConfigurer::disable);
        return http.build();
    }
}
