package fr.bnia.notifications.domain;

import fr.bnia.commun.domain.RegleEnfreinte;
import java.net.URI;
import java.time.Instant;
import java.util.Base64;
import java.util.Locale;

/**
 * L'abonnement push d'un appareil (spec 11 §3.1, RG-NOT-01) : l'adresse que son navigateur a reçue de son service
 * push (Google, Mozilla, Apple), et les clés qui chiffrent les messages pour lui (RFC 8291). Un utilisateur a un
 * abonnement par appareil où il a activé les notifications ; une adresse n'appartient qu'à un utilisateur.
 *
 * @param adresse    URL HTTPS du service push, propre à l'appareil
 * @param cleP256dh  clé publique de l'appareil, point P-256 non compressé de 65 octets, en base64url
 * @param secretAuth secret d'authentification de 16 octets, en base64url
 */
public record Abonnement(long utilisateur, String adresse, String cleP256dh, String secretAuth, Instant creeLe) {

    private static final String REGLE = "RG-NOT-01";

    /** RG-NOT-01 : un abonnement reçu d'un navigateur, vérifié. */
    public static Abonnement nouveau(long utilisateur, String adresse, String cleP256dh, String secretAuth,
            Instant le) {
        String url = adresse == null ? "" : adresse.strip();
        if (!estHttps(url)) {
            throw new RegleEnfreinte(REGLE, "L'adresse d'un abonnement push est une URL HTTPS.");
        }
        String cle = cleP256dh == null ? "" : cleP256dh.strip();
        byte[] point = decode(cle);
        if (point == null || point.length != 65 || point[0] != 0x04) {
            throw new RegleEnfreinte(REGLE, "La clé de l'appareil n'est pas une clé P-256 valide.");
        }
        String secret = secretAuth == null ? "" : secretAuth.strip();
        byte[] octets = decode(secret);
        if (octets == null || octets.length != 16) {
            throw new RegleEnfreinte(REGLE, "Le secret de l'appareil compte 16 octets.");
        }
        return new Abonnement(utilisateur, url, cle, secret, le);
    }

    private static boolean estHttps(String url) {
        try {
            URI uri = URI.create(url);
            return "https".equals(uri.getScheme() == null ? null : uri.getScheme().toLowerCase(Locale.ROOT))
                    && uri.getHost() != null;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /** Base64url, avec ou sans remplissage ; {@code null} si la valeur n'en est pas. */
    private static byte[] decode(String valeur) {
        try {
            return Base64.getUrlDecoder().decode(valeur.replace("=", ""));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
