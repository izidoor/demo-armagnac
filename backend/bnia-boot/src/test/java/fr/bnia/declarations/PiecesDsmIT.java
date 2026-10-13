package fr.bnia.declarations;

import static org.assertj.core.api.Assertions.assertThat;

import fr.bnia.operateurs.domain.partage.Role;
import fr.bnia.support.AbstractIT;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * Lot 3 de la spec 05 : justificatifs et DRM, déposés en PDF (multipart), téléchargés, supprimés ; manques
 * qui bloquent la validation ; stockage commun des documents (ADR-018).
 */
class PiecesDsmIT extends AbstractIT {

    private static final byte[] PDF = "%PDF-1.4\n% pièce de test\n%%EOF\n".getBytes(StandardCharsets.UTF_8);

    /** La campagne précédente : du 1er avril au 31 mars, déjà terminée ou en cours. */
    private static final LocalDate DEBUT = LocalDate.of(
            (LocalDate.now().getMonthValue() >= 4 ? LocalDate.now().getYear() : LocalDate.now().getYear() - 1) - 1, 4, 1);

    @Autowired JdbcClient jdbc;

    private long operateur;
    private RestTestClient agent;
    private String dsm;
    private Map<?, ?> vue;

    /** Un producteur, son point de départ, un agent connecté, sa DSM initiée avec 40 L exportés. */
    @BeforeEach
    void producteurAvecDsm() {
        operateur = nouvelOperateur();
        creer("/api/operateurs/" + operateur + "/point-de-depart", Map.of("debut", DEBUT, "lignes", List.of(
                Map.of("cle", "MB", "millesime", 2010, "volume", new BigDecimal("640.25")))));
        String email = emailUnique("agent");
        membre(operateur, Role.AGENT, email);
        agent = connecte(email);
        Map<?, ?> initiee = corps(agent.post().uri("/api/operateurs/" + operateur + "/dsm").exchange()
                .expectStatus().isCreated());
        dsm = "/api/operateurs/" + operateur + "/dsm/" + initiee.get("id");
        vue = corps(agent.put().uri(dsm).body(saisie(initiee, 22, "40")).exchange().expectStatus().isOk());
    }

    private static Map<?, ?> corps(RestTestClient.ResponseSpec reponse) {
        return reponse.expectBody(Map.class).returnResult().getResponseBody();
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> saisie(Map<?, ?> dsm, int colonne, String volume) {
        Map<String, Object> ligne = (Map<String, Object>) ((List<?>) dsm.get("lignes")).getFirst();
        List<Object> volumes = new ArrayList<>((List<Object>) ligne.get("volumes"));
        volumes.set(0, null);
        volumes.set(25, null);
        volumes.set(colonne - 1, new BigDecimal(volume));
        Map<String, Object> s = new HashMap<>(Map.of("id", ligne.get("id"), "cle", ligne.get("cle"),
                "millesime", ligne.get("millesime"), "compte", ligne.get("compte")));
        s.put("volumes", volumes);
        return Map.of("lignes", List.of(s), "version", dsm.get("version"));
    }

    private static MultiValueMap<String, Object> fichier(String nom, byte[] contenu) {
        HttpHeaders entete = new HttpHeaders();
        entete.setContentType(MediaType.APPLICATION_PDF);
        MultiValueMap<String, Object> corps = new LinkedMultiValueMap<>();
        corps.add("fichier", new HttpEntity<>(new ByteArrayResource(contenu) {
            @Override
            public String getFilename() {
                return nom;
            }
        }, entete));
        return corps;
    }

    private RestTestClient.ResponseSpec justificatif(RestTestClient session, String typeMouvement, String typePiece,
            byte[] contenu) {
        MultiValueMap<String, Object> corps = fichier("dae.pdf", contenu);
        corps.add("typeMouvement", typeMouvement);
        corps.add("typePiece", typePiece);
        corps.add("reference", "DAE-0001");
        return session.post().uri(dsm + "/justificatifs").contentType(MediaType.MULTIPART_FORM_DATA)
                .body(corps).exchange();
    }

    private RestTestClient.ResponseSpec drm(RestTestClient session, YearMonth mois) {
        return session.put().uri(dsm + "/drm/" + mois).contentType(MediaType.MULTIPART_FORM_DATA)
                .body(fichier("drm-" + mois + ".pdf", PDF)).exchange();
    }

    private static void regle(RestTestClient.ResponseSpec reponse, String regle) {
        reponse.expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT)
                .expectBody().jsonPath("$.regle").isEqualTo(regle);
    }

    private long documents() {
        return jdbc.sql("select count(*) from documents.document").query(Long.class).single();
    }

    @Test
    @DisplayName("RG-DEC-26 : dépôt d'un PDF, téléchargement en ligne, suppression ; non-PDF et type incompatible refusés")
    void rg_dec_26_justificatif() {
        assertThat((List<?>) vue.get("manques")).hasSize(13);
        justificatif(agent, "VENTE_EXPORT", "DAE", "pas un pdf".getBytes(StandardCharsets.UTF_8))
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT)
                .expectBody().jsonPath("$.detail").isEqualTo("Le fichier « dae.pdf » n'est pas un PDF.");
        regle(justificatif(agent, "VENTE_EXPORT", "AVOIR", PDF), "RG-DEC-26");

        Map<?, ?> depose = corps(justificatif(agent, "VENTE_EXPORT", "DAE", PDF).expectStatus().isCreated());

        assertThat(depose.get("reference")).isEqualTo("DAE-0001");
        assertThat(((Map<?, ?>) depose.get("document")).get("nom")).isEqualTo("dae.pdf");
        byte[] lu = client.get().uri(dsm + "/justificatifs/{id}/document", depose.get("id")).exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_PDF)
                .expectHeader().valueMatches("Content-Disposition", "inline; filename=.*dae\\.pdf.*")
                .expectBody(byte[].class).returnResult().getResponseBody();
        assertThat(lu).isEqualTo(PDF);
        Map<?, ?> pieces = corps(agent.get().uri(dsm + "/pieces").exchange().expectStatus().isOk());
        assertThat((List<?>) pieces.get("justificatifs")).hasSize(1);
        assertThat((List<?>) pieces.get("manques")).hasSize(12);

        long avant = documents();
        agent.delete().uri(dsm + "/justificatifs/{id}", depose.get("id")).exchange().expectStatus().isNoContent();
        assertThat(documents()).isEqualTo(avant - 1);
        client.get().uri(dsm + "/journal").exchange().expectStatus().isOk()
                .expectBody().jsonPath("$[0].action").isEqualTo("JUSTIFICATIF_SUPPRIME");
    }

    @Test
    @DisplayName("RG-DEC-27, 28, 29 : douze DRM attendues ; validation refusée tant qu'un mois manque ; le BNIA dépose en instruction")
    void rg_dec_29_validation() {
        justificatif(agent, "VENTE_EXPORT", "DAE", PDF).expectStatus().isCreated();
        YearMonth premier = YearMonth.from(DEBUT);
        for (int i = 0; i < 11; i++) {
            drm(agent, premier.plusMonths(i)).expectStatus().isNoContent();
        }
        regle(drm(agent, premier), "RG-DEC-27");
        regle(drm(agent, premier.plusMonths(12)), "RG-DEC-27");
        Map<?, ?> pieces = corps(agent.get().uri(dsm + "/pieces").exchange().expectStatus().isOk());
        assertThat((List<?>) pieces.get("mois")).hasSize(12);
        assertThat((List<?>) pieces.get("manques")).singleElement()
                .satisfies(m -> assertThat(((Map<?, ?>) m).get("mois")).isEqualTo(premier.plusMonths(11).toString()));

        agent.post().uri(dsm + "/envoi").body(Map.of()).exchange().expectStatus().isOk();
        regle(drm(agent, premier.plusMonths(11)), "RG-DEC-28");
        client.post().uri(dsm + "/prise-en-instruction").body(Map.of()).exchange().expectStatus().isOk();
        regle(client.post().uri(dsm + "/validation").body(Map.of()).exchange(), "RG-DEC-29");

        drm(client, premier.plusMonths(11)).expectStatus().isNoContent();
        client.post().uri(dsm + "/validation").body(Map.of()).exchange().expectStatus().isOk()
                .expectBody().jsonPath("$.etat").isEqualTo("VALIDEE");
        regle(client.delete().uri(dsm + "/drm/" + premier).exchange(), "RG-DEC-28");
        client.get().uri(dsm + "/drm/" + premier + "/document").exchange().expectStatus().isOk();
    }

    @Test
    @DisplayName("RG-DEC-01 : l'observateur consulte et télécharge, sans déposer")
    void rg_dec_01_observateur() {
        String email = emailUnique("observateur");
        membre(operateur, Role.OBSERVATEUR, email);
        RestTestClient observateur = connecte(email);
        drm(agent, YearMonth.from(DEBUT)).expectStatus().isNoContent();
        drm(observateur, YearMonth.from(DEBUT).plusMonths(1)).expectStatus().isForbidden();
        observateur.delete().uri(dsm + "/drm/" + YearMonth.from(DEBUT)).exchange().expectStatus().isForbidden();
        observateur.get().uri(dsm + "/drm/" + YearMonth.from(DEBUT) + "/document").exchange().expectStatus().isOk();
    }

    @Test
    @DisplayName("RG-DEC-22 : le brouillon supprimé emporte ses pièces et leurs documents")
    void rg_dec_22_suppression() {
        long avant = documents();
        justificatif(agent, "VENTE_EXPORT", "DAE", PDF).expectStatus().isCreated();
        drm(agent, YearMonth.from(DEBUT)).expectStatus().isNoContent();
        assertThat(documents()).isEqualTo(avant + 2);
        agent.delete().uri(dsm).exchange().expectStatus().isNoContent();
        assertThat(documents()).isEqualTo(avant);
    }
}
