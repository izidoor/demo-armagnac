package fr.bnia.boot;

import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.flyway.autoconfigure.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Base de développement recréée automatiquement quand {@code V1__schema.sql} a changé (ADR-005).
 *
 * <p>V1 est modifiée sur place à chaque incrément : Flyway refuse alors de démarrer (checksum). En
 * profil {@code dev} seulement, une base dont la validation échoue est vidée puis remigrée. Sinon
 * la migration est normale et les données de dev sont conservées. Le vidage exige
 * {@code spring.flyway.clean-disabled: false}, posé dans {@code application-dev.yml} uniquement.
 */
@Configuration(proxyBeanMethods = false)
@Profile("dev")
class MigrationDevConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(MigrationDevConfiguration.class);

    @Bean
    FlywayMigrationStrategy recreerLaBaseSiV1AChange() {
        return MigrationDevConfiguration::migrer;
    }

    static void migrer(Flyway flyway) {
        boolean dejaMigree = flyway.info().applied().length > 0;
        if (dejaMigree && !flyway.validateWithResult().validationSuccessful) {
            LOG.warn("V1__schema.sql a changé depuis la dernière migration : base de dev vidée puis recréée.");
            flyway.clean();
        }
        flyway.migrate();
    }
}
