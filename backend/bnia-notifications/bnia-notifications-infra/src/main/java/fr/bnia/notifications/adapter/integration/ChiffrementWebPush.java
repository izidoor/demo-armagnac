package fr.bnia.notifications.adapter.integration;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.interfaces.ECPublicKey;
import java.util.Arrays;
import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.Mac;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Chiffrement d'un message Web Push pour un appareil (RFC 8291, codage {@code aes128gcm} de RFC 8188) : une clé
 * éphémère et un sel par message, un seul enregistrement. Vérifié sur l'exemple de l'annexe A de RFC 8291.
 */
final class ChiffrementWebPush {

    private static final int TAILLE_ENREGISTREMENT = 4096;
    /** Le message, son délimiteur et l'étiquette GCM tiennent dans un enregistrement. */
    static final int TAILLE_MAXIMALE = TAILLE_ENREGISTREMENT - 1 - 16;

    private final SecureRandom aleatoire = new SecureRandom();

    /** @return le corps de la requête au service push : en-tête RFC 8188, puis le message chiffré */
    byte[] chiffrer(byte[] clair, byte[] cleAppareil, byte[] secretAuth) throws GeneralSecurityException {
        KeyPairGenerator generateur = KeyPairGenerator.getInstance("EC");
        generateur.initialize(CourbeP256.PARAMETRES, aleatoire);
        byte[] sel = new byte[16];
        aleatoire.nextBytes(sel);
        return chiffrer(clair, cleAppareil, secretAuth, generateur.generateKeyPair(), sel);
    }

    static byte[] chiffrer(byte[] clair, byte[] cleAppareil, byte[] secretAuth, KeyPair ephemere, byte[] sel)
            throws GeneralSecurityException {
        if (clair.length > TAILLE_MAXIMALE) {
            throw new GeneralSecurityException("Message push trop long : " + clair.length + " octets.");
        }
        byte[] clePubliqueServeur = CourbeP256.octets((ECPublicKey) ephemere.getPublic());
        KeyAgreement accord = KeyAgreement.getInstance("ECDH");
        accord.init(ephemere.getPrivate());
        accord.doPhase(CourbeP256.publique(cleAppareil), true);
        byte[] secretEcdh = accord.generateSecret();

        byte[] prkCle = hmac(secretAuth, secretEcdh);
        byte[] infoCle = concat(ascii("WebPush: info\0"), cleAppareil, clePubliqueServeur);
        byte[] ikm = hmac(prkCle, concat(infoCle, new byte[] {1}));
        byte[] prk = hmac(sel, ikm);
        byte[] cek = Arrays.copyOf(hmac(prk, concat(ascii("Content-Encoding: aes128gcm\0"), new byte[] {1})), 16);
        byte[] nonce = Arrays.copyOf(hmac(prk, concat(ascii("Content-Encoding: nonce\0"), new byte[] {1})), 12);

        Cipher aes = Cipher.getInstance("AES/GCM/NoPadding");
        aes.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(cek, "AES"), new GCMParameterSpec(128, nonce));
        byte[] chiffre = aes.doFinal(concat(clair, new byte[] {2}));

        ByteBuffer entete = ByteBuffer.allocate(16 + 4 + 1 + clePubliqueServeur.length);
        entete.put(sel).putInt(TAILLE_ENREGISTREMENT).put((byte) clePubliqueServeur.length).put(clePubliqueServeur);
        return concat(entete.array(), chiffre);
    }

    private static byte[] hmac(byte[] cle, byte[] donnees) throws GeneralSecurityException {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(cle, "HmacSHA256"));
        return mac.doFinal(donnees);
    }

    private static byte[] ascii(String texte) {
        return texte.getBytes(StandardCharsets.US_ASCII);
    }

    private static byte[] concat(byte[]... parties) {
        ByteArrayOutputStream tout = new ByteArrayOutputStream();
        for (byte[] partie : parties) {
            tout.writeBytes(partie);
        }
        return tout.toByteArray();
    }
}
