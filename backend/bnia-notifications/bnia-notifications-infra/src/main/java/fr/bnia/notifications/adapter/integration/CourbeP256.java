package fr.bnia.notifications.adapter.integration;

import java.math.BigInteger;
import java.security.AlgorithmParameters;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.ECPrivateKeySpec;
import java.security.spec.ECPublicKeySpec;
import java.util.Arrays;
import java.util.Base64;

/** Clés P-256 de Web Push, sous leur forme brute en base64url (point non compressé de 65 octets, scalaire de 32). */
final class CourbeP256 {

    static final ECParameterSpec PARAMETRES = parametres();

    private CourbeP256() {
    }

    static byte[] base64url(String valeur) {
        return Base64.getUrlDecoder().decode(valeur.strip().replace("=", ""));
    }

    static String base64url(byte[] octets) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(octets);
    }

    static ECPublicKey publique(byte[] point) throws GeneralSecurityException {
        if (point.length != 65 || point[0] != 0x04) {
            throw new GeneralSecurityException("Clé publique P-256 non compressée attendue (65 octets).");
        }
        ECPoint w = new ECPoint(new BigInteger(1, Arrays.copyOfRange(point, 1, 33)),
                new BigInteger(1, Arrays.copyOfRange(point, 33, 65)));
        return (ECPublicKey) KeyFactory.getInstance("EC").generatePublic(new ECPublicKeySpec(w, PARAMETRES));
    }

    static ECPrivateKey privee(byte[] scalaire) throws GeneralSecurityException {
        return (ECPrivateKey) KeyFactory.getInstance("EC")
                .generatePrivate(new ECPrivateKeySpec(new BigInteger(1, scalaire), PARAMETRES));
    }

    /** Le point non compressé : 0x04, X puis Y sur 32 octets chacun. */
    static byte[] octets(ECPublicKey cle) {
        byte[] point = new byte[65];
        point[0] = 0x04;
        copier(cle.getW().getAffineX(), point, 1);
        copier(cle.getW().getAffineY(), point, 33);
        return point;
    }

    private static void copier(BigInteger valeur, byte[] cible, int debut) {
        byte[] brut = valeur.toByteArray();
        int longueur = Math.min(brut.length, 32);
        System.arraycopy(brut, brut.length - longueur, cible, debut + 32 - longueur, longueur);
    }

    private static ECParameterSpec parametres() {
        try {
            AlgorithmParameters p = AlgorithmParameters.getInstance("EC");
            p.init(new ECGenParameterSpec("secp256r1"));
            return p.getParameterSpec(ECParameterSpec.class);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Courbe P-256 indisponible", e);
        }
    }
}
