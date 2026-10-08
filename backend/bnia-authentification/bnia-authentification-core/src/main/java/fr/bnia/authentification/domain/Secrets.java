package fr.bnia.authentification.domain;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

/**
 * Jetons et codes : tirés au hasard, transmis en clair une seule fois (e-mail, cookie), conservés en
 * empreinte SHA-256 (RG-AUT-05, 06, 08). Le mot de passe, lui, est haché lentement (RG-AUT-07).
 */
public final class Secrets {

    private static final SecureRandom ALEA = new SecureRandom();

    private Secrets() {
    }

    /** Un jeton de lien ou de cookie, 256 bits. */
    public static String jeton() {
        byte[] octets = new byte[32];
        ALEA.nextBytes(octets);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(octets);
    }

    /** Un code de vérification à 6 chiffres (RG-AUT-01). */
    public static String code() {
        return "%06d".formatted(ALEA.nextInt(1_000_000));
    }

    public static String empreinte(String clair) {
        try {
            byte[] hache = MessageDigest.getInstance("SHA-256").digest(clair.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hache);
        } catch (NoSuchAlgorithmException impossible) {
            throw new IllegalStateException(impossible);
        }
    }

    /** Comparaison en temps constant de deux empreintes. */
    static boolean egales(String a, String b) {
        return a != null && b != null
                && MessageDigest.isEqual(a.getBytes(StandardCharsets.UTF_8), b.getBytes(StandardCharsets.UTF_8));
    }
}
