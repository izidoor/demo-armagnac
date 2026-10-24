package fr.bnia.notifications;

import static org.assertj.core.api.Assertions.assertThat;

import fr.bnia.notifications.application.AbonnementRepository;
import fr.bnia.notifications.application.MessagePush;
import fr.bnia.notifications.domain.Abonnement;
import fr.bnia.operateurs.domain.partage.Role;
import fr.bnia.support.AbstractIT;
import fr.bnia.support.PushCaptures;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.client.RestTestClient;

/** Spec 11 : abonnements push des appareils, depuis Mon compte, et leur suppression avec l'utilisateur. */
class NotificationsIT extends AbstractIT {

    private static final String CLE = "BCVxsr7N_eNgVRqvHtD0zTZsEc6-VV-JvLexhqUzORcxaOzi6-AYWXvTBHm4bjyPjs7Vd8pZGH6SRpkNtoIAiw4";
    private static final String SECRET = "BTBZMqHH6r4Tts7J_aSIgg";

    @Autowired
    private PushCaptures push;
    @Autowired
    private AbonnementRepository abonnements;

    private long operateur;
    private long jeanne;
    private RestTestClient sessionJeanne;
    private RestTestClient sessionHenri;

    @BeforeEach
    void deuxMembres() {
        operateur = nouvelOperateur();
        String emailJeanne = emailUnique("jeanne");
        jeanne = membre(operateur, Role.AGENT, emailJeanne);
        sessionJeanne = connecte(emailJeanne);
        String emailHenri = emailUnique("henri");
        membre(operateur, Role.AGENT, emailHenri);
        sessionHenri = connecte(emailHenri);
    }

    private static String appareil() {
        return "https://fcm.googleapis.com/fcm/send/" + UUID.randomUUID();
    }

    private static void abonner(RestTestClient session, String adresse) {
        session.post().uri("/api/notifications/abonnements")
                .body(Map.of("adresse", adresse, "cleP256dh", CLE, "secretAuth", SECRET))
                .exchange().expectStatus().isNoContent();
    }

    @Test
    @DisplayName("RG-NOT-07 : la clé publique VAPID est donnée à l'utilisateur connecté, pas à un anonyme")
    void rg_not_07_cle_publique() {
        sessionJeanne.get().uri("/api/notifications/cle-publique").exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.clePublique").isEqualTo(PushCaptures.CLE_PUBLIQUE);
        anonyme().get().uri("/api/notifications/cle-publique").exchange().expectStatus().isUnauthorized();
    }

    @Test
    @DisplayName("RG-NOT-01, 06 : deux appareils abonnés reçoivent l'essai ; une adresse en HTTP est refusée")
    void rg_not_01_deux_appareils_et_essai() {
        String telephone = appareil();
        String ordinateur = appareil();
        abonner(sessionJeanne, telephone);
        abonner(sessionJeanne, ordinateur);
        sessionJeanne.post().uri("/api/notifications/essai").exchange().expectStatus().isNoContent();

        assertThat(push.recusPar(telephone)).extracting(MessagePush::titre).containsExactly("Notifications activées");
        assertThat(push.recusPar(ordinateur)).extracting(MessagePush::titre).containsExactly("Notifications activées");

        sessionJeanne.post().uri("/api/notifications/abonnements")
                .body(Map.of("adresse", "http://exemple.fr/push", "cleP256dh", CLE, "secretAuth", SECRET))
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT)
                .expectBody().jsonPath("$.regle").isEqualTo("RG-NOT-01");
    }

    @Test
    @DisplayName("RG-NOT-06 : sans appareil abonné, l'essai est refusé")
    void rg_not_06_essai_sans_appareil() {
        sessionJeanne.post().uri("/api/notifications/essai").exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT)
                .expectBody().jsonPath("$.regle").isEqualTo("RG-NOT-06");
    }

    @Test
    @DisplayName("RG-NOT-02, 05 : un appareil réabonné passe au nouvel utilisateur ; on ne désabonne que le sien")
    void rg_not_02_05_reprise_et_desabonnement() {
        String telephone = appareil();
        abonner(sessionHenri, telephone);
        abonner(sessionJeanne, telephone);
        assertThat(abonnements.parAdresse(telephone)).map(Abonnement::utilisateur).contains(jeanne);

        sessionHenri.post().uri("/api/notifications/desabonnement").body(Map.of("adresse", telephone))
                .exchange().expectStatus().isNoContent();
        assertThat(abonnements.parAdresse(telephone)).isPresent();

        sessionJeanne.post().uri("/api/notifications/desabonnement").body(Map.of("adresse", telephone))
                .exchange().expectStatus().isNoContent();
        assertThat(abonnements.parAdresse(telephone)).isEmpty();
    }

    @Test
    @DisplayName("Spec 11 §3.1 : les abonnements d'un utilisateur supprimé disparaissent avec lui")
    void abonnements_supprimes_avec_l_utilisateur() {
        String telephone = appareil();
        abonner(sessionJeanne, telephone);
        client.post().uri("/api/operateurs/{op}/membres/{id}/retrait", operateur, rattachement(operateur, jeanne))
                .body(Map.of("conserverContact", false))
                .exchange().expectStatus().isNoContent();
        assertThat(abonnements.de(Set.of(jeanne))).isEmpty();
        assertThat(abonnements.parAdresse(telephone)).isEmpty();
    }
}
