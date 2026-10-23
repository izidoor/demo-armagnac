package fr.bnia.contratsarmagnac;

import static org.assertj.core.api.Assertions.assertThat;

import fr.bnia.operateurs.domain.partage.Role;
import fr.bnia.support.AbstractIT;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
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

/**
 * Lot 3 de la spec 06 : PDF au formulaire CA-05 allégé, au portail et au back-office ; mercuriales. Le contrat : 9 hl
 * de millésime 2010 à 70 % (6,30 hl AP) à 1 750 € / hl AP, d'un négociant à un producteur.
 */
class EditionEtMercurialesIT extends AbstractIT {

    private long vendeur;
    private long acheteur;
    private RestTestClient jeanne;
    private RestTestClient henri;
    private long contrat;

    @BeforeEach
    void projet() {
        Map<String, Object> negociant = new HashMap<>();
        negociant.put("type", "NEGOCIANT");
        negociant.put("code", "T" + unique());
        negociant.put("raisonSociale", "Négoce " + unique());
        negociant.put("ville", "Condom");
        negociant.put("accordDiffusionGeneral", false);
        vendeur = creer("/api/operateurs", negociant);
        creer("/api/operateurs/" + vendeur + "/point-de-depart", Map.of("debut",
                LocalDate.now().withDayOfMonth(1).minusMonths(4), "lignes", List.of(
                        Map.of("cle", "MB", "millesime", 2010, "volume", new BigDecimal("640.25")))));
        acheteur = nouvelOperateur();
        String emailVendeur = emailUnique("jeanne");
        String emailAcheteur = emailUnique("henri");
        membre(vendeur, Role.ADMIN, emailVendeur);
        membre(acheteur, Role.AGENT, emailAcheteur);
        jeanne = connecte(emailVendeur);
        henri = connecte(emailAcheteur);
        Map<String, Object> ligne = new HashMap<>();
        ligne.put("appellation", "HAUT_ARMAGNAC");
        ligne.put("millesime", 2010);
        ligne.put("volume", new BigDecimal("9"));
        ligne.put("degre", new BigDecimal("70"));
        ligne.put("prix", new BigDecimal("1750"));
        contrat = creer(jeanne, "/api/operateurs/" + vendeur + "/contrats", Map.of("partie", "VENDEUR",
                "autrePartie", acheteur, "contenu", Map.of("lignes", List.of(ligne), "solde", "COMPTANT",
                        "conditionsRetiraison", "En une fois.")));
    }

    private static String texte(RestTestClient session, String uri, Object... variables) throws IOException {
        byte[] pdf = session.get().uri(uri, variables).exchange().expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_PDF)
                .expectHeader().valueMatches("Content-Disposition", "inline; filename=.*Contrat-CA-.*\\.pdf.*")
                .expectBody(byte[].class).returnResult().getResponseBody();
        try (PDDocument document = Loader.loadPDF(pdf)) {
            return new PDFTextStripper().getText(document);
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    @DisplayName("RG-CA-20, 19 : PDF « Projet, non conclu », puis conclu avec les signatures ; au journal ; au back-office")
    void rg_ca_20_pdf() throws IOException {
        String projet = texte(jeanne, "/api/operateurs/{o}/contrats/{id}/pdf", vendeur, contrat);
        assertThat(projet).contains("CONTRAT D'ACHAT D'ARMAGNAC", "Projet, non conclu", "Haut Armagnac", "2010",
                "6,30", "1 750,00", "11 025,00 €", "En une fois.", "S'agissant d'un contrat ponctuel, celui-ci est ferme.",
                "LE VENDEUR", "L'ACHETEUR", "Approuvé par");
        assertThat(projet).doesNotContain("Visa", "courtier", "Échéancier");

        henri.post().uri("/api/operateurs/{o}/contrats/{id}/approbation", acheteur, contrat)
                .body(Map.of("versionVue", 1)).exchange().expectStatus().isOk();
        String conclu = texte(client, "/api/contrats/{id}/pdf", contrat);
        assertThat(conclu).doesNotContain("Projet, non conclu").contains("Établi le " + LocalDate.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")));

        Map<?, ?> vu = jeanne.get().uri("/api/operateurs/{o}/contrats/{id}", vendeur, contrat).exchange()
                .expectStatus().isOk().expectBody(Map.class).returnResult().getResponseBody();
        assertThat((List<Map<?, ?>>) vu.get("journal")).extracting(e -> (Object) e.get("action"))
                .containsExactly("OUVERTURE", "EDITION_PDF", "APPROBATION", "CONCLUSION");
        henri.get().uri("/api/contrats/{id}/pdf", contrat).exchange().expectStatus().isForbidden();
    }

    @Test
    @SuppressWarnings("unchecked")
    @DisplayName("RG-CA-26 : les mercuriales comptent le contrat conclu, pas le projet ; réservées au BNIA")
    void rg_ca_26_mercuriales() {
        String aujourdhui = LocalDate.now().toString();
        Map<?, ?> avant = client.get().uri("/api/contrats/mercuriales?du={du}&au={au}", aujourdhui, aujourdhui)
                .exchange().expectStatus().isOk().expectBody(Map.class).returnResult().getResponseBody();
        int contratsAvant = ((List<Map<?, ?>>) avant.get("produits")).stream()
                .filter(p -> Integer.valueOf(2010).equals(p.get("millesime")))
                .mapToInt(p -> (Integer) p.get("contrats")).sum();
        henri.post().uri("/api/operateurs/{o}/contrats/{id}/approbation", acheteur, contrat)
                .body(Map.of("versionVue", 1)).exchange().expectStatus().isOk();

        Map<?, ?> apres = client.get().uri("/api/contrats/mercuriales").exchange().expectStatus().isOk()
                .expectBody(Map.class).returnResult().getResponseBody();

        assertThat(apres.get("au")).isEqualTo(aujourdhui);
        assertThat(apres.get("du")).isEqualTo(LocalDate.now().minusMonths(12).plusDays(1).toString());
        assertThat((List<Map<?, ?>>) apres.get("produits")).filteredOn(p -> Integer.valueOf(2010).equals(p.get("millesime")))
                .singleElement().satisfies(p -> {
                    assertThat(p.get("libelle")).isEqualTo("Millésime 2010");
                    assertThat((Integer) p.get("contrats")).isEqualTo(contratsAvant + 1);
                    assertThat(((Number) p.get("prixMinimum")).doubleValue()).isLessThanOrEqualTo(1750.0);
                    assertThat(((Number) p.get("prixMaximum")).doubleValue()).isGreaterThanOrEqualTo(1750.0);
                });
        henri.get().uri("/api/contrats/mercuriales").exchange().expectStatus().isForbidden();
    }
}
