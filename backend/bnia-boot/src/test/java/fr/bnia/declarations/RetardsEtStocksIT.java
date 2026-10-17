package fr.bnia.declarations;

import static org.assertj.core.api.Assertions.assertThat;

import fr.bnia.declarations.application.DetecterRetards;
import fr.bnia.operateurs.domain.partage.Role;
import fr.bnia.support.AbstractIT;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.dhatim.fastexcel.reader.ReadableWorkbook;
import org.dhatim.fastexcel.reader.Row;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.web.servlet.client.RestTestClient;

/**
 * Lots 7 et 8 de la spec 05 : échéances, périodes absentes et retards, détection planifiée, nouveau point de
 * départ après une interruption ; stocks déclarés et leur export. La détection est appelée directement : le
 * traitement planifié est coupé dans les IT (ADR-019).
 */
class RetardsEtStocksIT extends AbstractIT {

    /** Le premier du mois, quatre mois avant le mois courant : l'échéance de sa période est passée. */
    private static final LocalDate DEBUT = LocalDate.now().withDayOfMonth(1).minusMonths(4);

    @Autowired
    private DetecterRetards detecter;
    @Autowired
    private JdbcClient jdbcClient;

    private long operateur;
    private RestTestClient agent;
    private String dsms;

    /** Un négociant, son point de départ au {@link #DEBUT}, un agent connecté. */
    private void negociant() {
        Map<String, Object> saisie = new HashMap<>();
        saisie.put("type", "NEGOCIANT");
        saisie.put("code", "R" + unique());
        saisie.put("raisonSociale", "Négoce " + unique());
        saisie.put("ville", "Condom");
        saisie.put("accordDiffusionGeneral", false);
        operateur = creer("/api/operateurs", saisie);
        creer("/api/operateurs/" + operateur + "/point-de-depart", Map.of("debut", DEBUT, "lignes", List.of(
                Map.of("cle", "NB", "denomination", "XO", "compte", "12", "volume", new BigDecimal("1500")),
                Map.of("cle", "MB", "millesime", 2010, "volume", new BigDecimal("640.25")))));
        String email = emailUnique("agent");
        membre(operateur, Role.AGENT, email);
        agent = connecte(email);
        dsms = "/api/operateurs/" + operateur + "/dsm";
    }

    private static Map<?, ?> corps(RestTestClient.ResponseSpec reponse) {
        return reponse.expectBody(Map.class).returnResult().getResponseBody();
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> liste(RestTestClient.ResponseSpec reponse) {
        return reponse.expectBody(List.class).returnResult().getResponseBody();
    }

    /** Les lignes du listing du BNIA pour l'opérateur du test. */
    private List<Map<String, Object>> listing() {
        return liste(client.get().uri("/api/declarations/dsm?operateur={o}", operateur).exchange().expectStatus().isOk());
    }

    /** La DSM initiée, 500 L de XO vendus en France (colonne 17), envoyée, prise en charge et validée. */
    @SuppressWarnings("unchecked")
    private Object dsmValidee() {
        Map<?, ?> dsm = corps(agent.post().uri(dsms).exchange().expectStatus().isCreated());
        Object id = dsm.get("id");
        List<Map<String, Object>> lignes = new ArrayList<>();
        for (Map<String, Object> ligne : (List<Map<String, Object>>) dsm.get("lignes")) {
            List<Object> volumes = new ArrayList<>((List<Object>) ligne.get("volumes"));
            volumes.set(0, null);
            volumes.set(25, null);
            if (lignes.isEmpty()) {
                volumes.set(16, new BigDecimal("500"));
            }
            Map<String, Object> s = new HashMap<>(ligne);
            s.put("volumes", volumes);
            lignes.add(s);
        }
        agent.put().uri(dsms + "/{id}", id).body(Map.of("lignes", lignes, "version", 0)).exchange()
                .expectStatus().isOk();
        agent.post().uri(dsms + "/{id}/envoi", id).body(Map.of("version", 1)).exchange().expectStatus().isOk();
        client.post().uri(dsms + "/{id}/prise-en-instruction", id).body(Map.of("version", 2)).exchange()
                .expectStatus().isOk();
        client.post().uri(dsms + "/{id}/validation", id).body(Map.of("version", 3)).exchange().expectStatus().isOk();
        return id;
    }

    @Test
    @DisplayName("RG-DEC-39, 40 : la période absente au listing, puis la DSM en brouillon, en retard après l'échéance")
    void rg_dec_40_absente_et_en_retard() {
        negociant();
        String echeance = DEBUT.plusMonths(1).withDayOfMonth(5).toString();
        assertThat(listing()).singleElement().satisfies(l -> {
            assertThat(l.get("absente")).isEqualTo(true);
            assertThat(l.get("id")).isNull();
            assertThat(l.get("libelleEtat")).isEqualTo("Absente");
            assertThat(l.get("echeance")).isEqualTo(echeance);
            assertThat(l.get("enRetard")).isEqualTo(true);
        });

        Map<?, ?> dsm = corps(agent.post().uri(dsms).exchange().expectStatus().isCreated());
        assertThat(dsm.get("echeance")).isEqualTo(echeance);
        assertThat(dsm.get("enRetard")).isEqualTo(true);
        assertThat(listing()).singleElement().satisfies(l -> {
            assertThat(l.get("id")).isEqualTo(dsm.get("id"));
            assertThat(l.get("absente")).isEqualTo(false);
            assertThat(l.get("enRetard")).isEqualTo(true);
        });
        agent.get().uri(dsms + "/situation").exchange().expectStatus().isOk()
                .expectBody().jsonPath("$.echeance").isEqualTo(echeance).jsonPath("$.enRetard").isEqualTo(true);
        agent.get().uri(dsms).exchange().expectStatus().isOk()
                .expectBody().jsonPath("$[0].echeance").isEqualTo(echeance);

        agent.post().uri(dsms + "/{id}/envoi", dsm.get("id")).body(Map.of("version", 0)).exchange()
                .expectStatus().isOk();
        assertThat(listing()).singleElement().satisfies(l -> assertThat(l.get("enRetard")).isEqualTo(false));
    }

    @Test
    @DisplayName("RG-DEC-41 : la détection note le retard une seule fois par période")
    void rg_dec_41_detection_unique() {
        negociant();
        detecter.detecter();
        detecter.detecter();
        assertThat(jdbcClient.sql("select debut from declarations.retard_signale where operateur_id = :o")
                .param("o", operateur).query(LocalDate.class).list()).containsExactly(DEBUT);
    }

    @Test
    @DisplayName("RG-DEC-09 : stock proposé vieilli, nouveau point de départ après l'interruption, historique")
    void rg_dec_09_nouveau_point_de_depart() {
        negociant();
        String points = "/api/operateurs/" + operateur + "/point-de-depart";
        client.get().uri(points + "/proposition").exchange().expectStatus().isNoContent();
        dsmValidee();
        client.get().uri(points).exchange().expectStatus().isOk()
                .expectBody().jsonPath("$.fige").isEqualTo(true).jsonPath("$.renouvelable").isEqualTo(true);

        LocalDate reprise = DEBUT.plusMonths(3);
        Map<?, ?> proposition = corps(client.get().uri(points + "/proposition?debut={d}", reprise).exchange()
                .expectStatus().isOk());
        assertThat(proposition.get("debut")).isEqualTo(reprise.toString());
        assertThat((List<?>) proposition.get("lignes")).hasSize(2);
        agent.get().uri(points + "/proposition").exchange().expectStatus().isForbidden();

        client.post().uri(points).body(Map.of("debut", DEBUT, "lignes", List.of())).exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT)
                .expectBody().jsonPath("$.regle").isEqualTo("RG-DEC-09");
        creer(points, Map.of("debut", reprise, "lignes", proposition.get("lignes")));

        assertThat(liste(agent.get().uri(points + "/historique").exchange().expectStatus().isOk()))
                .extracting(p -> p.get("debut"), p -> p.get("fige"))
                .containsExactly(org.assertj.core.groups.Tuple.tuple(reprise.toString(), false),
                        org.assertj.core.groups.Tuple.tuple(DEBUT.toString(), true));
        DateTimeFormatter date = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        client.get().uri("/api/operateurs/{o}/declarations/journal", operateur).exchange().expectStatus().isOk()
                .expectBody().jsonPath("$[0].detail").value(String.class, detail -> assertThat(detail)
                        .contains("après une interruption du " + DEBUT.plusMonths(1).format(date)));
        agent.get().uri(dsms + "/situation").exchange().expectStatus().isOk()
                .expectBody().jsonPath("$.periode.debut").isEqualTo(reprise.toString());
    }

    @Test
    @DisplayName("RG-DEC-42 : stock déclaré de l'opérateur et de tous, export Excel ; lecture réservée")
    void rg_dec_42_stocks_declares() throws IOException {
        negociant();
        agent.get().uri("/api/operateurs/{o}/stock-declare", operateur).exchange().expectStatus().isNoContent();
        Object dsm = dsmValidee();

        Map<?, ?> stock = corps(agent.get().uri("/api/operateurs/{o}/stock-declare", operateur).exchange()
                .expectStatus().isOk());
        assertThat(stock.get("dsm")).isEqualTo(dsm);
        assertThat(((List<?>) stock.get("lignes")).stream().<Object>map(l -> ((Map<?, ?>) l).get("stock")).toList())
                .containsExactly(1000.0, 640.25);

        String observateur = emailUnique("observateur-bnia");
        membre(operateurs.operateurBnia(), Role.OBSERVATEUR, observateur);
        RestTestClient bnia = connecte(observateur);
        assertThat(liste(bnia.get().uri("/api/declarations/stocks?operateur={o}", operateur).exchange()
                .expectStatus().isOk())).extracting(l -> l.get("denomination"), l -> l.get("au"))
                .containsExactly(org.assertj.core.groups.Tuple.tuple("XO", DEBUT.plusMonths(1).minusDays(1).toString()),
                        org.assertj.core.groups.Tuple.tuple(null, DEBUT.plusMonths(1).minusDays(1).toString()));
        agent.get().uri("/api/declarations/stocks").exchange().expectStatus().isForbidden();

        byte[] excel = bnia.get().uri("/api/declarations/stocks/excel?operateur={o}", operateur).exchange()
                .expectStatus().isOk().expectBody(byte[].class).returnResult().getResponseBody();
        try (ReadableWorkbook classeur = new ReadableWorkbook(new ByteArrayInputStream(excel))) {
            // Rangées par leur numéro (à partir de 1) : la lecture saute les rangées vides.
            Map<Integer, Row> rangees = new HashMap<>();
            classeur.getFirstSheet().read().forEach(r -> rangees.put(r.getRowNum(), r));
            assertThat(rangees.get(5).getCellText(0)).isEqualTo("N° ressortissant");
            assertThat(rangees.get(6).getCellText(5)).isEqualTo("XO");
            assertThat(rangees.get(6).getCellAsNumber(7)).hasValueSatisfying(v -> assertThat(v).isEqualByComparingTo("1000"));
            assertThat(rangees.get(7).getCellText(5)).isEqualTo("2010");
            assertThat(rangees.get(8).getCellText(0)).isEqualTo("Total");
        }
    }

    @Test
    @DisplayName("RG-DEC-44 : historique d'une année déclarative, stock reporté à chaque fin de mois ; lecture réservée")
    void rg_dec_44_historique() {
        negociant();
        Object dsm = dsmValidee();
        LocalDate fin = DEBUT.plusMonths(1).minusDays(1);
        int annee = fin.getMonthValue() >= 4 ? fin.getYear() : fin.getYear() - 1;

        Map<?, ?> historique = corps(agent.get()
                .uri("/api/operateurs/{o}/stock-declare/historique?annee={a}", operateur, annee).exchange()
                .expectStatus().isOk());
        assertThat(((Map<?, ?>) historique.get("annee")).get("libelle")).isEqualTo(annee + "-" + (annee + 1));
        List<?> lignes = (List<?>) historique.get("lignes");
        // Deux lignes (XO, 2010) à la fin de la DSM, puis reportées à chaque fin de mois écoulée de l'année.
        assertThat(lignes).hasSizeGreaterThanOrEqualTo(2).hasSize(lignes.size() / 2 * 2);
        assertThat(lignes).allSatisfy(l -> {
            Map<?, ?> ligne = (Map<?, ?>) l;
            assertThat(ligne.get("dsm")).isEqualTo(dsm);
            assertThat(LocalDate.parse((String) ligne.get("au"))).isAfterOrEqualTo(fin).isBefore(LocalDate.now());
        });
        assertThat(((Map<?, ?>) lignes.getFirst()).get("au")).isEqualTo(fin.toString());
        assertThat(((Map<?, ?>) lignes.getFirst()).get("stock")).isEqualTo(1000.0);
        assertThat((List<?>) historique.get("anneesDisponibles")).isNotEmpty();
    }
}
