package fr.bnia.boot;

import fr.bnia.support.AbstractIT;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

/** La réponse à l'appel d'offres et le diaporama de la soutenance, servis après connexion, affichables dans une iframe. */
class OffreIT extends AbstractIT {

    @Test
    @DisplayName("L'offre exige une session ; ses sections s'affichent dans une iframe de la même origine")
    void offre_apres_connexion() {
        anonyme().get().uri("/api/offre/sections.json").exchange().expectStatus().isUnauthorized();

        client.get().uri("/api/offre/sections.json").exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.sections[0].id").isEqualTo("00-garde")
                .jsonPath("$.pdf").isEqualTo("reponse-ao-armagnac-py-pollet.pdf");
        client.get().uri("/api/offre/01-synthese.html").exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML)
                .expectHeader().valueEquals("X-Frame-Options", "SAMEORIGIN");
        client.get().uri("/api/offre/inconnue.html").exchange().expectStatus().isNotFound();
    }

    @Test
    @DisplayName("Le diaporama exige une session ; ses diapositives s'affichent dans une iframe de la même origine")
    void diaporama_apres_connexion() {
        anonyme().get().uri("/api/offre/diaporama/diaporama.json").exchange().expectStatus().isUnauthorized();

        client.get().uri("/api/offre/diaporama/diaporama.json").exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.diapositives[0].id").isEqualTo("01-titre")
                .jsonPath("$.pdf").isEqualTo("diaporama-py-pollet.pdf");
        client.get().uri("/api/offre/diaporama/01-titre.html").exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML)
                .expectHeader().valueEquals("X-Frame-Options", "SAMEORIGIN");
    }
}
