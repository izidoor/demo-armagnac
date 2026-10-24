package fr.bnia.notifications.adapter.integration;

import fr.bnia.notifications.application.MessagePush;
import fr.bnia.notifications.application.RetirerAbonnementsExpires;
import fr.bnia.notifications.application.ServicePush;
import fr.bnia.notifications.domain.Abonnement;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Web Push par HTTP (RFC 8030, 8291, 8292 ; ADR-024), sans bibliothèque : le message est chiffré pour chaque appareil
 * et posté à son service push, après la validation de la transaction, sur un thread virtuel qui ne retarde pas la
 * réponse. Un abonnement expiré (404, 410) est retiré (RG-NOT-04) ; tout autre échec est journalisé et n'annule rien.
 * Sans clés VAPID configurées, l'installation n'envoie rien (RG-NOT-07).
 */
@Component
class WebPushHttp implements ServicePush {

    private static final Logger LOG = LoggerFactory.getLogger(WebPushHttp.class);
    /** Un message reste un jour chez le service push si l'appareil est éteint. */
    private static final Duration DUREE_DE_VIE = Duration.ofDays(1);

    private final Optional<JetonVapid> vapid;
    private final RetirerAbonnementsExpires expires;
    private final Clock horloge;
    private final ChiffrementWebPush chiffrement = new ChiffrementWebPush();
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();

    WebPushHttp(@Value("${bnia.notifications.vapid.cle-publique:}") String clePublique,
            @Value("${bnia.notifications.vapid.cle-privee:}") String clePrivee,
            @Value("${bnia.notifications.vapid.sujet:}") String sujet, RetirerAbonnementsExpires expires,
            Clock horloge) throws GeneralSecurityException {
        this.vapid = clePublique.isBlank() || clePrivee.isBlank() ? Optional.empty()
                : Optional.of(new JetonVapid(clePublique, clePrivee, sujet));
        this.expires = expires;
        this.horloge = horloge;
        if (vapid.isEmpty()) {
            LOG.info("Notifications push désactivées : aucune clé VAPID (bnia.notifications.vapid).");
        }
    }

    @Override
    public Optional<String> clePublique() {
        return vapid.map(JetonVapid::clePublique);
    }

    @Override
    public void envoyer(List<Abonnement> appareils, MessagePush message) {
        if (vapid.isEmpty()) {
            LOG.info("Notification push non envoyée (aucune clé VAPID) à {} appareil(s) : {}", appareils.size(),
                    message.titre());
            return;
        }
        Runnable envoi = () -> Thread.ofVirtual().name("web-push").start(() -> appareils.forEach(a -> poster(a, message)));
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    envoi.run();
                }
            });
        } else {
            envoi.run();
        }
    }

    private void poster(Abonnement appareil, MessagePush message) {
        try {
            byte[] corps = chiffrement.chiffrer(json(message).getBytes(StandardCharsets.UTF_8),
                    CourbeP256.base64url(appareil.cleP256dh()), CourbeP256.base64url(appareil.secretAuth()));
            HttpRequest requete = HttpRequest.newBuilder(URI.create(appareil.adresse()))
                    .timeout(Duration.ofSeconds(10))
                    .header("Authorization", vapid.orElseThrow().entete(appareil.adresse(), Instant.now(horloge)))
                    .header("TTL", String.valueOf(DUREE_DE_VIE.toSeconds()))
                    .header("Urgency", "high")
                    .header("Content-Encoding", "aes128gcm")
                    .header("Content-Type", "application/octet-stream")
                    .POST(HttpRequest.BodyPublishers.ofByteArray(corps))
                    .build();
            int statut = http.send(requete, HttpResponse.BodyHandlers.discarding()).statusCode();
            if (statut == 404 || statut == 410) {
                LOG.info("Abonnement push expiré, retiré : {}", hote(appareil));
                expires.retirer(appareil.adresse());
            } else if (statut >= 300) {
                LOG.warn("Échec de la notification push « {} » vers {} : HTTP {}", message.titre(), hote(appareil),
                        statut);
            }
        } catch (IOException | GeneralSecurityException | RuntimeException echec) {
            LOG.warn("Échec de la notification push « {} » vers {} : {}", message.titre(), hote(appareil),
                    echec.getMessage());
        } catch (InterruptedException interruption) {
            Thread.currentThread().interrupt();
        }
    }

    /** Le service push, sans l'identifiant de l'appareil. */
    private static String hote(Abonnement appareil) {
        return URI.create(appareil.adresse()).getHost();
    }

    /** Ce que lit le service worker du front ({@code public/sw.js}). */
    static String json(MessagePush message) {
        return "{\"titre\":" + chaine(message.titre()) + ",\"texte\":" + chaine(message.texte()) + ",\"lien\":"
                + chaine(message.lien()) + "}";
    }

    private static String chaine(String texte) {
        StringBuilder json = new StringBuilder("\"");
        for (char c : texte.toCharArray()) {
            switch (c) {
                case '"' -> json.append("\\\"");
                case '\\' -> json.append("\\\\");
                case '\n' -> json.append("\\n");
                default -> {
                    if (c < 0x20) {
                        json.append(String.format("\\u%04x", (int) c));
                    } else {
                        json.append(c);
                    }
                }
            }
        }
        return json.append('"').toString();
    }
}
