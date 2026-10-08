package fr.bnia.authentification.application;

import java.util.Optional;

/**
 * Issue d'une étape de connexion. Un refus n'est pas une exception : le cas d'usage enregistre l'échec
 * (compteur, blocage, journal) et la transaction doit le conserver (RG-AUT-03, 12) ; le controller
 * traduit le refus en erreur.
 */
public sealed interface Resultat {

    /** RG-AUT-01 : mot de passe vérifié, un code est parti par e-mail. */
    record CodeEnvoye(long utilisateur) implements Resultat {
    }

    /**
     * La session peut s'ouvrir.
     *
     * @param jetonConfiance jeton du cookie d'un navigateur mis en confiance (RG-AUT-06)
     */
    record Connecte(long utilisateur, Optional<String> jetonConfiance) implements Resultat {
    }

    /** Refusé, avec la règle et le message destiné à l'utilisateur. */
    record Refuse(String regle, String message) implements Resultat {
    }
}
