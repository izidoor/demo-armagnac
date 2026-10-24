package fr.bnia.notifications.adapter.integration;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.Signature;
import java.security.interfaces.ECPrivateKey;
import java.time.Duration;
import java.time.Instant;

/**
 * Identification du serveur auprès des services push (VAPID, RFC 8292) : un jeton JWT signé ES256 par la clé privée de
 * l'installation, pour l'origine du service, valable douze heures. Sans compte chez Google, Mozilla ni Apple.
 */
final class JetonVapid {

    private static final Duration VALIDITE = Duration.ofHours(12);

    private final String clePublique;
    private final ECPrivateKey clePrivee;
    private final String sujet;

    /**
     * @param clePublique point P-256 non compressé, en base64url
     * @param clePrivee   scalaire P-256 de 32 octets, en base64url
     * @param sujet       contact de l'exploitant, {@code mailto:} ou URL HTTPS (exigé par Apple)
     */
    JetonVapid(String clePublique, String clePrivee, String sujet) throws GeneralSecurityException {
        CourbeP256.publique(CourbeP256.base64url(clePublique));
        this.clePublique = clePublique.strip().replace("=", "");
        this.clePrivee = CourbeP256.privee(CourbeP256.base64url(clePrivee));
        this.sujet = sujet;
    }

    String clePublique() {
        return clePublique;
    }

    /** L'en-tête {@code Authorization} d'une requête vers cette adresse. */
    String entete(String adresse, Instant maintenant) throws GeneralSecurityException {
        URI uri = URI.create(adresse);
        String origine = uri.getScheme() + "://" + uri.getHost() + (uri.getPort() < 0 ? "" : ":" + uri.getPort());
        String enTete = encoder("{\"typ\":\"JWT\",\"alg\":\"ES256\"}");
        String revendications = encoder("{\"aud\":\"" + origine + "\",\"exp\":"
                + maintenant.plus(VALIDITE).getEpochSecond() + ",\"sub\":\"" + sujet.replace("\"", "") + "\"}");
        String signe = enTete + "." + revendications;
        Signature ecdsa = Signature.getInstance("SHA256withECDSAinP1363Format");
        ecdsa.initSign(clePrivee);
        ecdsa.update(signe.getBytes(StandardCharsets.US_ASCII));
        return "vapid t=" + signe + "." + CourbeP256.base64url(ecdsa.sign()) + ", k=" + clePublique;
    }

    private static String encoder(String json) {
        return CourbeP256.base64url(json.getBytes(StandardCharsets.UTF_8));
    }
}
