package fr.bnia.systeme.application;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ConsulterInfoSystemeTest {

    @Test
    void renvoie_l_application_et_la_version_de_la_base() {
        var casDUsage = new ConsulterInfoSysteme(() -> "PostgreSQL 18.0");

        InfoSysteme info = casDUsage.executer();

        assertThat(info.application()).isEqualTo("BNIA");
        assertThat(info.version()).isNotBlank();
        assertThat(info.versionBase()).isEqualTo("PostgreSQL 18.0");
    }
}
