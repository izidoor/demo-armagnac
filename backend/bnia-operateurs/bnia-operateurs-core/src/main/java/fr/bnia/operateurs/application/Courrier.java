package fr.bnia.operateurs.application;

import fr.bnia.operateurs.domain.partage.Role;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Rédige les e-mails du contexte et les confie à l'envoi, qui part après la validation de la
 * transaction (RG-OPE-40). Les liens visent le front ({@link ParametresOperateurs#urlPublique()}).
 */
final class Courrier {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("d MMMM yyyy 'à' HH:mm", Locale.FRANCE)
            .withZone(ZoneId.of("Europe/Paris"));

    private final EnvoiCourriels envoi;
    private final ParametresOperateurs parametres;

    Courrier(EnvoiCourriels envoi, ParametresOperateurs parametres) {
        this.envoi = envoi;
        this.parametres = parametres;
    }

    /** RG-OPE-40, 41 : le lien d'activation, pour cet e-mail seulement. */
    void invitation(String email, String nomOperateur, Role role, String jeton, Instant expireLe, String invitant) {
        envoi.envoyer(new Courriel(email, "Invitation à rejoindre " + nomOperateur + " sur la plateforme du BNIA",
                invitant + " vous invite à accéder à la plateforme du BNIA pour « " + nomOperateur + " », avec le rôle "
                        + role.libelle() + ".\n\n"
                        + "Pour accepter, ouvrez ce lien : " + lien("/invitation", jeton) + "\n\n"
                        + "Ce lien est personnel et valable jusqu'au " + DATE.format(expireLe) + ".\n"));
    }

    /** RG-OPE-48 : le lien de confirmation, envoyé au nouvel e-mail. */
    void changementEmail(String nouvel, String jeton, Instant expireLe) {
        envoi.envoyer(new Courriel(nouvel, "Confirmez votre nouvel e-mail",
                "Vous avez demandé à utiliser cette adresse pour la plateforme du BNIA.\n\n"
                        + "Pour confirmer, ouvrez ce lien : " + lien("/changement-email", jeton) + "\n\n"
                        + "Ce lien est valable jusqu'au " + DATE.format(expireLe) + ". Sans confirmation, rien ne change.\n"));
    }

    /** RG-OPE-48 : la notification envoyée à l'ancien e-mail. */
    void emailChange(String ancien, String nouvel) {
        envoi.envoyer(new Courriel(ancien, "Votre e-mail a changé",
                "L'e-mail de votre compte sur la plateforme du BNIA est désormais " + nouvel + ".\n"
                        + "Si vous n'êtes pas à l'origine de ce changement, contactez le BNIA.\n"));
    }

    /** RG-OPE-50 : à l'utilisateur et aux ADMIN de ses opérateurs, sans le motif. */
    void blocage(String destinataire, String nomUtilisateur, boolean bloque) {
        String sujet = bloque ? "Compte bloqué par le BNIA" : "Compte débloqué par le BNIA";
        String texte = bloque
                ? "Le compte de " + nomUtilisateur + " sur la plateforme du BNIA a été bloqué par le BNIA : il ne permet"
                        + " plus de se connecter. Pour en savoir plus, contactez le BNIA.\n"
                : "Le compte de " + nomUtilisateur + " sur la plateforme du BNIA a été débloqué : il permet de nouveau"
                        + " de se connecter.\n";
        envoi.envoyer(new Courriel(destinataire, sujet, texte));
    }

    private String lien(String chemin, String jeton) {
        return parametres.urlPublique() + chemin + "?jeton=" + URLEncoder.encode(jeton, StandardCharsets.UTF_8);
    }
}
