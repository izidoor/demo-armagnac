package fr.bnia.contratsarmagnac;

import static org.assertj.core.api.Assertions.assertThat;

import fr.bnia.operateurs.domain.partage.Role;
import fr.bnia.support.AbstractIT;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.client.RestTestClient;

/**
 * Lot 2 de la spec 06 : retiraisons, mouvements de contrat, DSM préremplies, envoi après la fin de période. Deux
 * négociants partent du premier du mois courant ; le vendeur a initié sa DSM, l'acheteur pas encore. Le contrat :
 * 9 hl de millésime 2010 à 70 % (6,30 hl AP), sur 640,25 L AP en stock.
 */
class RetiraisonsIT extends AbstractIT {

    private static final LocalDate MOIS = LocalDate.now().withDayOfMonth(1);

    private long vendeur;
    private long acheteur;
    private String emailVendeur;
    private String emailAcheteur;
    private RestTestClient jeanne;
    private RestTestClient henri;
    private long contrat;
    private long dsmVendeur;

    private long negociant(String ligneDenomination) {
        Map<String, Object> negociant = new HashMap<>();
        negociant.put("type", "NEGOCIANT");
        negociant.put("code", "T" + unique());
        negociant.put("raisonSociale", "Négoce " + unique());
        negociant.put("ville", "Condom");
        negociant.put("accordDiffusionGeneral", false);
        long id = creer("/api/operateurs", negociant);
        creer("/api/operateurs/" + id + "/point-de-depart", Map.of("debut", MOIS, "lignes", List.of(
                Map.of("cle", "MB", "millesime", 2010, "volume", new BigDecimal("640.25")),
                Map.of("cle", "NB", "denomination", ligneDenomination, "compte", "12", "volume", new BigDecimal("1500")))));
        return id;
    }

    @BeforeEach
    void contratConclu() {
        vendeur = negociant("XO");
        acheteur = negociant("VSOP");
        emailVendeur = emailUnique("jeanne");
        emailAcheteur = emailUnique("henri");
        membre(vendeur, Role.ADMIN, emailVendeur);
        membre(acheteur, Role.AGENT, emailAcheteur);
        jeanne = connecte(emailVendeur);
        henri = connecte(emailAcheteur);
        dsmVendeur = creer(jeanne, "/api/operateurs/" + vendeur + "/dsm", Map.of());
        Map<String, Object> ligne = new HashMap<>();
        ligne.put("appellation", "BAS_ARMAGNAC");
        ligne.put("millesime", 2010);
        ligne.put("volume", new BigDecimal("9"));
        ligne.put("degre", new BigDecimal("70"));
        ligne.put("prix", new BigDecimal("1750"));
        contrat = creer(jeanne, "/api/operateurs/" + vendeur + "/contrats", Map.of("partie", "VENDEUR",
                "autrePartie", acheteur, "contenu", Map.of("lignes", List.of(ligne), "solde", "COMPTANT")));
        henri.post().uri("/api/operateurs/{o}/contrats/{id}/approbation", acheteur, contrat)
                .body(Map.of("versionVue", 1)).exchange().expectStatus().isOk();
    }

    private static Map<?, ?> corps(RestTestClient.ResponseSpec reponse) {
        return reponse.expectBody(Map.class).returnResult().getResponseBody();
    }

    private static Map<String, Object> retiraison(LocalDate date, boolean derniere, String volume) {
        return Map.of("dateEnlevement", date, "derniere", derniere,
                "lignes", List.of(Map.of("rang", 0, "volume", new BigDecimal(volume))));
    }

    private RestTestClient.ResponseSpec declarer(Map<String, Object> retiraison) {
        return henri.post().uri("/api/operateurs/{o}/contrats/{id}/retiraisons", acheteur, contrat).body(retiraison)
                .exchange();
    }

    private Map<?, ?> dsm(RestTestClient session, long operateur, long id) {
        return corps(session.get().uri("/api/operateurs/{o}/dsm/{id}", operateur, id).exchange().expectStatus().isOk());
    }

    @SuppressWarnings("unchecked")
    private static Map<?, ?> ligne2010(Map<?, ?> dsm) {
        return ((List<Map<?, ?>>) dsm.get("lignes")).stream().filter(l -> Integer.valueOf(2010).equals(l.get("millesime")))
                .findFirst().orElseThrow();
    }

    private static Object colonne(Map<?, ?> ligne, int numero) {
        return ((List<?>) ligne.get("volumes")).get(numero - 1);
    }

    @Test
    @SuppressWarnings("unchecked")
    @DisplayName("RG-CA-13, 15, 21 à 23 : Henri déclare, Jeanne valide : la vente est dans la DSM de Jeanne ; l'achat dans celle d'Henri à son initiation, justifié")
    void rg_ca_22_retiraison_jusqu_aux_dsm() {
        Map<?, ?> declaree = corps(declarer(retiraison(LocalDate.now(), false, "5")).expectStatus().isCreated());
        Map<?, ?> r = ((List<Map<?, ?>>) declaree.get("retiraisons")).getFirst();
        assertThat(r.get("etat")).isEqualTo("A_VALIDER_VENDEUR");
        assertThat(((List<Map<?, ?>>) declaree.get("restes")).getFirst().get("reste")).isEqualTo(4.0);
        String numero = (String) declaree.get("numero");
        assertThat(courriels.dernier(emailVendeur).getSubject()).isEqualTo("Retiraison à valider, contrat " + numero);

        Map<?, ?> validee = corps(jeanne.post().uri("/api/operateurs/{o}/contrats/{id}/retiraisons/1/validation",
                vendeur, contrat).body(Map.of("versionVue", 1)).exchange().expectStatus().isOk());

        assertThat(((List<Map<?, ?>>) validee.get("retiraisons")).getFirst().get("etat")).isEqualTo("VALIDEE");
        assertThat(((List<Map<?, ?>>) validee.get("restes")).getFirst().get("retire")).isEqualTo(5.0);
        // RG-CA-21, 22 : 5 hl à 70 % = 350 L AP, vente en colonne 21 (acheteur hors région délimitée).
        Map<?, ?> chezJeanne = ligne2010(dsm(jeanne, vendeur, dsmVendeur));
        assertThat(colonne(chezJeanne, 21)).isEqualTo(350.0);
        assertThat((List<Map<?, ?>>) chezJeanne.get("contrats")).singleElement()
                .satisfies(c -> assertThat(c.get("libelle")).isEqualTo("Contrat " + numero + ", retiraison 1"));
        // RG-CA-11 : les stocks estimés bougent.
        Map<?, ?> stockVendeur = corps(jeanne.get().uri("/api/operateurs/{o}/stock-estime", vendeur).exchange()
                .expectStatus().isOk());
        assertThat(((List<Map<?, ?>>) stockVendeur.get("lignes")).stream()
                .filter(l -> Integer.valueOf(2010).equals(l.get("millesime"))).findFirst().orElseThrow())
                .satisfies(l -> {
                    assertThat(l.get("ventes")).isEqualTo(350.0);
                    assertThat(l.get("estime")).isEqualTo(290.25);
                });

        // RG-CA-22 : l'acheteur n'avait pas de DSM : l'achat s'inscrit à l'initiation, en colonne 3 (vendeur négociant).
        long dsmAcheteur = creer(henri, "/api/operateurs/" + acheteur + "/dsm", Map.of());
        Map<?, ?> chezHenri = dsm(henri, acheteur, dsmAcheteur);
        assertThat(colonne(ligne2010(chezHenri), 3)).isEqualTo(350.0);
        assertThat((List<?>) chezHenri.get("manques")).isEmpty();
        // RG-CA-23 : le justificatif contrat, dans l'onglet Pièces.
        Map<?, ?> pieces = corps(henri.get().uri("/api/operateurs/{o}/dsm/{id}/pieces", acheteur, dsmAcheteur)
                .exchange().expectStatus().isOk());
        assertThat((List<Map<?, ?>>) pieces.get("contrats")).singleElement().satisfies(c -> {
            assertThat(c.get("typeMouvement")).isEqualTo("ACHAT");
            assertThat(c.get("contrat")).isEqualTo((int) contrat);
            assertThat(c.get("libelle")).isEqualTo("Contrat " + numero + ", retiraison 1");
        });
        assertThat(courriels.dernier(emailAcheteur).getSubject()).isEqualTo("Retiraison validée, contrat " + numero);
    }

    @Test
    @SuppressWarnings("unchecked")
    @DisplayName("RG-CA-24 : la DSM du mois en cours ne s'envoie pas avant la fin de sa période ; « Envoyer » n'est pas offert")
    void rg_ca_24_envoi_apres_la_fin_de_periode() {
        Map<?, ?> vue = dsm(jeanne, vendeur, dsmVendeur);
        assertThat((List<Object>) vue.get("actions")).doesNotContain("ENVOYER").contains("ENREGISTRER");
        assertThat(vue.get("envoiPossibleLe")).isEqualTo(MOIS.plusMonths(1).toString());
        jeanne.post().uri("/api/operateurs/{o}/dsm/{id}/envoi", vendeur, dsmVendeur).body(Map.of()).exchange()
                .expectStatus().isEqualTo(422).expectBody().jsonPath("$.regle").isEqualTo("RG-CA-24");
    }

    @Test
    @SuppressWarnings("unchecked")
    @DisplayName("RG-CA-13, 14 : au-delà du reste à retirer, refus ; le vendeur modifie, l'acheteur voit l'écart et valide la dernière : exécuté")
    void rg_ca_14_refus_et_modification() {
        Map<?, ?> refus = corps(henri.post().uri("/api/operateurs/{o}/contrats/{id}/retiraisons", acheteur, contrat)
                .body(retiraison(LocalDate.now(), false, "10")).exchange().expectStatus().isEqualTo(422));
        assertThat(refus.get("regle")).isEqualTo("RG-CA-13");
        assertThat((String) refus.get("detail")).contains("10,00 hl demandés, il en reste 9,00 hl à retirer");

        declarer(retiraison(LocalDate.now().plusDays(3), false, "8")).expectStatus().isCreated();
        jeanne.put().uri("/api/operateurs/{o}/contrats/{id}/retiraisons/1", vendeur, contrat)
                .body(Map.of("versionVue", 1, "contenu", retiraison(LocalDate.now().plusDays(3), true, "10")))
                .exchange().expectStatus().isEqualTo(422).expectBody().jsonPath("$.regle").isEqualTo("RG-CA-14");
        Map<?, ?> modifiee = corps(jeanne.put().uri("/api/operateurs/{o}/contrats/{id}/retiraisons/1", vendeur, contrat)
                .body(Map.of("versionVue", 1, "contenu", retiraison(LocalDate.now().plusDays(4), true, "7")))
                .exchange().expectStatus().isOk());
        Map<?, ?> r = ((List<Map<?, ?>>) modifiee.get("retiraisons")).getFirst();
        assertThat(r.get("attend")).isEqualTo("ACHETEUR");

        Map<?, ?> vueHenri = corps(henri.get().uri("/api/operateurs/{o}/contrats/{id}", acheteur, contrat).exchange()
                .expectStatus().isOk());
        Map<?, ?> ecart = (Map<?, ?>) ((List<Map<?, ?>>) vueHenri.get("retiraisons")).getFirst().get("ecart");
        assertThat((List<Map<?, ?>>) ecart.get("champs")).extracting(c -> (Object) c.get("champ"))
                .containsExactly("dateEnlevement", "derniere");
        Map<?, ?> executee = corps(henri.post().uri("/api/operateurs/{o}/contrats/{id}/retiraisons/1/validation",
                acheteur, contrat).body(Map.of("versionVue", 2)).exchange().expectStatus().isOk());
        assertThat(executee.get("etat")).isEqualTo("EXECUTE");
        assertThat((List<Object>) executee.get("journal")).extracting(e -> (Object) ((Map<?, ?>) e).get("action"))
                .endsWith("RETIRAISON_DECLAREE", "RETIRAISON_MODIFIEE", "RETIRAISON_VALIDEE", "EXECUTION");
    }

    @Test
    @DisplayName("RG-CA-16, 17, 01 : Henri retire sa retiraison, Jeanne annule ; le BNIA n'agit pas")
    void rg_ca_17_retrait_et_annulation() {
        declarer(retiraison(LocalDate.now(), false, "1")).expectStatus().isCreated();
        henri.delete().uri("/api/operateurs/{o}/contrats/{id}/retiraisons/1", acheteur, contrat).exchange()
                .expectStatus().isOk();
        client.post().uri("/api/operateurs/{o}/contrats/{id}/annulation", vendeur, contrat).exchange()
                .expectStatus().isForbidden();
        Map<?, ?> annule = corps(jeanne.post().uri("/api/operateurs/{o}/contrats/{id}/annulation", vendeur, contrat)
                .exchange().expectStatus().isOk());
        assertThat(annule.get("etat")).isEqualTo("ANNULE");
        assertThat((List<?>) annule.get("retiraisons")).isEmpty();
        assertThat(courriels.dernier(emailAcheteur).getSubject()).isEqualTo("Contrat " + annule.get("numero") + " annulé");
    }
}
