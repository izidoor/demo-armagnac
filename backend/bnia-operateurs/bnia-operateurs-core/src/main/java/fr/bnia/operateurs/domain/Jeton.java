package fr.bnia.operateurs.domain;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

/**
 * Jeton d'un lien envoyé par e-mail (invitation, changement d'e-mail) : aléatoire, transmis en clair une
 * seule fois, conservé sous forme d'empreinte SHA-256 (RG-OPE-40, vision §2.4 règle 2).
 *
 * @param clair    à mettre dans le lien, jamais enregistré
 * @param empreinte à enregistrer
 */
public record Jeton(String clair, String empreinte) {

    private static final SecureRandom ALEA = new SecureRandom();

    public static Jeton nouveau() {
        byte[] octets = new byte[32];
        ALEA.nextBytes(octets);
        String clair = Base64.getUrlEncoder().withoutPadding().encodeToString(octets);
        return new Jeton(clair, empreinte(clair));
    }

    /** L'empreinte d'un jeton reçu, à comparer à celle enregistrée. */
    public static String empreinte(String clair) {
        try {
            byte[] hache = MessageDigest.getInstance("SHA-256").digest(clair.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hache);
        } catch (NoSuchAlgorithmException impossible) {
            throw new IllegalStateException(impossible);
        }
    }
}
