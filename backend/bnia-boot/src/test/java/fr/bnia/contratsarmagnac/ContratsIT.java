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
 * Lot 1 de la spec 06 : négociation et conclusion d'un contrat, au portail et au back-office ; stock estimé. Le
 * vendeur est un négociant dont le point de départ porte 640,25 L AP de millésime 2010 sous bois.
 */
class ContratsIT extends AbstractIT {

    /** Le premier du mois, quatre mois avant le mois courant. */
    private static final LocalDate DEBUT = LocalDate.now().withDayOfMonth(1).minusMonths(4);

    private long vendeur;
    private long acheteur;
    private String emailVendeur;
    private String emailAcheteur;
    private RestTestClient jeanne;
    private RestTestClient henri;

    @BeforeEach
    void parties() {
        Map<String, Object> negociant = new HashMap<>();
        negociant.put("type", "NEGOCIANT");
        negociant.put("code", "T" + unique());
        negociant.put("raisonSociale", "Négoce " + unique());
        negociant.put("ville", "Condom");
        negociant.put("accordDiffusionGeneral", false);
        vendeur = creer("/api/operateurs", negociant);
        creer("/api/operateurs/" + vendeur + "/point-de-depart", Map.of("debut", DEBUT, "lignes", List.of(
                Map.of("cle", "MB", "millesime", 2010, "volume", new BigDecimal("640.25")),
                Map.of("cle", "NB", "denomination", "XO", "compte", "12", "volume", new BigDecimal("1500")),
                Map.of("cle", "MV", "millesime", 2012, "compte", "8", "volume", new BigDecimal("50")))));
        acheteur = nouvelOperateur();
        emailVendeur = emailUnique("jeanne");
        emailAcheteur = emailUnique("henri");
        membre(vendeur, Role.ADMIN, emailVendeur);
        membre(acheteur, Role.AGENT, emailAcheteur);
        jeanne = connecte(emailVendeur);
        henri = connecte(emailAcheteur);
    }

    private static Map<String, Object> contenu(String volume, String prix) {
        Map<String, Object> ligne = new HashMap<>();
        ligne.put("appellation", "BAS_ARMAGNAC");
        ligne.put("millesime", 2010);
        ligne.put("volume", new BigDecimal(volume));
        ligne.put("degre", new BigDecimal("70"));
        ligne.put("prix", new BigDecimal(prix));
        return Map.of("lignes", List.of(ligne), "solde", "COMPTANT");
    }

    private RestTestClient.ResponseSpec ouvrir(String volume) {
        return jeanne.post().uri("/api/operateurs/{o}/contrats", vendeur)
                .body(Map.of("partie", "VENDEUR", "autrePartie", acheteur, "contenu", contenu(volume, "1750")))
                .exchange();
    }

    private static Map<?, ?> corps(RestTestClient.ResponseSpec reponse) {
        return reponse.expectBody(Map.class).returnResult().getResponseBody();
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> liste(RestTestClient.ResponseSpec reponse) {
        return reponse.expectBody(List.class).returnResult().getResponseBody();
    }

    private long projet() {
        return ((Number) corps(ouvrir("9").expectStatus().isCreated()).get("id")).longValue();
    }

    @Test
    @SuppressWarnings("unchecked")
    @DisplayName("RG-CA-12, 07, 08 : projet bloqué par le stock estimé, corrigé, modifié par l'acheteur, approuvé : conclu")
    void rg_ca_08_fil_complet() {
        Map<?, ?> bloque = corps(ouvrir("10").expectStatus().isEqualTo(422));
        assertThat(bloque.get("regle")).isEqualTo("RG-CA-12");
        assertThat((String) bloque.get("detail")).contains("6,40 hl AP en stock");

        Map<?, ?> projet = corps(ouvrir("9").expectStatus().isCreated());
        long id = ((Number) projet.get("id")).longValue();
        String numero = (String) projet.get("numero");
        assertThat(numero).matches("CA-\\d{4}-\\d{4}");
        assertThat(projet.get("attend")).isEqualTo("ACHETEUR");
        assertThat(projet.get("maPartie")).isEqualTo("VENDEUR");
        assertThat(((Map<?, ?>) projet.get("version")).get("volumeAp")).isEqualTo(630.0);
        assertThat(courriels.dernier(emailAcheteur).getSubject()).isEqualTo("Projet de contrat " + numero + " à approuver");

        assertThat(liste(henri.get().uri("/api/operateurs/{o}/contrats", acheteur).exchange().expectStatus().isOk()))
                .singleElement().satisfies(c -> {
                    assertThat(c.get("onglet")).isEqualTo("A_APPROUVER");
                    assertThat(c.get("produit")).isEqualTo("2010");
                    assertThat(((Map<?, ?>) c.get("vendeur")).get("id")).isEqualTo((int) vendeur);
                });

        Map<?, ?> modifie = corps(henri.put().uri("/api/operateurs/{o}/contrats/{id}", acheteur, id)
                .body(Map.of("versionVue", 1, "contenu", contenu("9", "1700"))).exchange().expectStatus().isOk());
        assertThat(((Map<?, ?>) modifie.get("version")).get("numero")).isEqualTo(2);
        assertThat(courriels.dernier(emailVendeur).getSubject()).isEqualTo("Projet de contrat " + numero + " modifié");

        Map<?, ?> vu = corps(jeanne.get().uri("/api/operateurs/{o}/contrats/{id}", vendeur, id).exchange()
                .expectStatus().isOk());
        Map<?, ?> ecart = (Map<?, ?>) vu.get("ecart");
        assertThat(ecart.get("depuis")).isEqualTo(1);
        assertThat((List<?>) ecart.get("lignes")).singleElement().satisfies(l -> assertThat((List<?>) ((Map<?, ?>) l)
                .get("champs")).singleElement().isEqualTo(Map.of("champ", "prix", "avant", "1750.00", "apres", "1700.00")));

        henri.post().uri("/api/operateurs/{o}/contrats/{id}/approbation", acheteur, id).body(Map.of("versionVue", 2))
                .exchange().expectStatus().isEqualTo(422).expectBody().jsonPath("$.regle").isEqualTo("RG-CA-08");
        jeanne.post().uri("/api/operateurs/{o}/contrats/{id}/approbation", vendeur, id).body(Map.of("versionVue", 1))
                .exchange().expectStatus().isEqualTo(409);
        Map<?, ?> conclu = corps(jeanne.post().uri("/api/operateurs/{o}/contrats/{id}/approbation", vendeur, id)
                .body(Map.of("versionVue", 2)).exchange().expectStatus().isOk());

        assertThat(conclu.get("etat")).isEqualTo("CONCLU");
        assertThat(conclu.get("dateContrat")).isEqualTo(LocalDate.now().toString());
        assertThat(conclu.get("ecart")).isNull();
        assertThat((List<?>) conclu.get("approbations")).hasSize(2);
        assertThat((List<Object>) conclu.get("journal")).extracting(e -> (Object) ((Map<?, ?>) e).get("action"))
                .containsExactly("OUVERTURE", "MODIFICATION", "APPROBATION", "CONCLUSION");
        assertThat(courriels.dernier(emailAcheteur).getText()).contains("est conclu");
    }

    @Test
    @DisplayName("RG-CA-25 : au back-office, tous les contrats, filtrés par opérateur, en lecture ; onglets du portail")
    void rg_ca_25_listes() {
        long id = projet();

        List<Map<String, Object>> duVendeur = liste(client.get().uri("/api/contrats?operateur={o}", vendeur).exchange()
                .expectStatus().isOk());
        assertThat(duVendeur).singleElement().satisfies(c -> {
            assertThat(c.get("id")).isEqualTo((int) id);
            assertThat(c.get("onglet")).isNull();
            assertThat(c.get("etat")).isEqualTo("PROJET");
        });
        Map<?, ?> lu = corps(client.get().uri("/api/contrats/{id}", id).exchange().expectStatus().isOk());
        assertThat(lu.get("peutAgir")).isEqualTo(false);
        assertThat(lu.get("maPartie")).isNull();
        assertThat(liste(jeanne.get().uri("/api/operateurs/{o}/contrats", vendeur).exchange().expectStatus().isOk()))
                .singleElement().satisfies(c -> assertThat(c.get("onglet")).isEqualTo("EN_NEGOCIATION"));
    }

    @Test
    @DisplayName("RG-CA-01 : le BNIA lit sans agir ; un observateur lit sans agir ; un tiers ne voit pas le contrat")
    void rg_ca_01_refus() {
        long id = projet();
        long bnia = operateurs.operateurBnia();

        client.post().uri("/api/operateurs/{o}/contrats", bnia)
                .body(Map.of("partie", "ACHETEUR", "autrePartie", vendeur, "contenu", contenu("1", "1750")))
                .exchange().expectStatus().isForbidden();
        client.post().uri("/api/operateurs/{o}/contrats/{id}/approbation", acheteur, id).body(Map.of("versionVue", 1))
                .exchange().expectStatus().isForbidden().expectBody().jsonPath("$.regle").isEqualTo("RG-CA-01");
        String observateur = emailUnique("observateur");
        membre(acheteur, Role.OBSERVATEUR, observateur);
        RestTestClient lecteur = connecte(observateur);
        lecteur.get().uri("/api/operateurs/{o}/contrats/{id}", acheteur, id).exchange().expectStatus().isOk();
        lecteur.post().uri("/api/operateurs/{o}/contrats/{id}/refus", acheteur, id).body(Map.of("versionVue", 1))
                .exchange().expectStatus().isForbidden();
        henri.get().uri("/api/contrats").exchange().expectStatus().isForbidden();

        long tiers = nouvelOperateur();
        String emailTiers = emailUnique("tiers");
        membre(tiers, Role.ADMIN, emailTiers);
        connecte(emailTiers).get().uri("/api/operateurs/{o}/contrats/{id}", tiers, id).exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @DisplayName("RG-CA-02, 10 : l'autre partie n'est pas le BNIA ; la partie attendue refuse le projet")
    void rg_ca_10_refus_et_rg_ca_02() {
        jeanne.post().uri("/api/operateurs/{o}/contrats", vendeur).body(Map.of("partie", "VENDEUR", "autrePartie",
                operateurs.operateurBnia(), "contenu", contenu("1", "1750"))).exchange().expectStatus()
                .isEqualTo(422).expectBody().jsonPath("$.regle").isEqualTo("RG-CA-02");

        long id = projet();
        Map<?, ?> refuse = corps(henri.post().uri("/api/operateurs/{o}/contrats/{id}/refus", acheteur, id)
                .body(Map.of("versionVue", 1)).exchange().expectStatus().isOk());
        assertThat(refuse.get("etat")).isEqualTo("REFUSE");
        assertThat(courriels.dernier(emailVendeur).getSubject()).endsWith("refusé");
    }

    @Test
    @DisplayName("RG-CA-12 : le vendeur voit au détail que son stock ne couvre pas le projet de l'acheteur, et ne peut l'approuver")
    void rg_ca_12_stock_insuffisant_au_detail() {
        long id = ((Number) corps(henri.post().uri("/api/operateurs/{o}/contrats", acheteur)
                .body(Map.of("partie", "ACHETEUR", "autrePartie", vendeur, "contenu", contenu("10", "1750")))
                .exchange().expectStatus().isCreated()).get("id")).longValue();

        assertThat((String) corps(jeanne.get().uri("/api/operateurs/{o}/contrats/{id}", vendeur, id).exchange()
                .expectStatus().isOk()).get("stockInsuffisant")).contains("6,40 hl AP en stock");
        assertThat(corps(henri.get().uri("/api/operateurs/{o}/contrats/{id}", acheteur, id).exchange()
                .expectStatus().isOk()).get("stockInsuffisant")).isNull();
        jeanne.post().uri("/api/operateurs/{o}/contrats/{id}/approbation", vendeur, id).body(Map.of("versionVue", 1))
                .exchange().expectStatus().isEqualTo(422).expectBody().jsonPath("$.regle").isEqualTo("RG-CA-12");
    }

    @Test
    @DisplayName("RG-CA-11 : stock estimé publié (base du point de départ) ; stock sous bois pour la saisie du vendeur")
    void rg_ca_11_stock_estime() {
        Map<?, ?> estime = corps(jeanne.get().uri("/api/operateurs/{o}/stock-estime", vendeur).exchange()
                .expectStatus().isOk());
        assertThat(estime.get("baseAu")).isEqualTo(DEBUT.toString());
        assertThat((List<?>) estime.get("lignes")).hasSize(3);

        assertThat(liste(jeanne.get().uri("/api/operateurs/{o}/contrats/stock", vendeur).exchange()
                .expectStatus().isOk())).extracting(l -> l.get("cle")).containsExactly("MB", "NB");
        henri.get().uri("/api/operateurs/{o}/stock-estime", vendeur).exchange().expectStatus().isForbidden();
    }
}
