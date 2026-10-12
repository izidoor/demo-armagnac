package fr.bnia.operateurs;

import static org.assertj.core.api.Assertions.assertThat;

import fr.bnia.operateurs.application.partage.OperateursLecture;
import fr.bnia.referentiels.application.partage.ReferentielsLecture;
import fr.bnia.referentiels.domain.partage.PaysVue;
import fr.bnia.support.AbstractIT;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

/**
 * Opérateurs, identité : chaîne complète. La base est partagée entre les tests : les codes saisis sont
 * uniques, et les codes proposés sont vérifiés l'un par rapport à l'autre, jamais en valeur absolue.
 */
class OperateurIT extends AbstractIT {

    private static final String URL = "/api/operateurs";
    private static final String PAYS = "/api/referentiels/pays";
    private static final String ZONES = "/api/referentiels/zones-commerciales";

    @Autowired
    ReferentielsLecture referentiels;

    @Autowired
    OperateursLecture operateurs;

    private long france;
    private long belgique;

    @BeforeEach
    void pays() {
        france = pays("FR", "France");
        belgique = pays("BE", "Belgique");
        // Les codes proposés par ces tests partent de ces numéros : ils laissent libres ceux des données de
        // démonstration (000001…, N320001…), chargées par DemoOperateursIT dans la même base.
        reserver("PRODUCTEUR", "900000", "32100");
        reserver("NEGOCIANT", "N329000", "32100");
        reserver("NEGOCIANT", "N2A9000", "20150");
    }

    private void reserver(String type, String code, String codePostal) {
        if (operateurs.operateurParCode(code).isEmpty()) {
            creer(URL, saisie(type, code, "Réserve de numérotation " + code, codePostal));
        }
    }

    @Test
    @DisplayName("RG-OPE-02 et 03 : création sans code ni pays, puis consultation de la fiche complète")
    void rg_ope_02_03_creer_un_producteur() {
        var saisie = saisie("PRODUCTEUR", null, "Domaine " + suffixe(), "32100");
        saisie.remove("pays");
        long id = creer(URL, saisie);
        long suivant = creer(URL, saisie("AUTRE", null, "Courtier " + suffixe(), "40000"));

        String code = code(id);
        assertThat(code).matches("[0-9]{6}");
        assertThat(code(suivant)).isEqualTo(String.format("%06d", Integer.parseInt(code) + 1));
        client.get().uri(URL + "/{id}", id)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.pays").isEqualTo(france)
                .jsonPath("$.actif").isEqualTo(true)
                .jsonPath("$.champsManquants").isEqualTo(List.of("région"))
                .jsonPath("$.codeConforme").isEqualTo(true)
                .jsonPath("$.periodicite").isEqualTo("ANNUELLE")
                .jsonPath("$.statutDeclarationStock").isEqualTo("OUI")
                .jsonPath("$.niveauDiffusion").isEqualTo("REFUS")
                .jsonPath("$.accesStatistiques").isEqualTo(false)
                .jsonPath("$.autresDuGroupe").isEmpty();
    }

    @Test
    @DisplayName("RG-OPE-03 : code proposé à un négociant selon son département ; aucun hors de France")
    void rg_ope_03_code_propose() {
        String propose = codePropose("NEGOCIANT", "20150", france);
        assertThat(propose).startsWith("N2A").hasSize(7);

        creer(URL, saisie("NEGOCIANT", propose, "Négoce corse " + suffixe(), "20150"));
        assertThat(codePropose("NEGOCIANT", "20150", france))
                .isEqualTo("N2A" + String.format("%04d", Integer.parseInt(propose.substring(3)) + 1));

        client.get().uri(u -> u.path(URL + "/code-propose").queryParam("type", "NEGOCIANT")
                        .queryParam("codePostal", "1000").queryParam("pays", belgique).build())
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.code").doesNotExist();
    }

    @Test
    @DisplayName("RG-OPE-04 et 05 : le code se corrige, reste unique ; changer de type le signale non conforme")
    void rg_ope_04_05_code() {
        String code = "T" + suffixe();
        long id = creer(URL, saisie("PRODUCTEUR", code, "Domaine " + suffixe(), "32100"));

        client.post().uri(URL).body(saisie("PRODUCTEUR", code.toLowerCase(), "Autre " + suffixe(), "32100"))
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT)
                .expectBody().jsonPath("$.regle").isEqualTo("RG-OPE-04");

        client.put().uri(URL + "/{id}/identite", id).body(saisie("NEGOCIANT", code, "Négoce " + suffixe(), "32100"))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.code").isEqualTo(code)
                .jsonPath("$.codeConforme").isEqualTo(false)
                .jsonPath("$.codePropose").value(propose -> assertThat((String) propose).startsWith("N32"));

        client.post().uri(URL + "/{id}/nouveau-code", id)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.codeConforme").isEqualTo(true)
                .jsonPath("$.code").value(nouveau -> assertThat((String) nouveau).startsWith("N32"));
    }

    @Test
    @DisplayName("RG-OPE-06 : un SIRET mal formé est refusé en 422 ; un SIRET en double est signalé sans bloquer")
    void rg_ope_06_siret() {
        var invalide = saisie("PRODUCTEUR", "T" + suffixe(), "Domaine " + suffixe(), "32100");
        invalide.put("siret", "41234567800017");
        client.post().uri(URL).body(invalide)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT)
                .expectBody().jsonPath("$.regle").isEqualTo("RG-OPE-06");

        String raison = "Domaine du Tariquet " + suffixe();
        var valide = saisie("PRODUCTEUR", "T" + suffixe(), raison, "32800");
        valide.put("siret", "73282932000074");
        long tariquet = creer(URL, valide);
        var doublon = new HashMap<>(valide);
        doublon.put("code", "T" + suffixe());
        creer(URL, doublon);

        client.get().uri(u -> u.path(URL + "/doublons").queryParam("raisonSociale", raison.toUpperCase())
                        .queryParam("siret", "732 829 320 00074").queryParam("sauf", tariquet).build())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[?(@.champ == 'RAISON_SOCIALE')].regle").isEqualTo("RG-OPE-08")
                .jsonPath("$[?(@.champ == 'SIRET')].operateur.code").isEqualTo(doublon.get("code"))
                .jsonPath("$[?(@.operateur.id == " + tariquet + ")]").isEmpty();
    }

    @Test
    @DisplayName("RG-OPE-07 : l'identifiant tiers ODG est unique (409)")
    void rg_ope_07_identifiant_odg() {
        String odg = "ODG-" + suffixe();
        var premier = saisie("PRODUCTEUR", "T" + suffixe(), "Domaine " + suffixe(), "32100");
        premier.put("identifiantOdg", odg);
        creer(URL, premier);

        var second = saisie("PRODUCTEUR", "T" + suffixe(), "Domaine " + suffixe(), "32100");
        second.put("identifiantOdg", odg.toLowerCase());
        client.post().uri(URL).body(second)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT)
                .expectBody().jsonPath("$.regle").isEqualTo("RG-OPE-07");
    }

    @Test
    @DisplayName("RG-OPE-09 : autres fiches du groupe et groupes proposés, à la casse et aux accents près")
    void rg_ope_09_groupe() {
        String groupe = "Maison Durand " + suffixe();
        var condom = saisie("PRODUCTEUR", "T" + suffixe(), "Chai de Condom", "32100");
        condom.put("groupe", groupe);
        long idCondom = creer(URL, condom);
        var eauze = saisie("PRODUCTEUR", "T" + suffixe(), "Chai d'Eauze", "32800");
        eauze.put("groupe", "  " + groupe.toUpperCase() + " ");
        long idEauze = creer(URL, eauze);

        client.get().uri(URL + "/{id}", idCondom)
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.autresDuGroupe[*].id").isEqualTo(List.of((int) idEauze));
        client.get().uri(u -> u.path(URL + "/groupes").queryParam("recherche", groupe.toLowerCase()).build())
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.length()").isEqualTo(1);
    }

    @Test
    @DisplayName("RG-OPE-20, 21 et 24 : formats et références contrôlés en 422")
    void rg_ope_20_21_24_controles() {
        var gps = saisie("PRODUCTEUR", "T" + suffixe(), "Domaine " + suffixe(), "32100");
        gps.put("latitude", 43.9);
        attendre422(gps, "RG-OPE-20");

        var email = saisie("PRODUCTEUR", "T" + suffixe(), "Domaine " + suffixe(), "32100");
        email.put("emailGeneral", "contact@");
        attendre422(email, "RG-OPE-24");

        String codeTitre = "TT" + suffixe();
        long titre = creer("/api/referentiels/titres", Map.of("code", codeTitre, "libelle", "Titre " + codeTitre));
        client.post().uri("/api/referentiels/titres/{id}/desactivation", titre).exchange().expectStatus().isOk();
        var avecTitre = saisie("PRODUCTEUR", "T" + suffixe(), "Domaine " + suffixe(), "32100");
        avecTitre.put("titre", titre);
        attendre422(avecTitre, "RG-OPE-21");
    }

    @Test
    @DisplayName("RG-OPE-21 : une valeur de référentiel portée par une fiche ne se supprime plus (RG-REF-04)")
    void rg_ope_21_reference_protegee() {
        String codeTitre = "TT" + suffixe();
        long titre = creer("/api/referentiels/titres", Map.of("code", codeTitre, "libelle", "Titre " + codeTitre));
        var saisie = saisie("PRODUCTEUR", "T" + suffixe(), "Domaine " + suffixe(), "32100");
        saisie.put("titre", titre);
        creer(URL, saisie);

        client.delete().uri("/api/referentiels/titres/{id}", titre)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    @DisplayName("RG-OPE-02 : listing filtré, fiches incomplètes comprises")
    void rg_ope_02_listing() {
        String ville = "Ville" + suffixe();
        var complete = saisie("NEGOCIANT", "T" + suffixe(), "Négoce " + suffixe(), "32100");
        complete.put("ville", ville);
        long idComplete = creer(URL, complete);
        client.put().uri(URL + "/{id}/production", idComplete).body(production("MENSUELLE", "OUI", region(), List.of()))
                .exchange().expectStatus().isOk().expectBody().jsonPath("$.champsManquants").isEmpty();
        var incomplete = saisie("NEGOCIANT", "T" + suffixe(), "Négoce " + suffixe(), "32100");
        incomplete.put("ville", ville);
        incomplete.remove("adresseLigne1");
        long idIncomplete = creer(URL, incomplete);

        client.get().uri(u -> u.path(URL).queryParam("recherche", ville.toLowerCase()).queryParam("type", "NEGOCIANT").build())
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.length()").isEqualTo(2);
        client.get().uri(u -> u.path(URL).queryParam("recherche", ville).queryParam("incomplet", true).build())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[*].id").isEqualTo(List.of((int) idIncomplete))
                .jsonPath("$[0].incomplet").isEqualTo(true);
        client.get().uri(URL + "/{id}", idIncomplete)
                .exchange()
                .expectBody().jsonPath("$.champsManquants").isEqualTo(List.of("adresse", "région"));
        assertThat(idComplete).isNotEqualTo(idIncomplete);
    }

    @Test
    @DisplayName("Listing : une seule recherche dans le code, la raison sociale, le nom commercial, le code postal et la ville")
    void listing_recherche_unique() {
        String marque = suffixe();
        var saisie = saisie("NEGOCIANT", "T" + marque, "Négoce " + suffixe(), "32100");
        saisie.put("nomCommercial", "Enseigne " + marque);
        long id = creer(URL, saisie);

        for (String recherche : List.of("t" + marque, "enseigne " + marque.toLowerCase())) {
            client.get().uri(u -> u.path(URL).queryParam("recherche", recherche).build())
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody().jsonPath("$[*].id").isEqualTo(List.of((int) id));
        }
    }

    @Test
    @DisplayName("RG-OPE-25 et RG-OPE-26 : désactiver, réactiver, puis supprimer un opérateur que rien ne référence")
    void rg_ope_25_26_cycle_de_vie() {
        String ville = "Ville" + suffixe();
        var saisie = saisie("PRODUCTEUR", "T" + suffixe(), "Domaine " + suffixe(), "32100");
        saisie.put("ville", ville);
        long id = creer(URL, saisie);

        client.post().uri(URL + "/{id}/desactivation", id).exchange()
                .expectStatus().isOk().expectBody().jsonPath("$.actif").isEqualTo(false);
        client.get().uri(u -> u.path(URL).queryParam("recherche", ville).queryParam("actif", true).build())
                .exchange()
                .expectBody().jsonPath("$.length()").isEqualTo(0);
        client.post().uri(URL + "/{id}/reactivation", id).exchange()
                .expectStatus().isOk().expectBody().jsonPath("$.actif").isEqualTo(true);

        client.delete().uri(URL + "/{id}", id).exchange().expectStatus().isNoContent();
        client.get().uri(URL + "/{id}", id).exchange().expectStatus().isNotFound();
    }

    @Test
    @DisplayName("RG-OPE-10 à 13 et 21 : onglet Production enregistré, contrôlé, et relu")
    void rg_ope_10_13_production() {
        long id = creer(URL, saisie("NEGOCIANT", "T" + suffixe(), "Négoce " + suffixe(), "32100"));
        long region = region();
        var alambics = List.of(Map.of("numeroAgrement", "AG-12", "type", "ARMAGNACAIS", "capacite", 12.5),
                Map.of("type", "DOUBLE_CHAUFFE"));
        var saisie = production("TRIMESTRIELLE", "SOMMEIL", region, alambics);
        saisie.put("appellations", List.of("BAS_ARMAGNAC", "BLANCHE_ARMAGNAC"));

        client.put().uri(URL + "/{id}/production", id).body(saisie)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.periodicite").isEqualTo("TRIMESTRIELLE")
                .jsonPath("$.periodiciteParDefaut").isEqualTo("MENSUELLE")
                .jsonPath("$.statutDeclarationStock").isEqualTo("SOMMEIL")
                .jsonPath("$.region").isEqualTo(region)
                .jsonPath("$.appellations").isEqualTo(List.of("BAS_ARMAGNAC", "BLANCHE_ARMAGNAC"))
                .jsonPath("$.alambics[0].numeroAgrement").isEqualTo("AG-12")
                .jsonPath("$.alambics[1].type").isEqualTo("DOUBLE_CHAUFFE")
                .jsonPath("$.detenteurAlambic").isEqualTo(true);

        client.put().uri(URL + "/{id}/production", id).body(production(null, "OUI", region, List.of()))
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT)
                .expectBody().jsonPath("$.regle").isEqualTo("RG-OPE-10");
        client.put().uri(URL + "/{id}/production", id)
                .body(production("MENSUELLE", "OUI", region, List.of(Map.of("type", "ARMAGNACAIS", "capacite", -1))))
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT)
                .expectBody().jsonPath("$.regle").isEqualTo("RG-OPE-13");

        client.post().uri("/api/referentiels/regions/{id}/desactivation", region).exchange().expectStatus().isOk();
        // La région déjà portée reste acceptée, même inactive.
        client.put().uri(URL + "/{id}/production", id).body(production("MENSUELLE", "OUI", region, List.of()))
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.detenteurAlambic").isEqualTo(false);
        long autre = creer(URL, saisie("NEGOCIANT", "T" + suffixe(), "Négoce " + suffixe(), "32100"));
        client.put().uri(URL + "/{id}/production", autre).body(production("MENSUELLE", "OUI", region, List.of()))
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT)
                .expectBody().jsonPath("$.regle").isEqualTo("RG-OPE-21");
    }

    @Test
    @DisplayName("RG-OPE-01 et RG-OPE-11 : passer un négociant en AUTRE retire sa périodicité et met son statut à NON")
    void rg_ope_01_11_passage_en_autre() {
        String code = "T" + suffixe();
        long id = creer(URL, saisie("NEGOCIANT", code, "Négoce " + suffixe(), "32100"));

        client.put().uri(URL + "/{id}/identite", id).body(saisie("AUTRE", code, "Courtier " + suffixe(), "32100"))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.periodicite").doesNotExist()
                .jsonPath("$.statutDeclarationStock").isEqualTo("NON");
        client.put().uri(URL + "/{id}/production", id).body(production(null, "OUI", null, List.of()))
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT)
                .expectBody().jsonPath("$.regle").isEqualTo("RG-OPE-11");
    }

    @Test
    @DisplayName("RG-OPE-14 et RG-OPE-15 : diffusion partielle, puis totale qui vide la liste ; accès aux statistiques")
    void rg_ope_14_15_diffusion() {
        long id = creer(URL, saisie("PRODUCTEUR", "T" + suffixe(), "Domaine " + suffixe(), "32100"));

        client.put().uri(URL + "/{id}/diffusion", id)
                .body(Map.of("niveau", "PARTIEL", "millesimes", List.of(2010, 2005), "accesStatistiques", true))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.millesimesDiffusables").isEqualTo(List.of(2005, 2010))
                .jsonPath("$.accesStatistiques").isEqualTo(true);
        client.put().uri(URL + "/{id}/diffusion", id)
                .body(Map.of("niveau", "PARTIEL", "millesimes", List.of(), "accesStatistiques", true))
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT)
                .expectBody().jsonPath("$.regle").isEqualTo("RG-OPE-14");
        client.put().uri(URL + "/{id}/diffusion", id)
                .body(Map.of("niveau", "TOTAL", "millesimes", List.of(2005), "accesStatistiques", false))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.niveauDiffusion").isEqualTo("TOTAL")
                .jsonPath("$.millesimesDiffusables").isEmpty()
                .jsonPath("$.accesStatistiques").isEqualTo(false);
    }

    private static Map<String, Object> production(String periodicite, String statut, Long region, List<?> alambics) {
        Map<String, Object> saisie = new HashMap<>();
        saisie.put("periodicite", periodicite);
        saisie.put("statutDeclarationStock", statut);
        saisie.put("region", region);
        saisie.put("appellations", List.of());
        saisie.put("alambics", alambics);
        return saisie;
    }

    private long region() {
        String code = "R" + suffixe();
        return creer("/api/referentiels/regions", Map.of("code", code, "libelle", "Région " + code, "enRegionDelimitee", true));
    }

    private void attendre422(Map<String, Object> saisie, String regle) {
        client.post().uri(URL).body(saisie)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT)
                .expectBody().jsonPath("$.regle").isEqualTo(regle);
    }

    private String code(long id) {
        Map<?, ?> fiche = client.get().uri(URL + "/{id}", id).exchange()
                .expectStatus().isOk().expectBody(Map.class).returnResult().getResponseBody();
        return (String) fiche.get("code");
    }

    private String codePropose(String type, String codePostal, long pays) {
        Map<?, ?> reponse = client.get().uri(u -> u.path(URL + "/code-propose").queryParam("type", type)
                        .queryParam("codePostal", codePostal).queryParam("pays", pays).build())
                .exchange().expectStatus().isOk().expectBody(Map.class).returnResult().getResponseBody();
        return (String) reponse.get("code");
    }

    private Map<String, Object> saisie(String type, String code, String raisonSociale, String codePostal) {
        Map<String, Object> saisie = new HashMap<>();
        saisie.put("type", type);
        if (code != null) {
            saisie.put("code", code);
        }
        saisie.put("raisonSociale", raisonSociale);
        saisie.put("adresseLigne1", "1 route d'Auch");
        saisie.put("codePostal", codePostal);
        saisie.put("ville", "Condom");
        saisie.put("pays", france);
        saisie.put("accordDiffusionGeneral", false);
        return saisie;
    }

    /** Le pays de ce code ISO, créé s'il n'existe pas encore dans la base partagée. */
    private long pays(String iso, String libelle) {
        return referentiels.paysActifs().stream().filter(p -> p.codeIso().equals(iso)).map(PaysVue::id).findFirst()
                .orElseGet(() -> {
                    String code = "Z" + suffixe();
                    long zone = creer(ZONES, Map.of("code", code, "libelle", "Zone " + code));
                    return creer(PAYS, Map.of("code", iso + "-" + suffixe(), "libelle", libelle, "codeIso", iso,
                            "zoneCommerciale", zone, "membreUe", true));
                });
    }

    private static String suffixe() {
        return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
