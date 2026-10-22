package fr.bnia.boot.suivi;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HexFormat;
import java.util.Optional;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

/** Écriture des événements du suivi ({@link SuiviConfiguration}), venus du front ou de {@link SuiviFichiers}. */
@Component
class JournalSuivi {

    enum Type {
        /** Signalés par le front. */
        ECRAN, OFFRE, PRESENCE,
        /** Relevés par le serveur, avec le nom du fichier. */
        TELECHARGEMENT, IMPORT
    }

    enum Appareil {
        ORDINATEUR, TABLETTE, MOBILE;

        /** À défaut d'un événement du front dans la session (un iPad sous iPadOS passe ici pour un ordinateur). */
        static Appareil selon(String userAgent) {
            String ua = userAgent == null ? "" : userAgent;
            if (ua.contains("iPad") || ua.contains("Tablet") || (ua.contains("Android") && !ua.contains("Mobile"))) {
                return TABLETTE;
            }
            return ua.contains("Mobi") || ua.contains("iPhone") || ua.contains("Android") ? MOBILE : ORDINATEUR;
        }
    }

    /** {@code statut} : réponse HTTP d'un import ou d'un téléchargement, null pour un événement du front. */
    record Evenement(Type type, String lieu, String detail, Integer statut) {
    }

    private final JdbcClient jdbc;
    private final Clock horloge;

    JournalSuivi(JdbcClient jdbc, Clock horloge) {
        this.jdbc = jdbc;
        this.horloge = horloge;
    }

    /**
     * Enregistre l'événement, avec une copie de l'e-mail de l'utilisateur : les identifiants changent à chaque remise
     * à zéro de la démonstration. Utilisateur introuvable : rien.
     */
    void enregistrer(long utilisateur, String session, Appareil appareil, Evenement evenement) {
        Optional<String> email = jdbc.sql("select email from operateurs.utilisateur where id = :id")
                .param("id", utilisateur)
                .query(String.class).optional();
        email.ifPresent(e -> jdbc.sql("""
                        insert into suivi.evenement (instant, session, email, appareil, type, lieu, detail, statut)
                        values (:instant, :session, :email, :appareil, :type, :lieu, :detail, :statut)""")
                .param("instant", OffsetDateTime.ofInstant(horloge.instant(), ZoneOffset.UTC))
                .param("session", empreinte(session))
                .param("email", e)
                .param("appareil", appareil.name())
                .param("type", evenement.type().name())
                .param("lieu", evenement.lieu())
                .param("detail", evenement.detail())
                .param("statut", evenement.statut())
                .update());
    }

    /** L'appareil du dernier événement du front dans cette session, sinon celui que dit le user-agent. */
    Appareil appareil(String session, String userAgent) {
        return jdbc.sql("select appareil from suivi.evenement where session = :session order by instant desc limit 1")
                .param("session", empreinte(session))
                .query(String.class).optional()
                .map(Appareil::valueOf)
                .orElseGet(() -> Appareil.selon(userAgent));
    }

    /** Regroupe les événements d'une session sans conserver son identifiant, qui l'ouvrirait. */
    private static String empreinte(String session) {
        try {
            byte[] hache = MessageDigest.getInstance("SHA-256").digest(session.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hache, 0, 8);
        } catch (NoSuchAlgorithmException impossible) {
            throw new IllegalStateException(impossible);
        }
    }
}
