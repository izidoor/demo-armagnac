package fr.bnia.referentiels;

import fr.bnia.support.AbstractIT;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.client.RestTestClient;

class ClasseTvaIT extends AbstractIT {

    private static final String URL = "/api/referentiels/classes-tva";

    private String code;
    private long id;

    @BeforeEach
    void creerUneClasse() {
        code = "T" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        id = creer(URL, Map.of("code", code, "libelle", "Classe " + code));
    }

    @Test
    void historique_des_taux_et_taux_en_vigueur() {
        ajouter("19.6", "2000-01-01", "2013-12-31").expectStatus().isOk();
        ajouter("20", "2014-01-01", null)
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.taux.length()").isEqualTo(2)
                .jsonPath("$.taux[0].dateDebut").isEqualTo("2014-01-01")
                .jsonPath("$.tauxEnVigueur.taux").isEqualTo(20.0);

        client.get().uri(u -> u.path(URL).queryParam("recherche", code).build())
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$[0].tauxEnVigueur").isEqualTo(20.0);

        client.put().uri(URL + "/{id}/taux/{debut}", id, "2014-01-01")
                .body(Map.of("taux", "20", "dateDebut", "2014-01-01", "dateFin", "2015-12-31"))
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.taux[0].dateFin").isEqualTo("2015-12-31").jsonPath("$.tauxEnVigueur").doesNotExist();

        client.delete().uri(URL + "/{id}/taux/{debut}", id, "2000-01-01")
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.taux.length()").isEqualTo(1);
    }

    @Test
    @DisplayName("RG-REF-11 : un chevauchement est refusé en 422, avec le taux en conflit")
    void rg_ref_11_chevauchement() {
        ajouter("20", "2014-01-01", null).expectStatus().isOk();

        ajouter("21", "2020-01-01", null)
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT)
                .expectBody()
                .jsonPath("$.regle").isEqualTo("RG-REF-11")
                .jsonPath("$.detail").isEqualTo(
                        "Le taux 21,00 % à partir du 01/01/2020 chevauche le taux 20,00 % à partir du 01/01/2014.");
    }

    @Test
    @DisplayName("RG-REF-13 : une classe inactive garde ses taux consultables")
    void rg_ref_13_classe_inactive() {
        ajouter("5.5", "2014-01-01", null).expectStatus().isOk();
        client.post().uri(URL + "/{id}/desactivation", id).exchange().expectStatus().isOk();

        client.get().uri(URL + "/{id}", id)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.actif").isEqualTo(false)
                .jsonPath("$.tauxEnVigueur.taux").isEqualTo(5.5);
    }

    @Test
    void une_classe_nait_sans_taux_et_se_supprime_avec_ses_taux() {
        client.get().uri(URL + "/{id}", id)
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.taux.length()").isEqualTo(0).jsonPath("$.tauxEnVigueur").doesNotExist();
        ajouter("20", "2014-01-01", null).expectStatus().isOk();

        client.delete().uri(URL + "/{id}", id).exchange().expectStatus().isNoContent();

        client.get().uri(URL + "/{id}", id).exchange().expectStatus().isNotFound();
    }

    @Test
    void retirer_un_taux_inconnu_renvoie_404() {
        client.delete().uri(URL + "/{id}/taux/{debut}", id, "2014-01-01")
                .exchange()
                .expectStatus().isNotFound();
    }

    private RestTestClient.ResponseSpec ajouter(String taux, String debut, String fin) {
        var saisie = new HashMap<String, Object>(Map.of("taux", taux, "dateDebut", debut));
        if (fin != null) {
            saisie.put("dateFin", fin);
        }
        return client.post().uri(URL + "/{id}/taux", id).body(saisie).exchange();
    }
}
