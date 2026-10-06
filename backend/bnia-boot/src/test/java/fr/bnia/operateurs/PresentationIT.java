package fr.bnia.operateurs;

import fr.bnia.support.AbstractIT;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

/** Présentation de l'opérateur : chaîne complète, sur des valeurs de référentiels créées par le test. */
class PresentationIT extends AbstractIT {

    private static final String REFERENTIELS = "/api/referentiels/";

    private long operateur;
    private String url;

    @BeforeEach
    void operateur() {
        var saisie = new HashMap<String, Object>();
        saisie.put("type", "PRODUCTEUR");
        saisie.put("code", "T" + suffixe());
        saisie.put("raisonSociale", "Domaine " + suffixe());
        saisie.put("accordDiffusionGeneral", false);
        operateur = creer("/api/operateurs", saisie);
        url = "/api/operateurs/" + operateur + "/presentation";
    }

    @Test
    @DisplayName("RG-OPE-16 à 19 : présentation vide, puis enregistrée et relue, cartes vidées sans paiement par carte")
    void rg_ope_16_19_enregistrer_et_relire() {
        client.get().uri(url).exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.marques").isEmpty().jsonPath("$.paiementCarte").isEqualTo(false);

        long marque = valeur("marques");
        long circuit = valeur("circuits-distribution");
        long visite = valeur("types-visite");
        long label = valeur("labels");
        long langue = valeur("langues");
        long produit = valeur("produits-commercialises");
        var presentation = presentation(
                List.of(Map.of("portee", "FRANCE", "marque", marque, "diffusableWeb", true),
                        Map.of("portee", "EXPORT", "marque", marque, "diffusableWeb", false)),
                List.of(Map.of("portee", "EXPORT", "circuit", circuit)));
        presentation.put("visites", List.of(Map.of("typeVisite", visite, "commentaire", "Sur rendez-vous")));
        presentation.put("labels", List.of(Map.of("label", label)));
        presentation.put("cartes", List.of("VISA"));
        presentation.put("langues", List.of(langue));
        presentation.put("produitsCommercialises", List.of(produit));
        presentation.put("modePresentation", "ARMAGNAC_EXCLUSIVEMENT");
        presentation.put("presentationFr", "Un domaine familial.");
        presentation.put("medailles", List.of(Map.of("concours", "Concours général agricole", "annee", 2024,
                "distinction", "OR", "produit", "Hors d'âge"), Map.of("concours", "Spirits Selection")));

        client.put().uri(url).body(presentation).exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.cartes").isEmpty();
        client.get().uri(url).exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.marques.length()").isEqualTo(2)
                .jsonPath("$.marques[0].diffusableWeb").isEqualTo(true)
                .jsonPath("$.circuits[0].portee").isEqualTo("EXPORT")
                .jsonPath("$.visites[0].commentaire").isEqualTo("Sur rendez-vous")
                .jsonPath("$.labels[0].label").isEqualTo(label)
                .jsonPath("$.langues").isEqualTo(List.of((int) langue))
                .jsonPath("$.modePresentation").isEqualTo("ARMAGNAC_EXCLUSIVEMENT")
                .jsonPath("$.medailles[0].annee").isEqualTo(2024)
                .jsonPath("$.medailles[1].distinction").doesNotExist();

        presentation.put("paiementCarte", true);
        presentation.put("medailles", List.of());
        client.put().uri(url).body(presentation).exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.cartes").isEqualTo(List.of("VISA")).jsonPath("$.medailles").isEmpty();
    }

    @Test
    @DisplayName("RG-OPE-16 et RG-OPE-21 : doublon de marque et marque inactive refusés en 422")
    void rg_ope_16_21_refus() {
        long marque = valeur("marques");
        client.put().uri(url).body(presentation(List.of(Map.of("portee", "FRANCE", "marque", marque, "diffusableWeb", true),
                        Map.of("portee", "FRANCE", "marque", marque, "diffusableWeb", false)), List.of()))
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT)
                .expectBody().jsonPath("$.regle").isEqualTo("RG-OPE-16");

        client.post().uri(REFERENTIELS + "marques/{id}/desactivation", marque).exchange().expectStatus().isOk();
        client.put().uri(url).body(presentation(List.of(Map.of("portee", "FRANCE", "marque", marque, "diffusableWeb", true)),
                        List.of()))
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT)
                .expectBody().jsonPath("$.regle").isEqualTo("RG-OPE-21");
    }

    @Test
    @DisplayName("RG-OPE-25 : supprimer l'opérateur supprime sa présentation")
    void rg_ope_25_suppression_avec_presentation() {
        client.put().uri(url).body(presentation(List.of(), List.of())).exchange().expectStatus().isOk();

        client.delete().uri("/api/operateurs/{id}", operateur).exchange().expectStatus().isNoContent();
        client.get().uri(url).exchange().expectStatus().isNotFound();
    }

    private static Map<String, Object> presentation(List<?> marques, List<?> circuits) {
        var p = new HashMap<String, Object>();
        p.put("marques", marques);
        p.put("circuits", circuits);
        p.put("visites", List.of());
        p.put("labels", List.of());
        p.put("paiementCarte", false);
        p.put("cartes", List.of());
        p.put("degustationGratuite", true);
        p.put("lieuDeVente", false);
        p.put("accueilGroupes", false);
        p.put("adhesionOenotourisme", false);
        p.put("langues", List.of());
        p.put("produitsCommercialises", List.of());
        p.put("medailles", List.of());
        return p;
    }

    private long valeur(String liste) {
        String code = "V" + suffixe();
        return creer(REFERENTIELS + liste, Map.of("code", code, "libelle", "Valeur " + code));
    }

    private static String suffixe() {
        return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
