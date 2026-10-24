package fr.bnia.notifications.adapter.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.Signature;
import java.time.Instant;
import java.util.Base64;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ChiffrementWebPushTest {

    /** RFC 8291 §5 et annexe A : clés du serveur et de l'appareil, sel, secret, message et résultat. */
    private static final String SERVEUR_PUBLIQUE = "BP4z9KsN6nGRTbVYI_c7VJSPQTBtkgcy27mlmlMoZIIgDll6e3vCYLocInmYWAmS6TlzAC8wEqKK6PBru3jl7A8";
    private static final String SERVEUR_PRIVEE = "yfWPiYE-n46HLnH0KqZOF1fJJU3MYrct3AELtAQ-oRw";
    private static final String APPAREIL_PUBLIQUE = "BCVxsr7N_eNgVRqvHtD0zTZsEc6-VV-JvLexhqUzORcxaOzi6-AYWXvTBHm4bjyPjs7Vd8pZGH6SRpkNtoIAiw4";
    private static final String SECRET = "BTBZMqHH6r4Tts7J_aSIgg";
    private static final String SEL = "DGv6ra1nlYgDCS1FRnbzlw";
    private static final String MESSAGE = "When I grow up, I want to be a watermelon";
    private static final String RESULTAT = "DGv6ra1nlYgDCS1FRnbzlwAAEABBBP4z9KsN6nGRTbVYI_c7VJSPQTBtkgcy27ml"
            + "mlMoZIIgDll6e3vCYLocInmYWAmS6TlzAC8wEqKK6PBru3jl7A_yl95bQpu6cVPT"
            + "pK4Mqgkf1CXztLVBSt2Ks3oZwbuwXPXLWyouBWLVWGNWQexSgSxsj_Qulcy4a-fN";

    @Test
    @DisplayName("RFC 8291 : le message de l'exemple donne exactement le corps de l'exemple")
    void exemple_rfc_8291() throws GeneralSecurityException {
        KeyPair serveur = new KeyPair(CourbeP256.publique(CourbeP256.base64url(SERVEUR_PUBLIQUE)),
                CourbeP256.privee(CourbeP256.base64url(SERVEUR_PRIVEE)));
        byte[] corps = ChiffrementWebPush.chiffrer(MESSAGE.getBytes(StandardCharsets.US_ASCII),
                CourbeP256.base64url(APPAREIL_PUBLIQUE), CourbeP256.base64url(SECRET), serveur, CourbeP256.base64url(SEL));
        assertThat(CourbeP256.base64url(corps)).isEqualTo(RESULTAT);
    }

    @Test
    @DisplayName("Chaque message a sa clé éphémère et son sel : deux chiffrements diffèrent")
    void sel_et_cle_ephemeres() throws GeneralSecurityException {
        ChiffrementWebPush chiffrement = new ChiffrementWebPush();
        byte[] message = MESSAGE.getBytes(StandardCharsets.US_ASCII);
        byte[] premier = chiffrement.chiffrer(message, CourbeP256.base64url(APPAREIL_PUBLIQUE), CourbeP256.base64url(SECRET));
        byte[] second = chiffrement.chiffrer(message, CourbeP256.base64url(APPAREIL_PUBLIQUE), CourbeP256.base64url(SECRET));
        assertThat(premier).hasSize(RESULTAT.length() * 3 / 4).isNotEqualTo(second);
    }

    @Test
    @DisplayName("Un message trop long pour un enregistrement est refusé")
    void message_trop_long() {
        assertThatThrownBy(() -> new ChiffrementWebPush().chiffrer(new byte[ChiffrementWebPush.TAILLE_MAXIMALE + 1],
                CourbeP256.base64url(APPAREIL_PUBLIQUE), CourbeP256.base64url(SECRET)))
                .isInstanceOf(GeneralSecurityException.class);
    }

    @Test
    @DisplayName("RFC 8292 : le jeton VAPID vise l'origine du service push, expire dans douze heures, et sa signature"
            + " se vérifie avec la clé publique")
    void jeton_vapid() throws GeneralSecurityException {
        JetonVapid vapid = new JetonVapid(SERVEUR_PUBLIQUE, SERVEUR_PRIVEE, "https://bnia-demo.example");
        Instant maintenant = Instant.parse("2026-10-24T08:00:00Z");
        String entete = vapid.entete("https://fcm.googleapis.com/fcm/send/abc:def", maintenant);
        assertThat(entete).startsWith("vapid t=").endsWith(", k=" + SERVEUR_PUBLIQUE);

        String jeton = entete.substring("vapid t=".length(), entete.indexOf(", k="));
        String[] parties = jeton.split("\\.");
        assertThat(parties).hasSize(3);
        assertThat(texte(parties[0])).isEqualTo("{\"typ\":\"JWT\",\"alg\":\"ES256\"}");
        assertThat(texte(parties[1])).isEqualTo("{\"aud\":\"https://fcm.googleapis.com\",\"exp\":"
                + maintenant.plusSeconds(12 * 3600).getEpochSecond() + ",\"sub\":\"https://bnia-demo.example\"}");

        Signature ecdsa = Signature.getInstance("SHA256withECDSAinP1363Format");
        ecdsa.initVerify(CourbeP256.publique(CourbeP256.base64url(SERVEUR_PUBLIQUE)));
        ecdsa.update((parties[0] + "." + parties[1]).getBytes(StandardCharsets.US_ASCII));
        assertThat(ecdsa.verify(CourbeP256.base64url(parties[2]))).isTrue();
    }

    @Test
    @DisplayName("La charge utile est le JSON lu par le service worker, échappé")
    void charge_utile() {
        assertThat(WebPushHttp.json(new fr.bnia.notifications.application.MessagePush("Contrat « CA-1 »",
                "Il a dit \"oui\"\\\n", "/portail/contrats/1")))
                .isEqualTo("{\"titre\":\"Contrat « CA-1 »\",\"texte\":\"Il a dit \\\"oui\\\"\\\\\\n\","
                        + "\"lien\":\"/portail/contrats/1\"}");
    }

    private static String texte(String base64url) {
        return new String(Base64.getUrlDecoder().decode(base64url), StandardCharsets.UTF_8);
    }
}
