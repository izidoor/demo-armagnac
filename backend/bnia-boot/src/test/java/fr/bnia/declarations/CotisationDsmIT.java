package fr.bnia.declarations;

import static org.assertj.core.api.Assertions.assertThat;

import fr.bnia.declarations.application.partage.DeclarationsLecture;
import fr.bnia.declarations.domain.partage.AvoirEmis;
import fr.bnia.declarations.domain.partage.CotisationDsmVue;
import fr.bnia.declarations.domain.partage.FactureEmise;
import fr.bnia.operateurs.domain.partage.Role;
import fr.bnia.referentiels.application.partage.ReferentielsLecture;
import fr.bnia.referentiels.domain.partage.ValeurVue;
import fr.bnia.support.AbstractIT;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.client.RestTestClient;

/**
 * Lot 4 de la spec 05 : grille CVO, cotisation de la DSM, total HT des listings, projection des factures,
 * lecture publiée. La grille est commune à toute la base des IT : on y assure les deux taux de la
 * démonstration, et l'on n'y ajoute ou retire que des taux lointains.
 */
@SuppressWarnings("unchecked")
class CotisationDsmIT extends AbstractIT {

    private static final String GRILLE = "/api/declarations/taux-cvo";

    @Autowired ApplicationEventPublisher evenements;
    @Autowired DeclarationsLecture lecture;
    @Autowired ReferentielsLecture referentiels;

    private long operateur;
    private RestTestClient agent;
    private String dsms;

    /**
     * Les deux taux de la démonstration : 55 € depuis 2021, 60 € depuis 2024, 20 % exonérés ; et la classe de
     * TVA de la CVO, {@code NORMAL} à 20 %, comme en démonstration.
     */
    @BeforeEach
    void grilleDeDemonstration() {
        long normal = referentiels.classeTvaParCode("NORMAL").map(ValeurVue::id).orElseGet(() -> creer(
                "/api/referentiels/classes-tva", Map.of("code", "NORMAL", "libelle", "Normal")));
        if (referentiels.tauxTva(normal, LocalDate.now()).isEmpty()) {
            client.post().uri("/api/referentiels/classes-tva/{id}/taux", normal)
                    .body(Map.of("taux", new BigDecimal("20"), "dateDebut", "2014-01-01")).exchange()
                    .expectStatus().isOk();
        }
        List<?> grille = client.get().uri(GRILLE).exchange().expectStatus().isOk()
                .expectBody(List.class).returnResult().getResponseBody();
        for (Map<String, Object> taux : List.of(taux("55.00", "2021-01-01"), taux("60.00", "2024-01-01"))) {
            if (grille.stream().noneMatch(t -> ((Map<?, ?>) t).get("debut").equals(taux.get("debut")))) {
                client.post().uri(GRILLE).body(taux).exchange().expectStatus().isOk();
            }
        }
    }

    private static Map<String, Object> taux(String montant, String debut) {
        return Map.of("montant", new BigDecimal(montant), "pourcentageExonere", new BigDecimal("20.00"), "debut", debut);
    }

    /** Un négociant au point de départ donné, un agent connecté ; sa DSM avec les volumes de la facture d'exemple. */
    private Map<?, ?> dsmDeLExemple(LocalDate debut) {
        Map<String, Object> saisie = new HashMap<>();
        saisie.put("type", "NEGOCIANT");
        saisie.put("code", "T" + unique());
        saisie.put("raisonSociale", "Négoce " + unique());
        saisie.put("ville", "Condom");
        saisie.put("accordDiffusionGeneral", false);
        operateur = creer("/api/operateurs", saisie);
        creer("/api/operateurs/" + operateur + "/point-de-depart", Map.of("debut", debut, "lignes", List.of(
                Map.of("cle", "NB", "denomination", "XO", "compte", "12", "volume", new BigDecimal("4000")))));
        String email = emailUnique("agent");
        membre(operateur, Role.AGENT, email);
        agent = connecte(email);
        dsms = "/api/operateurs/" + operateur + "/dsm";
        Map<?, ?> dsm = corps(agent.post().uri(dsms).exchange().expectStatus().isCreated());
        // Sorties France 839,11 L (17, 21), export 2 254,15 L (22, 23).
        return corps(agent.put().uri(dsms + "/{id}", dsm.get("id"))
                .body(saisie(dsm, Map.of(17, "719.11", 21, "120.00", 22, "954.15", 23, "1300.00")))
                .exchange().expectStatus().isOk());
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

    @Test
    @DisplayName("RG-DEC-30 : le BNIA ajoute, corrige et supprime un taux ; chaîne recomposée ; début en double refusé ; l'opérateur n'y a pas accès")
    void rg_dec_30_grille() {
        int annee = 2100 + (int) (Math.random() * 800);
        String debut = annee + "-01-01";

        List<?> chaine = client.post().uri(GRILLE).body(taux("70.00", debut)).exchange().expectStatus().isOk()
                .expectBody(List.class).returnResult().getResponseBody();
        Map<?, ?> precedent = (Map<?, ?>) chaine.get(chaine.size() - 2);
        assertThat(precedent.get("fin")).isEqualTo((annee - 1) + "-12-31");
        client.post().uri(GRILLE).body(taux("71.00", debut)).exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT)
                .expectBody().jsonPath("$.regle").isEqualTo("RG-DEC-30");
        client.post().uri(GRILLE).body(Map.of("montant", 0, "pourcentageExonere", 20, "debut", "2999-06-01")).exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT);
        List<?> corrigee = client.put().uri(GRILLE + "/" + debut).body(taux("72.50", debut)).exchange()
                .expectStatus().isOk().expectBody(List.class).returnResult().getResponseBody();
        assertThat(((Map<?, ?>) corrigee.getLast()).get("montant")).isEqualTo(72.5);

        dsmDeLExemple(LocalDate.now().withDayOfMonth(1));
        agent.get().uri(GRILLE).exchange().expectStatus().isForbidden();
        agent.post().uri(GRILLE).body(taux("80.00", "2999-01-01")).exchange().expectStatus().isForbidden();

        List<?> recomposee = client.delete().uri(GRILLE + "/" + debut).exchange().expectStatus().isOk()
                .expectBody(List.class).returnResult().getResponseBody();
        assertThat(((Map<?, ?>) recomposee.getLast()).get("fin")).isNull();
    }

    @Test
    @DisplayName("RG-DEC-31 à 34 : la DSM de l'exemple, détail, TVA indicative et TTC ; total HT au listing du BNIA et au portail")
    void rg_dec_33_dsm_de_l_exemple() {
        Map<?, ?> dsm = dsmDeLExemple(LocalDate.now().withDayOfMonth(1));

        Map<?, ?> cotisation = (Map<?, ?>) dsm.get("cotisation");
        assertThat(cotisation.get("statut")).isEqualTo("CALCULEE");
        assertThat((List<Object>) cotisation.get("lignes")).extracting(l -> (Object) ((Map<?, ?>) l).get("nonAssujettie"))
                .containsExactly(100.69, 270.5);
        assertThat(cotisation.get("totalHt")).isEqualTo(1855.95);
        assertThat(((Map<?, ?>) cotisation.get("tva")).get("montant")).isEqualTo(296.95);
        assertThat(cotisation.get("ttc")).isEqualTo(2152.9);
        assertThat(((Map<?, ?>) cotisation.get("taux")).get("montant")).isEqualTo(60.0);

        client.get().uri("/api/declarations/dsm?operateur=" + operateur).exchange().expectStatus().isOk()
                .expectBody().jsonPath("$[0].totalHt").isEqualTo(1855.95)
                .jsonPath("$[0].facturee").isEqualTo(false);
        agent.get().uri(dsms).exchange().expectStatus().isOk()
                .expectBody().jsonPath("$[0].totalHt").isEqualTo(1855.95);
    }

    @Test
    @DisplayName("RG-DEC-32 : sans taux à la fin de la période, non calculable ; pas de total HT")
    void rg_dec_32_non_calculable() {
        Map<?, ?> dsm = dsmDeLExemple(LocalDate.of(2020, 6, 1));

        Map<?, ?> cotisation = (Map<?, ?>) dsm.get("cotisation");
        assertThat(cotisation.get("statut")).isEqualTo("NON_CALCULABLE");
        assertThat(cotisation.get("finPeriode")).isEqualTo("2020-06-30");
        assertThat(cotisation.get("totalHt")).isNull();
        assertThat(lecture.cotisation(((Number) dsm.get("id")).longValue()).orElseThrow().montant()).isEmpty();
    }

    @Test
    @DisplayName("RG-DEC-34 : FactureEmise et AvoirEmis reçus, une seule fois chacun ; la DSM est facturée, au listing et à l'écran")
    void rg_dec_34_facturee() {
        Map<?, ?> dsm = dsmDeLExemple(LocalDate.now().withDayOfMonth(1));
        long id = ((Number) dsm.get("id")).longValue();
        String numero = "F" + unique();
        FactureEmise facture = new FactureEmise(id, numero, LocalDate.now(), new BigDecimal("1855.95"),
                new BigDecimal("296.95"), new BigDecimal("2152.90"));

        evenements.publishEvent(facture);
        evenements.publishEvent(facture);
        evenements.publishEvent(new AvoirEmis(id, "A" + unique(), LocalDate.now(), new BigDecimal("100.00"),
                new BigDecimal("20.00"), new BigDecimal("120.00")));

        Map<?, ?> cotisation = (Map<?, ?>) corps(agent.get().uri(dsms + "/{id}", id).exchange().expectStatus().isOk())
                .get("cotisation");
        assertThat(cotisation.get("statut")).isEqualTo("FACTUREE");
        assertThat((List<Object>) cotisation.get("factures")).extracting(f -> (Object) ((Map<?, ?>) f).get("type"))
                .containsExactly("FACTURE", "AVOIR");
        assertThat(cotisation.get("totalHt")).isEqualTo(1755.95);
        client.get().uri("/api/declarations/dsm?operateur=" + operateur).exchange().expectStatus().isOk()
                .expectBody().jsonPath("$[0].facturee").isEqualTo(true)
                .jsonPath("$[0].totalHt").isEqualTo(1755.95);
    }

    @Test
    @DisplayName("RG-DEC-31 à 33 : la cotisation publiée pour facturation")
    void rg_dec_31_lecture_publiee() {
        Map<?, ?> dsm = dsmDeLExemple(LocalDate.now().withDayOfMonth(1));

        CotisationDsmVue vue = lecture.cotisation(((Number) dsm.get("id")).longValue()).orElseThrow();

        assertThat(vue.operateur()).isEqualTo(operateur);
        assertThat(vue.validee()).isFalse();
        assertThat(vue.lignes()).hasSize(2);
        assertThat(vue.totalHt()).isEqualByComparingTo("1855.95");
        assertThat(lecture.cotisation(-1)).isEmpty();
    }
}
