package fr.bnia.notifications.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import fr.bnia.commun.domain.RegleEnfreinte;
import fr.bnia.notifications.api.Notification;
import fr.bnia.notifications.domain.Abonnement;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class NotificationsTest {

    private static final String CLE = "BCVxsr7N_eNgVRqvHtD0zTZsEc6-VV-JvLexhqUzORcxaOzi6-AYWXvTBHm4bjyPjs7Vd8pZGH6SRpkNtoIAiw4";
    private static final String SECRET = "BTBZMqHH6r4Tts7J_aSIgg";
    private static final String TELEPHONE = "https://fcm.googleapis.com/fcm/send/telephone";
    private static final String TABLETTE = "https://web.push.apple.com/tablette";
    private static final Acteur JEANNE = new Acteur(80);
    private static final Acteur HENRI = new Acteur(70);
    private static final Instant MAINTENANT = Instant.parse("2026-10-24T08:00:00Z");

    private final AbonnementsEnMemoire abonnements = new AbonnementsEnMemoire();
    private final PushCaptures push = new PushCaptures();
    private final GererAbonnements gerer = new GererAbonnements(abonnements, push,
            Clock.fixed(MAINTENANT, ZoneOffset.UTC));
    private final EnvoyerNotifications envoyer = new EnvoyerNotifications(abonnements, push);

    private static SaisieAbonnement appareil(String adresse) {
        return new SaisieAbonnement(adresse, CLE, SECRET);
    }

    private static void enfreint(String regle, Runnable appel) {
        assertThatThrownBy(appel::run).isInstanceOf(RegleEnfreinte.class)
                .extracting(e -> ((RegleEnfreinte) e).regle().orElseThrow()).isEqualTo(regle);
    }

    @Test
    @DisplayName("RG-NOT-01 : un utilisateur abonne chacun de ses appareils")
    void rg_not_01_un_abonnement_par_appareil() {
        gerer.abonner(JEANNE, appareil(TELEPHONE));
        gerer.abonner(JEANNE, appareil(TABLETTE));
        assertThat(abonnements.de(Set.of(80L))).extracting(Abonnement::adresse).containsExactly(TELEPHONE, TABLETTE);
        assertThat(abonnements.parAdresse(TELEPHONE).orElseThrow().creeLe()).isEqualTo(MAINTENANT);
    }

    @Test
    @DisplayName("RG-NOT-02 : réabonné par un autre utilisateur, un appareil passe à celui-ci")
    void rg_not_02_adresse_reprise() {
        gerer.abonner(HENRI, appareil(TELEPHONE));
        gerer.abonner(JEANNE, appareil(TELEPHONE));
        assertThat(abonnements.parAdresse.values()).singleElement()
                .extracting(Abonnement::utilisateur).isEqualTo(80L);
    }

    @Test
    @DisplayName("RG-NOT-03 : une notification part vers tous les appareils de tous ses destinataires")
    void rg_not_03_tous_les_appareils() {
        gerer.abonner(JEANNE, appareil(TELEPHONE));
        gerer.abonner(JEANNE, appareil(TABLETTE));
        gerer.abonner(new Acteur(99), appareil("https://updates.push.services.mozilla.com/autre"));
        envoyer.envoyer(new Notification(Set.of(80L, 70L), "Contrat CA-2026-0001 conclu", "Henri a approuvé.",
                "/portail/contrats/1"));
        assertThat(push.envois).singleElement().satisfies(e -> {
            assertThat(e.appareils()).extracting(Abonnement::adresse).containsExactlyInAnyOrder(TELEPHONE, TABLETTE);
            assertThat(e.message()).isEqualTo(new MessagePush("Contrat CA-2026-0001 conclu", "Henri a approuvé.",
                    "/portail/contrats/1"));
        });
    }

    @Test
    @DisplayName("RG-NOT-03 : sans appareil abonné parmi les destinataires, rien ne part")
    void rg_not_03_aucun_appareil() {
        envoyer.envoyer(new Notification(Set.of(80L), "Titre", "Texte", "/"));
        envoyer.envoyer(new Notification(Set.of(), "Titre", "Texte", "/"));
        assertThat(push.envois).isEmpty();
    }

    @Test
    @DisplayName("RG-NOT-04 : un abonnement expiré est retiré")
    void rg_not_04_expire() {
        gerer.abonner(JEANNE, appareil(TELEPHONE));
        new RetirerAbonnementsExpires(abonnements).retirer(TELEPHONE);
        new RetirerAbonnementsExpires(abonnements).retirer(TELEPHONE);
        assertThat(abonnements.parAdresse).isEmpty();
    }

    @Test
    @DisplayName("RG-NOT-05 : on désabonne son appareil")
    void rg_not_05_desabonner() {
        gerer.abonner(JEANNE, appareil(TELEPHONE));
        gerer.abonner(JEANNE, appareil(TABLETTE));
        gerer.desabonner(JEANNE, TELEPHONE);
        assertThat(abonnements.parAdresse.keySet()).containsExactly(TABLETTE);
    }

    @Test
    @DisplayName("RG-NOT-05 : refus, l'appareil d'un autre utilisateur reste abonné")
    void rg_not_05_refus_appareil_d_un_autre() {
        gerer.abonner(HENRI, appareil(TELEPHONE));
        gerer.desabonner(JEANNE, TELEPHONE);
        gerer.desabonner(JEANNE, "https://inconnue.example/x");
        assertThat(abonnements.parAdresse(TELEPHONE)).isPresent();
    }

    @Test
    @DisplayName("RG-NOT-06 : l'essai part vers mes seuls appareils ; sans appareil abonné, il est refusé")
    void rg_not_06_essai() {
        enfreint("RG-NOT-06", () -> gerer.essayer(JEANNE));
        gerer.abonner(JEANNE, appareil(TELEPHONE));
        gerer.abonner(HENRI, appareil(TABLETTE));
        gerer.essayer(JEANNE);
        assertThat(push.envois).singleElement().satisfies(e -> {
            assertThat(e.appareils()).extracting(Abonnement::adresse).containsExactly(TELEPHONE);
            assertThat(e.message().titre()).isEqualTo("Notifications activées");
        });
    }

    @Test
    @DisplayName("RG-NOT-07 : sans clés VAPID, pas de clé publique et pas d'abonnement")
    void rg_not_07_sans_cles() {
        push.cle = Optional.empty();
        assertThat(gerer.clePublique()).isEmpty();
        enfreint("RG-NOT-07", () -> gerer.abonner(JEANNE, appareil(TELEPHONE)));
        assertThat(abonnements.parAdresse).isEmpty();
    }
}
