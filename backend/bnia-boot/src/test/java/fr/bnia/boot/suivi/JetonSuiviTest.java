package fr.bnia.boot.suivi;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class JetonSuiviTest {

    @Test
    void sans_jeton_configure_l_export_est_ferme() {
        JetonSuivi vide = new JetonSuivi(" ");

        assertThat(vide.configure()).isFalse();
        assertThat(vide.accepte("Bearer ")).isFalse();
    }

    @Test
    void seul_le_bon_jeton_est_accepte() {
        JetonSuivi jeton = new JetonSuivi("secret");

        assertThat(jeton.accepte("Bearer secret")).isTrue();
        assertThat(jeton.accepte("Bearer autre")).isFalse();
        assertThat(jeton.accepte("secret")).isFalse();
        assertThat(jeton.accepte(null)).isFalse();
    }
}
