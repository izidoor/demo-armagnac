package fr.bnia.declarations;

import static org.assertj.core.api.Assertions.assertThat;

import fr.bnia.operateurs.domain.partage.Role;
import fr.bnia.support.AbstractIT;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.dhatim.fastexcel.Workbook;
import org.dhatim.fastexcel.Worksheet;
import org.dhatim.fastexcel.reader.ReadableWorkbook;
import org.dhatim.fastexcel.reader.ReadingOptions;
import org.dhatim.fastexcel.reader.Row;
import org.dhatim.fastexcel.reader.Sheet;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/** Lot 6 de la spec 05 : le modèle Excel, édité puis réimporté ; import refusé ; stock initial (RG-DEC-36 à 38). */
class ExcelDsmIT extends AbstractIT {

    private static final String XLSX = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    private static final YearMonth MOIS = YearMonth.now();

    private long operateur;
    private RestTestClient agent;
    private String dsms;

    /** Un négociant au mois courant, sans point de départ, et son agent connecté. */
    private void negociant() {
        Map<String, Object> saisie = new HashMap<>();
        saisie.put("type", "NEGOCIANT");
        saisie.put("code", "X" + unique());
        saisie.put("raisonSociale", "Négoce " + unique());
        saisie.put("ville", "Eauze");
        saisie.put("accordDiffusionGeneral", false);
        operateur = creer("/api/operateurs", saisie);
        String email = emailUnique("agent");
        membre(operateur, Role.AGENT, email);
        agent = connecte(email);
        dsms = "/api/operateurs/" + operateur + "/dsm";
    }

    /** Le négociant, son point de départ (XO NB 12, 4 000 L), sa DSM avec ces volumes sur la ligne XO. */
    private Map<?, ?> dsm(Map<Integer, String> volumes) {
        negociant();
        creer("/api/operateurs/" + operateur + "/point-de-depart", Map.of("debut", MOIS.atDay(1),
                "lignes", List.of(Map.of("cle", "NB", "denomination", "XO", "compte", "12",
                        "volume", new BigDecimal("4000")))));
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

    /** Le volume d'une colonne de la ligne {@code rang} de la DSM. */
    private static BigDecimal volume(Map<?, ?> dsm, int rang, int colonne) {
        Map<?, ?> ligne = (Map<?, ?>) ((List<?>) dsm.get("lignes")).get(rang);
        Object volume = ((List<?>) ligne.get("volumes")).get(colonne - 1);
        return volume == null ? BigDecimal.ZERO : new BigDecimal(volume.toString()).stripTrailingZeros();
    }

    private static MultiValueMap<String, Object> fichier(String nom, byte[] contenu) {
        HttpHeaders entete = new HttpHeaders();
        entete.setContentType(MediaType.parseMediaType(XLSX));
        MultiValueMap<String, Object> corps = new LinkedMultiValueMap<>();
        corps.add("fichier", new HttpEntity<>(new ByteArrayResource(contenu) {
            @Override
            public String getFilename() {
                return nom;
            }
        }, entete));
        return corps;
    }

    private RestTestClient.ResponseSpec importer(RestTestClient session, Object id, String nom, byte[] contenu) {
        return session.post().uri(dsms + "/{id}/import", id).contentType(MediaType.MULTIPART_FORM_DATA)
                .body(fichier(nom, contenu)).exchange();
    }

    /**
     * Un modèle écrit à la main, aux positions de la matrice négociant du BNIA : période en A8, numéros de colonne en
     * ligne 17 du récapitulatif (8 des autres onglets, 7 de l'export), lignes à partir de 18 (9, 8).
     *
     * @param lignes désignation, clé, compte, puis les volumes par numéro de colonne
     */
    private static byte[] modele(YearMonth mois, List<Object[]> lignes) throws IOException {
        ByteArrayOutputStream sortie = new ByteArrayOutputStream();
        Workbook classeur = new Workbook(sortie, "test", "1.0");
        String[] noms = {"RECAPITULATIF", "MOUVEMENTS ENTREE", "MVMTS SORTIES & FABRICATIONS", "VENTES FRANCE",
                "VENTES EXPORT & VINS VINES"};
        int[] numeros = {16, 7, 7, 7, 6};
        List<Worksheet> feuilles = new ArrayList<>();
        for (int f = 0; f < noms.length; f++) {
            feuilles.add(classeur.newWorksheet(noms[f]));
        }
        if (mois != null) {
            feuilles.getFirst().value(7, 0, mois.atDay(1));
        }
        for (int n = 1; n <= 26; n++) {
            int[] position = position(n);
            feuilles.get(position[0]).value(numeros[position[0]], position[1], n);
        }
        for (int i = 0; i < lignes.size(); i++) {
            Object[] ligne = lignes.get(i);
            Worksheet recap = feuilles.getFirst();
            for (int c = 0; c < 3; c++) {
                if (ligne[c] instanceof Number nombre) {
                    recap.value(17 + i, c, nombre);
                } else if (ligne[c] != null) {
                    recap.value(17 + i, c, ligne[c].toString());
                }
            }
            @SuppressWarnings("unchecked")
            Map<Integer, Object> volumes = (Map<Integer, Object>) ligne[3];
            for (Map.Entry<Integer, Object> e : volumes.entrySet()) {
                int[] position = position(e.getKey());
                Worksheet feuille = feuilles.get(position[0]);
                int rangee = numeros[position[0]] + 1 + i;
                if (e.getValue() instanceof Number nombre) {
                    feuille.value(rangee, position[1], nombre);
                } else {
                    feuille.value(rangee, position[1], e.getValue().toString());
                }
            }
        }
        feuilles.getFirst().value(17 + lignes.size(), 0, "Total");
        classeur.finish();
        return sortie.toByteArray();
    }

    /** Les colonnes masquées d'une feuille (numérotées à partir de 1), lues dans son XML. */
    private static List<Integer> colonnesMasquees(byte[] classeur, String feuille) throws IOException {
        // ZipFile, pas ZipInputStream : fastexcel écrit ses entrées en flux, que ZipInputStream refuse.
        java.nio.file.Path fichier = java.nio.file.Files.createTempFile("dsm", ".xlsx");
        try {
            java.nio.file.Files.write(fichier, classeur);
            try (var zip = new java.util.zip.ZipFile(fichier.toFile())) {
                var entree = zip.getEntry(feuille);
                assertThat(entree).as("feuille " + feuille).isNotNull();
                String xml = new String(zip.getInputStream(entree).readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
                return java.util.regex.Pattern.compile("<col [^>]*>").matcher(xml).results()
                        .map(java.util.regex.MatchResult::group)
                        .filter(col -> col.contains("hidden=\"true\"") || col.contains("hidden=\"1\""))
                        .map(col -> Integer.valueOf(col.replaceAll(".*min=\"(\\d+)\".*", "$1")))
                        .toList();
            }
        } finally {
            java.nio.file.Files.deleteIfExists(fichier);
        }
    }

    /** L'onglet et l'index de colonne (à partir de 0) d'une colonne de DSM dans la matrice négociant. */
    private static int[] position(int n) {
        if (n <= 3) {
            return new int[] {0, n + 2};
        }
        if (n == 26) {
            return new int[] {0, 8};
        }
        if (n <= 9) {
            return new int[] {1, n - 1};
        }
        if (n <= 16) {
            return new int[] {2, n - 7};
        }
        if (n <= 21) {
            return new int[] {3, n - 14};
        }
        return new int[] {4, n - 19};
    }

    @Test
    @DisplayName("RG-DEC-36 : l'Excel reprend la matrice négociant (onglets, numéros, format hl.l,cl, cotisation) "
            + "et se réimporte à l'identique ; édition et import au journal")
    void rg_dec_36_aller_retour() throws IOException {
        Map<?, ?> dsm = dsm(Map.of(17, "719.11", 22, "954.15"));
        Object id = dsm.get("id");

        byte[] excel = agent.get().uri(dsms + "/{id}/excel", id).exchange().expectStatus().isOk()
                .expectHeader().contentType(XLSX)
                .expectHeader().valueMatches("Content-Disposition", "attachment; filename=.*DSM-X.*\\.xlsx.*")
                .expectBody(byte[].class).returnResult().getResponseBody();

        try (ReadableWorkbook classeur = new ReadableWorkbook(new ByteArrayInputStream(excel), new ReadingOptions(true, false))) {
            List<Sheet> feuilles = classeur.getSheets().toList();
            assertThat(feuilles).extracting(Sheet::getName).containsExactly("RECAPITULATIF", "MOUVEMENTS ENTREE",
                    "MVMTS SORTIES & FABRICATIONS", "VENTES FRANCE", "VENTES EXPORT & VINS VINES", "COTISATION");
            List<Row> recap = feuilles.getFirst().read();
            Row ligne18 = recap.stream().filter(r -> r.getRowNum() == 18).findFirst().orElseThrow();
            assertThat(ligne18.getCellText(0)).isEqualTo("XO");
            assertThat(ligne18.getCellText(1)).isEqualTo("NB");
            assertThat(ligne18.getCellText(2)).isEqualTo("12");
            assertThat(ligne18.getCellAsNumber(3)).hasValueSatisfying(v -> assertThat(v).isEqualByComparingTo("4000"));
            Row france = feuilles.get(3).read().stream().filter(r -> r.getRowNum() == 9).findFirst().orElseThrow();
            assertThat(france.getCellAsNumber(3)).hasValueSatisfying(v -> assertThat(v).isEqualByComparingTo("719.11"));
            assertThat(france.getCell(3).getDataFormatString()).isEqualTo("#0\\.00.00");
            assertThat(recap.stream().filter(r -> r.getRowNum() == 17).findFirst().orElseThrow().getCellAsNumber(8))
                    .hasValueSatisfying(v -> assertThat(v).isEqualByComparingTo("26"));
            assertThat(recap.stream().filter(r -> r.getRowNum() == 3).findFirst().orElseThrow().getCellText(0))
                    .isEqualTo("Document provisoire, DSM non validée");
        }

        // Comme dans la matrice du BNIA, les copies des colonnes 12 et 11 (K et L des entrées) sont masquées.
        assertThat(colonnesMasquees(excel, "xl/worksheets/sheet2.xml")).containsExactly(11, 12);

        Map<?, ?> importee = corps(importer(agent, id, "retour.xlsx", excel).expectStatus().isOk());

        Map<?, ?> apres = (Map<?, ?>) importee.get("dsm");
        assertThat(volume(apres, 0, 17)).isEqualByComparingTo("719.11");
        assertThat(volume(apres, 0, 22)).isEqualByComparingTo("954.15");
        assertThat(volume(apres, 0, 26)).isEqualByComparingTo("2326.74");
        assertThat((List<?>) importee.get("avertissements")).isEmpty();
        client.get().uri(dsms + "/{id}/journal", id).exchange().expectStatus().isOk()
                .expectBody().jsonPath("$[0].action").isEqualTo("DSM_IMPORTEE")
                .jsonPath("$[0].detail").isEqualTo("retour.xlsx")
                .jsonPath("$[1].action").isEqualTo("DSM_EDITEE")
                .jsonPath("$[1].detail").isEqualTo("Excel");
    }

    @Test
    @DisplayName("RG-DEC-38 : le modèle du BNIA rempli à la main remplace les colonnes 2 à 25 ; ligne nouvelle ajoutée")
    void rg_dec_38_modele_rempli() throws IOException {
        Map<?, ?> dsm = dsm(Map.of(17, "719.11", 22, "954.15"));
        byte[] rempli = modele(MOIS, List.of(
                new Object[] {"XO", "NB", "12", Map.of(17, 500)},
                new Object[] {"VSOP", "NV", 5, Map.of(4, "1 000,50", 22, 100)}));

        Map<?, ?> importee = corps(importer(agent, dsm.get("id"), "rempli.xlsx", rempli).expectStatus().isOk());

        Map<?, ?> apres = (Map<?, ?>) importee.get("dsm");
        assertThat((List<?>) apres.get("lignes")).hasSize(2);
        assertThat(volume(apres, 0, 17)).isEqualByComparingTo("500");
        assertThat(volume(apres, 0, 22)).isZero();
        assertThat(volume(apres, 1, 4)).isEqualByComparingTo("1000.50");
        assertThat(volume(apres, 1, 22)).isEqualByComparingTo("100");
    }

    @Test
    @DisplayName("RG-DEC-37 : un fichier en erreur est refusé, erreurs situées ; un fichier qui n'est pas un classeur "
            + "aussi ; rien n'est importé")
    void rg_dec_37_refus() throws IOException {
        Map<?, ?> dsm = dsm(Map.of(17, "719.11"));
        Object id = dsm.get("id");
        byte[] errone = modele(MOIS.minusMonths(1), List.<Object[]>of(
                new Object[] {"XO", "XX", "12", Map.of()},
                new Object[] {"VSOP", "NB", "5", Map.of(17, "beaucoup")}));

        importer(agent, id, "errone.xlsx", errone).expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT)
                .expectBody()
                .jsonPath("$.regle").isEqualTo("RG-DEC-37")
                .jsonPath("$.detail").isEqualTo("Le fichier est refusé (3 erreurs) : rien n'est importé.")
                .jsonPath("$.erreurs.length()").isEqualTo(3)
                .jsonPath("$.erreurs[0].onglet").isEqualTo("Récapitulatif")
                .jsonPath("$.erreurs[0].ligne").isEqualTo(8)
                .jsonPath("$.erreurs[0].colonne").isEqualTo("A")
                .jsonPath("$.erreurs[1].colonne").isEqualTo("B")
                .jsonPath("$.erreurs[2].onglet").isEqualTo("Ventes France")
                .jsonPath("$.erreurs[2].ligne").isEqualTo(10)
                .jsonPath("$.erreurs[2].colonne").isEqualTo("D")
                .jsonPath("$.erreurs[2].message").isEqualTo("« beaucoup » n'est pas un volume (colonne 17).");
        importer(agent, id, "piece.pdf", "%PDF-1.4".getBytes()).expectStatus()
                .isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT).expectBody()
                .jsonPath("$.regle").isEqualTo("RG-DEC-37")
                .jsonPath("$.detail").isEqualTo("Le fichier n'est pas un classeur Excel (.xlsx) lisible.");

        Map<?, ?> apres = corps(agent.get().uri(dsms + "/{id}", id).exchange().expectStatus().isOk());
        assertThat(volume(apres, 0, 17)).isEqualByComparingTo("719.11");
    }

    @Test
    @DisplayName("RG-DEC-01 : seul l'opérateur importe sa DSM ; seul le BNIA importe un stock initial")
    void rg_dec_01_refus() throws IOException {
        Map<?, ?> dsm = dsm(Map.of());
        byte[] vide = modele(MOIS, List.of());

        importer(client, dsm.get("id"), "bnia.xlsx", vide).expectStatus().isForbidden();
        agent.post().uri("/api/operateurs/" + operateur + "/point-de-depart/import")
                .contentType(MediaType.MULTIPART_FORM_DATA).body(fichier("stock.xlsx", vide)).exchange()
                .expectStatus().isForbidden();
    }

    @Test
    @DisplayName("RG-DEC-38 : le BNIA importe le stock de fin du mois précédent ; le point de départ est fixé au "
            + "premier du mois, la DSM s'initie avec ce stock")
    void rg_dec_38_stock_initial() throws IOException {
        negociant();
        byte[] precedent = modele(MOIS.minusMonths(1), List.of(
                new Object[] {"XO", "NB", "12", Map.of(1, 1000, 4, 200, 17, 100)},
                new Object[] {"VSOP", "NV", "5", Map.of(1, 50, 17, 50)}));

        Map<?, ?> importe = corps(client.post().uri("/api/operateurs/" + operateur + "/point-de-depart/import")
                .contentType(MediaType.MULTIPART_FORM_DATA).body(fichier("stock.xlsx", precedent)).exchange()
                .expectStatus().isOk());

        Map<?, ?> point = (Map<?, ?>) importe.get("point");
        assertThat(point.get("debut")).isEqualTo(MOIS.atDay(1).toString());
        assertThat((List<?>) point.get("lignes")).singleElement().satisfies(l -> {
            assertThat(((Map<?, ?>) l).get("denomination")).isEqualTo("XO");
            assertThat(new BigDecimal(((Map<?, ?>) l).get("volume").toString())).isEqualByComparingTo("1100");
        });
        Map<?, ?> dsm = corps(agent.post().uri(dsms).exchange().expectStatus().isCreated());
        assertThat(volume(dsm, 0, 1)).isEqualByComparingTo("1100");
        client.get().uri("/api/operateurs/" + operateur + "/declarations/journal").exchange().expectStatus().isOk()
                .expectBody().jsonPath("$[1].action").isEqualTo("POINT_DE_DEPART_IMPORTE")
                .jsonPath("$[1].detail").isEqualTo("stock.xlsx : Début au "
                        + MOIS.atDay(1).format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                        + ", 1 ligne de stock initial");
    }
}
