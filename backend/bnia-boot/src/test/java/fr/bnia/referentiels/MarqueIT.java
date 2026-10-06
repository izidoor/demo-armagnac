package fr.bnia.referentiels;

import static org.assertj.core.api.Assertions.assertThat;

import fr.bnia.support.AbstractIT;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.client.RestTestClient;

/** Règles propres aux marques : RG-REF-14 et RG-REF-15. */
class MarqueIT extends AbstractIT {

    private static final String URL = "/api/referentiels/marques";

    @Autowired
    RestTestClient client;

    @Test
    @DisplayName("RG-REF-14 : la création de marque, commune au paramétrage et à la fiche opérateur, exige un code")
    void rg_ref_14_code_saisi_obligatoire() {
        client.post().uri(URL).body(Map.of("code", "", "libelle", "Marque " + suffixe()))
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT)
                .expectBody().jsonPath("$.regle").isEqualTo("RG-REF-01");
    }

    @Test
    @DisplayName("RG-REF-15 : un libellé déjà pris à la casse, aux accents et aux espaces près est refusé en 409")
    void rg_ref_15_libelle_en_double() {
        String suffixe = suffixe();
        creer("A" + suffixe, "Château " + suffixe);

        client.post().uri(URL).body(Map.of("code", "B" + suffixe, "libelle", "  CHATEAU   " + suffixe.toLowerCase()))
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT)
                .expectBody()
                .jsonPath("$.regle").isEqualTo("RG-REF-15")
                .jsonPath("$.detail").isEqualTo("La marque « Château " + suffixe + " » (code A" + suffixe + ") existe déjà.");
    }

    @Test
    @DisplayName("RG-REF-15 : une marque inactive bloque son libellé et invite à la réactiver")
    void rg_ref_15_inactive_bloque_son_libelle() {
        String suffixe = suffixe();
        creer("A" + suffixe, "Domaine " + suffixe);
        client.post().uri(URL + "/{code}/desactivation", "A" + suffixe).exchange().expectStatus().isOk();

        client.post().uri(URL).body(Map.of("code", "B" + suffixe, "libelle", "Domaine " + suffixe))
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT)
                .expectBody().jsonPath("$.detail").value(String.class,
                        detail -> assertThat(detail).contains("réactivez-la"));
    }

    @Test
    @DisplayName("RG-REF-15 : les marques au libellé proche sont proposées, accents et casse ignorés")
    void rg_ref_15_marques_proches() {
        String suffixe = suffixe();
        creer("A" + suffixe, "Château Lestang " + suffixe);
        creer("B" + suffixe, "Domaine Lestang " + suffixe);
        creer("C" + suffixe, "Autre " + suffixe);

        client.get().uri(u -> u.path(URL + "/proches").queryParam("libelle", "lestang " + suffixe.toLowerCase()).build())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(2)
                .jsonPath("$[0].code").isEqualTo("A" + suffixe)
                .jsonPath("$[1].code").isEqualTo("B" + suffixe);

        client.get().uri(u -> u.path(URL + "/proches").queryParam("libelle", "chÂteau lestang " + suffixe).build())
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.length()").isEqualTo(1);
    }

    private void creer(String code, String libelle) {
        client.post().uri(URL).body(Map.of("code", code, "libelle", libelle))
                .exchange()
                .expectStatus().isCreated();
    }

    private static String suffixe() {
        return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
