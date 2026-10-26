package fr.bnia.boot.sauvegarde;

import fr.bnia.operateurs.domain.partage.Role;
import fr.bnia.support.AbstractIT;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** La copie de la base et des documents, pour un ADMIN du BNIA seul. */
class SauvegardeIT extends AbstractIT {

    @Test
    @DisplayName("Un ADMIN du BNIA télécharge la base et les documents ; un AGENT du BNIA est refusé")
    void admin_bnia_seul() {
        client.get().uri("/api/sauvegarde/base").exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals("Content-Disposition", "attachment; filename=\"bnia.dump\"");
        client.get().uri("/api/sauvegarde/documents").exchange()
                .expectStatus().isOk()
                .expectHeader().contentType("application/zip");

        String agentBnia = emailUnique("agent-bnia");
        membre(operateurs.operateurBnia(), Role.AGENT, agentBnia);
        connecte(agentBnia).get().uri("/api/sauvegarde/base").exchange().expectStatus().isForbidden();
        connecte(agentBnia).get().uri("/api/sauvegarde/documents").exchange().expectStatus().isForbidden();
        anonyme().get().uri("/api/sauvegarde/base").exchange().expectStatus().isUnauthorized();
    }
}
