package fr.bnia.referentiels;

import fr.bnia.support.AbstractIT;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.client.RestTestClient;

/**
 * Pays : chaîne complète. Les codes ISO sont uniques en base (RG-REF-06) : ce test réserve les codes
 * {@code XA} à {@code XZ}.
 */
class PaysIT extends AbstractIT {

    private static final String URL = "/api/referentiels/pays";
    private static final AtomicInteger ISO = new AtomicInteger();

    @Autowired
    RestTestClient client;

    private String zone;

    @BeforeEach
    void creerUneZone() {
        zone = "Z" + suffixe();
        client.post().uri("/api/referentiels/zones-commerciales").body(Map.of("code", zone, "libelle", "Zone " + zone))
                .exchange().expectStatus().isCreated();
    }

    @Test
    void creer_un_pays_avec_les_valeurs_par_defaut_puis_le_modifier() {
        String code = "P" + suffixe();
        String iso = isoUnique();

        client.post().uri(URL).body(pays(code, iso, zone))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.codeIso").isEqualTo(iso)
                .jsonPath("$.langueCertificat").isEqualTo("FR_EN")
                .jsonPath("$.niveauDetailCertificat").isEqualTo("CUMUL_DEGRE")
                .jsonPath("$.ageMinimum").doesNotExist();

        var modification = new HashMap<>(pays(code, iso.toLowerCase(), zone));
        modification.remove("code");
        modification.put("langueCertificat", "FR_ES");
        modification.put("niveauDetailCertificat", "DETAIL_COMPTE_AGE");
        modification.put("membreUe", true);
        modification.put("ageMinimum", "00");
        client.put().uri(URL + "/{code}", code).body(modification)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.codeIso").isEqualTo(iso)
                .jsonPath("$.langueCertificat").isEqualTo("FR_ES")
                .jsonPath("$.niveauDetailCertificat").isEqualTo("DETAIL_COMPTE_AGE")
                .jsonPath("$.membreUe").isEqualTo(true)
                .jsonPath("$.ageMinimum").isEqualTo("00");

        client.get().uri(u -> u.path(URL).queryParam("recherche", iso).build())
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$[?(@.code == '" + code + "')].ageMinimum").isEqualTo("00");
    }

    @Test
    @DisplayName("RG-REF-05 : un pays ne peut pas être rattaché à une zone inactive")
    void rg_ref_05_zone_inactive() {
        client.post().uri("/api/referentiels/zones-commerciales/{code}/desactivation", zone)
                .exchange().expectStatus().isOk();

        client.post().uri(URL).body(pays("P" + suffixe(), isoUnique(), zone))
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT)
                .expectBody().jsonPath("$.regle").isEqualTo("RG-REF-05");
    }

    @Test
    @DisplayName("RG-REF-06 : un code ISO déjà attribué est refusé en 409")
    void rg_ref_06_code_iso_en_double() {
        String iso = isoUnique();
        client.post().uri(URL).body(pays("P" + suffixe(), iso, zone)).exchange().expectStatus().isCreated();

        client.post().uri(URL).body(pays("P" + suffixe(), iso, zone))
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT)
                .expectBody().jsonPath("$.regle").isEqualTo("RG-REF-06");
    }

    @Test
    @DisplayName("RG-REF-09 : un âge minimum qui n'est pas un compte d'âge est refusé en 422")
    void rg_ref_09_age_minimum_invalide() {
        var saisie = new HashMap<>(pays("P" + suffixe(), isoUnique(), zone));
        saisie.put("ageMinimum", "VSOP");

        client.post().uri(URL).body(saisie)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT)
                .expectBody().jsonPath("$.regle").isEqualTo("RG-REF-09");
    }

    private static Map<String, Object> pays(String code, String iso, String zone) {
        return Map.of("code", code, "libelle", "Pays " + code, "codeIso", iso, "zoneCommerciale", zone,
                "membreUe", false);
    }

    private static String isoUnique() {
        return "X" + (char) ('A' + ISO.getAndIncrement());
    }

    private static String suffixe() {
        return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
