package fr.bnia.boot;

import static org.assertj.core.api.Assertions.assertThat;

import fr.bnia.support.AbstractIT;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.JdbcClient;

/**
 * Simule une modification de V1 (checksum faussé) et vérifie que la stratégie du profil dev recrée
 * la base. Les autres IT n'en souffrent pas : chacun crée ses propres données, et la session d'ADMIN du
 * BNIA, effacée avec la base, est rouverte par le test suivant.
 */
class MigrationDevConfigurationIT extends AbstractIT {

    @Autowired
    Flyway flyway;

    @Autowired
    JdbcClient jdbcClient;

    @AfterEach
    void sessionsEffacees() {
        oublierLesSessions();
    }

    @Test
    void une_v1_modifiee_entraine_la_recreation_de_la_base() {
        Flyway flywayDev = Flyway.configure()
                .configuration(flyway.getConfiguration())
                .cleanDisabled(false)
                .load();
        jdbcClient.sql("insert into referentiels.zone_commerciale (code, libelle) values ('MIGR', 'Avant')").update();
        jdbcClient.sql("update public.flyway_schema_history set checksum = 0 where version = '1'").update();
        assertThat(flywayDev.validateWithResult().validationSuccessful).isFalse();

        MigrationDevConfiguration.migrer(flywayDev);

        assertThat(flywayDev.validateWithResult().validationSuccessful).isTrue();
        assertThat(jdbcClient.sql("select count(*) from referentiels.zone_commerciale where code = 'MIGR'")
                .query(Integer.class).single()).isZero();
    }

    @Test
    void une_base_a_jour_est_conservee() {
        jdbcClient.sql("insert into referentiels.zone_commerciale (code, libelle) values ('GARDE', 'Gardée')").update();

        MigrationDevConfiguration.migrer(flyway);

        assertThat(jdbcClient.sql("select count(*) from referentiels.zone_commerciale where code = 'GARDE'")
                .query(Integer.class).single()).isOne();
    }
}
