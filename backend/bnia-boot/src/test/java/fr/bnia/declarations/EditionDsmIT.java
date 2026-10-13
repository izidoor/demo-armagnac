package fr.bnia.declarations;

import static org.assertj.core.api.Assertions.assertThat;

import fr.bnia.operateurs.domain.partage.Role;
import fr.bnia.support.AbstractIT;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.client.RestTestClient;

/** Lot 5 de la spec 05 : le PDF de la DSM, lu par PDFBox (RG-DEC-35). */
class EditionDsmIT extends AbstractIT {

    private long operateur;
    private RestTestClient agent;
    private String dsms;

    /** Le taux de 2024 de la démonstration, s'il manque : la page « Cotisation » est alors calculée. */
    @BeforeEach
    void tauxCvo() {
        List<?> grille = client.get().uri("/api/declarations/taux-cvo").exchange().expectStatus().isOk()
                .expectBody(List.class).returnResult().getResponseBody();
        if (grille.stream().noneMatch(t -> "2024-01-01".equals(((Map<?, ?>) t).get("debut")))) {
            client.post().uri("/api/declarations/taux-cvo").body(Map.of("montant", new BigDecimal("60.00"),
                    "pourcentageExonere", new BigDecimal("20.00"), "debut", "2024-01-01")).exchange().expectStatus().isOk();
        }
    }

    /** Un négociant au mois courant, un agent connecté, sa DSM avec ces volumes sur la première ligne. */
    private Map<?, ?> dsm(Map<Integer, String> volumes) {
        Map<String, Object> saisie = new HashMap<>();
        saisie.put("type", "NEGOCIANT");
        saisie.put("code", "T" + unique());
        saisie.put("raisonSociale", "Négoce " + unique());
        saisie.put("ville", "Condom");
        saisie.put("accordDiffusionGeneral", false);
        operateur = creer("/api/operateurs", saisie);
        creer("/api/operateurs/" + operateur + "/point-de-depart", Map.of("debut", LocalDate.now().withDayOfMonth(1),
                "lignes", List.of(Map.of("cle", "NB", "denomination", "XO", "compte", "12",
                        "volume", new BigDecimal("4000")))));
        String email = emailUnique("agent");
        membre(operateur, Role.AGENT, email);
        agent = connecte(email);
        dsms = "/api/operateurs/" + operateur + "/dsm";
        Map<?, ?> dsm = corps(agent.post().uri(dsms).exchange().expectStatus().isCreated());
        return corps(agent.put().uri(dsms + "/{id}", dsm.get("id")).body(saisie(dsm, volumes)).exchange()
                .expectStatus().isOk());
    }

    private static Map<?, ?> corps(RestTestClient.ResponseSpec reponse) {
        return reponse.expectBody(Map.class).returnResult().getResponseBody();
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> saisie(Map<?, ?> dsm, Map<Integer, String> volumesPremiereLigne) {
        Map<String, Object> ligne = (Map<String, Object>) ((List<?>) dsm.get("lignes")).getFirst();
        List<Object> volumes = new ArrayList<>((List<Object>) ligne.get("volumes"));
        volumes.set(0, null);
        volumes.set(25, null);
        volumesPremiereLigne.forEach((colonne, litres) -> volumes.set(colonne - 1, new BigDecimal(litres)));
        Map<String, Object> s = new HashMap<>(Map.of("id", ligne.get("id"), "cle", ligne.get("cle"),
                "denomination", ligne.get("denomination"), "compte", ligne.get("compte")));
        s.put("volumes", volumes);
        return Map.of("lignes", List.of(s), "version", dsm.get("version"));
    }

    /** Le texte du PDF de la DSM, lu par la session donnée. */
    private String texte(RestTestClient session, Object id) throws IOException {
        byte[] pdf = session.get().uri(dsms + "/{id}/pdf", id).exchange().expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_PDF)
                .expectHeader().valueMatches("Content-Disposition", "inline; filename=.*DSM-T.*\\.pdf.*")
                .expectBody(byte[].class).returnResult().getResponseBody();
        try (PDDocument document = Loader.loadPDF(pdf)) {
            assertThat(document.getNumberOfPages()).as("cinq onglets et la cotisation").isGreaterThanOrEqualTo(6);
            return new PDFTextStripper().getText(document);
        }
    }

    @Test
    @DisplayName("RG-DEC-35 : PDF provisoire avant validation, au format unique, volumes en hl.l,cl, cotisation ; au journal")
    void rg_dec_35_provisoire() throws IOException {
        Map<?, ?> dsm = dsm(Map.of(17, "719.11", 21, "120.00", 22, "954.15", 23, "1300.00"));

        String texte = texte(agent, dsm.get("id"));

        assertThat(texte).contains("Document provisoire, DSM non validée", "Récapitulatif", "Ventes à l'exportation",
                "Brouillon", "7.19,11", "22.54,15", "Sorties France", "1 855,95 €");
        client.get().uri(dsms + "/{id}/journal", dsm.get("id")).exchange().expectStatus().isOk()
                .expectBody().jsonPath("$[0].action").isEqualTo("DSM_EDITEE")
                .jsonPath("$[0].detail").isEqualTo("PDF");
    }

    @Test
    @DisplayName("RG-DEC-35, 01 : validée, plus de mention provisoire ; l'observateur l'édite, un étranger non")
    void rg_dec_35_validee() throws IOException {
        Map<?, ?> dsm = dsm(Map.of(17, "100.00"));
        Object id = dsm.get("id");
        agent.post().uri(dsms + "/{id}/envoi", id).body(Map.of()).exchange().expectStatus().isOk();
        client.post().uri(dsms + "/{id}/prise-en-instruction", id).body(Map.of()).exchange().expectStatus().isOk();
        client.post().uri(dsms + "/{id}/validation", id).body(Map.of()).exchange().expectStatus().isOk();
        String email = emailUnique("observateur");
        membre(operateur, Role.OBSERVATEUR, email);

        String texte = texte(connecte(email), id);

        assertThat(texte).doesNotContain("provisoire").contains("Validée", "1.00,00");
        String etranger = emailUnique("etranger");
        membre(nouvelOperateur(), Role.ADMIN, etranger);
        connecte(etranger).get().uri(dsms + "/{id}/pdf", id).exchange().expectStatus().isForbidden();
    }
}
