package fr.bnia.boot.suivi;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

class SuiviFichiersTest {

    @Test
    void un_navigateur_marque_par_suivi_non_est_exclu() {
        MockHttpServletRequest requete = new MockHttpServletRequest();
        assertThat(SuiviFichiers.exclu(requete)).isFalse();

        requete.setCookies(new Cookie("bnia-suivi", "non"));
        assertThat(SuiviFichiers.exclu(requete)).isTrue();
    }
}
