package fr.bnia.boot.suivi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import fr.bnia.operateurs.domain.partage.Role;
import fr.bnia.support.AbstractIT;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.test.web.servlet.client.RestTestClient;

/**
 * Suivi de l'utilisation : événements du front sous session, fichiers relevés par le serveur, export et remise à
 * zéro par jeton ({@code bnia.suivi.jeton} des tests).
 */
class SuiviIT extends AbstractIT {

    private static final String JETON = "Bearer jeton-des-tests";

    @Autowired
    JdbcClient jdbc;

    @Test
    @DisplayName("Un événement exige une session ; il est enregistré avec l'e-mail de l'utilisateur")
    void evenement_sous_session() {
        anonyme().post().uri("/api/suivi/evenements")
                .body(Map.of("type", "ECRAN", "lieu", "/bo", "appareil", "ORDINATEUR"))
                .exchange().expectStatus().isUnauthorized();

        String email = emailUnique("suivi");
        membre(operateurs.operateurBnia(), Role.OBSERVATEUR, email);
        RestTestClient session = connecte(email);
        session.post().uri("/api/suivi/evenements")
                .body(Map.of("type", "ECRAN", "lieu", "/bo/operateurs/$operateurId", "appareil", "TABLETTE"))
                .exchange().expectStatus().isNoContent();

        assertThat(jdbc.sql("select appareil || ' ' || type || ' ' || lieu from suivi.evenement where email = :email")
                .param("email", email).query(String.class).list())
                .containsExactly("TABLETTE ECRAN /bo/operateurs/$operateurId");
    }

    @Test
    @DisplayName("Un événement au lieu ou au type inconnu est refusé")
    void evenement_invalide() {
        client.post().uri("/api/suivi/evenements")
                .body(Map.of("type", "ECRAN", "lieu", "https://ailleurs.example", "appareil", "MOBILE"))
                .exchange().expectStatus().isBadRequest();
        client.post().uri("/api/suivi/evenements")
                .body(Map.of("type", "AUTRE", "lieu", "/bo", "appareil", "MOBILE"))
                .exchange().expectStatus().isBadRequest();
    }

    @Test
    @DisplayName("L'export exige le jeton ; un rapport inconnu n'existe pas")
    void export_par_jeton() {
        anonyme().get().uri("/api/suivi/export/visites").exchange().expectStatus().isUnauthorized();
        anonyme().get().uri("/api/suivi/export/visites").header("Authorization", "Bearer mauvais")
                .exchange().expectStatus().isUnauthorized();
        anonyme().get().uri("/api/suivi/export/inconnu").header("Authorization", JETON)
                .exchange().expectStatus().isNotFound();
    }

    @Test
    @DisplayName("La remise à zéro exige le jeton ; elle efface tout le suivi et renvoie le nombre d'événements effacés")
    void remise_a_zero() {
        String email = emailUnique("remise");
        evenement(email, OffsetDateTime.parse("2026-01-05T10:00:00Z"), "ECRAN", "/bo");

        anonyme().post().uri("/api/suivi/reset").exchange().expectStatus().isUnauthorized();
        anonyme().post().uri("/api/suivi/reset").header("Authorization", "Bearer mauvais")
                .exchange().expectStatus().isUnauthorized();
        assertThat(exporter("evenements")).contains(email);

        String effaces = anonyme().post().uri("/api/suivi/reset").header("Authorization", JETON)
                .exchange().expectStatus().isOk()
                .expectBody(String.class).returnResult().getResponseBody();
        assertThat(Integer.parseInt(effaces)).isPositive();
        assertThat(exporter("evenements")).doesNotContain(email);
    }

    @Test
    @DisplayName("Une pause de plus de 30 minutes ouvre une nouvelle visite, au parcours propre ; 90 s au plus par événement")
    void visites_et_temps_passe() {
        String email = emailUnique("visites");
        OffsetDateTime debut = OffsetDateTime.parse("2026-01-05T10:00:00Z");
        evenement(email, debut, "ECRAN", "/bo");
        fichier(email, debut.plusSeconds(30), "TELECHARGEMENT", "stocks.xlsx", 200);
        evenement(email, debut.plusMinutes(1), "OFFRE", "offre/01-synthese");
        evenement(email, debut.plusMinutes(45), "ECRAN", "/bo/operateurs");
        fichier(email, debut.plusMinutes(46), "IMPORT", "dsm.xlsx", 400);

        // 60 s, puis 90 s (44 min plafonnées), puis rien : 2,5 min, arrondies à 3. Les fichiers ne comptent pas.
        assertThat(exporter("personnes")).contains(email + ";ORDINATEUR;2;3;1;1;2026-01-05 11:00;2026-01-05 11:45");
        assertThat(exporter("visites").lines().filter(l -> l.startsWith(email + ";"))).containsExactly(
                email + ";ORDINATEUR;2026-01-05 11:00;2026-01-05 11:01;1;"
                        + "/bo (1.0) [téléchargement stocks.xlsx] > offre/01-synthese (1.5)",
                email + ";ORDINATEUR;2026-01-05 11:45;2026-01-05 11:45;0;/bo/operateurs (0.0) [import dsm.xlsx refusé 400]");
        assertThat(exporter("personnes?depuis=2999-01-01")).doesNotContain(email);
    }

    @Test
    @DisplayName("Les téléchargements (exports, offre en PDF) et les imports, même refusés, sont relevés par le serveur")
    void fichiers_releves_par_le_serveur() {
        client.get().uri("/api/declarations/stocks/excel").exchange().expectStatus().isOk();
        assertThat(dernierFichier("TELECHARGEMENT GET /api/declarations/stocks/excel ")).endsWith(".xlsx 200");

        client.get().uri("/api/offre/reponse-ao-armagnac-py-pollet.pdf").exchange().expectStatus().isOk();
        assertThat(dernierFichier("TELECHARGEMENT GET /api/offre/"))
                .isEqualTo("TELECHARGEMENT GET /api/offre/reponse-ao-armagnac-py-pollet.pdf reponse-ao-armagnac-py-pollet.pdf 200");

        MultiValueMap<String, Object> corps = new LinkedMultiValueMap<>();
        corps.add("fichier", new ByteArrayResource("pas un classeur".getBytes()) {
            @Override
            public String getFilename() {
                return "faux.xlsx";
            }
        });
        client.post().uri("/api/operateurs/" + nouvelOperateur() + "/point-de-depart/import")
                .contentType(MediaType.MULTIPART_FORM_DATA).body(corps).exchange()
                .expectStatus().is4xxClientError();
        assertThat(dernierFichier("IMPORT "))
                .isEqualTo("IMPORT POST /api/operateurs/{operateur}/point-de-depart/import faux.xlsx 422");

        // Une lecture en JSON n'est pas un fichier.
        client.get().uri("/api/declarations/stocks").exchange().expectStatus().isOk();
        assertThat(dernierFichier()).startsWith("IMPORT ");
    }

    /** Le serveur relève le fichier après l'envoi de la réponse : il arrive en base un instant après. */
    private String dernierFichier(String attendu) {
        String[] dernier = new String[1];
        await().atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> assertThat(dernier[0] = dernierFichier()).startsWith(attendu));
        return dernier[0];
    }

    private String dernierFichier() {
        return jdbc.sql("""
                        select type || ' ' || lieu || ' ' || detail || ' ' || statut from suivi.evenement
                        where email = :email and type in ('TELECHARGEMENT', 'IMPORT') order by id desc limit 1""")
                .param("email", ADMIN_BNIA).query(String.class).optional().orElse("");
    }

    private void fichier(String email, OffsetDateTime instant, String type, String nom, int statut) {
        jdbc.sql("""
                        insert into suivi.evenement (instant, session, email, appareil, type, lieu, detail, statut)
                        values (:instant, :session, :email, 'ORDINATEUR', :type, 'GET /api/x', :nom, :statut)""")
                .param("instant", instant).param("session", "it-" + email).param("email", email)
                .param("type", type).param("nom", nom).param("statut", statut)
                .update();
    }

    private void evenement(String email, OffsetDateTime instant, String type, String lieu) {
        jdbc.sql("""
                        insert into suivi.evenement (instant, session, email, appareil, type, lieu)
                        values (:instant, :session, :email, 'ORDINATEUR', :type, :lieu)""")
                .param("instant", instant).param("session", "it-" + email).param("email", email)
                .param("type", type).param("lieu", lieu)
                .update();
    }

    private String exporter(String rapport) {
        return anonyme().get().uri("/api/suivi/export/" + rapport).header("Authorization", JETON)
                .exchange().expectStatus().isOk()
                .expectBody(String.class).returnResult().getResponseBody();
    }
}
