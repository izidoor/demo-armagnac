package fr.bnia.referentiels;

import fr.bnia.support.AbstractIT;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpStatus;

/**
 * Contrat commun des neuf listes simples (A-REF-3) : REST, cas d'usage, Spring Data JDBC, PostgreSQL.
 * La base est partagée entre les IT : chaque test crée ses propres codes et libellés (les marques
 * exigent un libellé unique, RG-REF-15).
 */
class ListesSimplesIT extends AbstractIT {

    private static final String RACINE = "/api/referentiels/";

    @ListesSimples
    void creer_consulter_modifier_desactiver_reactiver(String liste) {
        String url = RACINE + liste;
        String code = codeUnique();

        long id = creer(url, Map.of("code", code, "libelle", "Valeur " + code));

        client.put().uri(url + "/{id}", id).body(Map.of("code", code + "-C", "libelle", "Valeur modifiée " + code))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(id)
                .jsonPath("$.code").isEqualTo(code + "-C")
                .jsonPath("$.libelle").isEqualTo("Valeur modifiée " + code);

        client.post().uri(url + "/{id}/desactivation", id)
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.actif").isEqualTo(false);

        client.get().uri(url + "/{id}", id)
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.actif").isEqualTo(false).jsonPath("$.code").isEqualTo(code + "-C");

        client.post().uri(url + "/{id}/reactivation", id)
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.actif").isEqualTo(true);
    }

    @ListesSimples
    @DisplayName("RG-REF-01 : un code déjà pris, à la casse près, est refusé en 409, à la création comme à la modification")
    void rg_ref_01_code_en_double(String liste) {
        String url = RACINE + liste;
        String code = codeUnique();
        creer(url, Map.of("code", code, "libelle", "Valeur " + code));
        long autre = creer(url, Map.of("code", code + "-B", "libelle", "Autre " + code));

        client.post().uri(url).body(Map.of("code", code.toLowerCase(), "libelle", "Doublon " + code))
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT)
                .expectBody().jsonPath("$.regle").isEqualTo("RG-REF-01");

        client.put().uri(url + "/{id}", autre).body(Map.of("code", code, "libelle", "Autre " + code))
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT)
                .expectBody().jsonPath("$.regle").isEqualTo("RG-REF-01");
    }

    @ListesSimples
    @DisplayName("RG-REF-03 : un libellé vide est refusé en 422")
    void rg_ref_03_libelle_vide(String liste) {
        client.post().uri(RACINE + liste).body(Map.of("code", codeUnique(), "libelle", " "))
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT)
                .expectBody().jsonPath("$.regle").isEqualTo("RG-REF-03");
    }

    @ListesSimples
    @DisplayName("RG-REF-04 : le filtre actif écarte les valeurs désactivées")
    void rg_ref_04_filtre_actif(String liste) {
        String url = RACINE + liste;
        String code = codeUnique();
        long actif = creer(url, Map.of("code", code + "A", "libelle", "Filtre " + code + " actif"));
        long inactif = creer(url, Map.of("code", code + "I", "libelle", "Filtre " + code + " inactif"));
        client.post().uri(url + "/{id}/desactivation", inactif).exchange().expectStatus().isOk();

        client.get().uri(u -> u.path(url).queryParam("actif", true).queryParam("recherche", "filtre " + code).build())
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.length()").isEqualTo(1).jsonPath("$[0].id").isEqualTo(actif);

        client.get().uri(u -> u.path(url).queryParam("recherche", "FILTRE " + code).build())
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.length()").isEqualTo(2);
    }

    @ListesSimples
    @DisplayName("RG-REF-04 : une valeur que rien n'utilise se supprime")
    void rg_ref_04_supprimer(String liste) {
        String url = RACINE + liste;
        String code = codeUnique();
        long id = creer(url, Map.of("code", code, "libelle", "À supprimer " + code));

        client.delete().uri(url + "/{id}", id).exchange().expectStatus().isNoContent();

        client.get().uri(url + "/{id}", id).exchange().expectStatus().isNotFound();
    }

    @ListesSimples
    void une_valeur_inconnue_renvoie_404(String liste) {
        client.get().uri(RACINE + liste + "/{id}", Long.MAX_VALUE)
                .exchange()
                .expectStatus().isNotFound();
    }

    private static String codeUnique() {
        return "Z" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    /** Les neuf listes simples, par leur chemin sous {@code /api/referentiels/}. */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = {"zones-commerciales", "titres", "labels", "types-visite", "langues", "centres-interet",
            "marques", "circuits-distribution", "produits-commercialises"})
    @interface ListesSimples {
    }
}
