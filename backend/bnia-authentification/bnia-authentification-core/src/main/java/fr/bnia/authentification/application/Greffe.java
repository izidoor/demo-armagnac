package fr.bnia.authentification.application;

import fr.bnia.authentification.domain.EvenementConnexion;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Journal des connexions et e-mails, communs aux cas d'usage du contexte (RG-AUT-08, 12) : jamais de mot
 * de passe ni de code au journal.
 */
final class Greffe {

    static final DateTimeFormatter HEURE = DateTimeFormatter.ofPattern("HH:mm", Locale.FRANCE)
            .withZone(ZoneId.of("Europe/Paris"));

    private final JournalConnexions journal;
    private final EnvoiCourriels envoi;
    private final ParametresAuthentification parametres;
    private final Clock horloge;

    Greffe(JournalConnexions journal, EnvoiCourriels envoi, ParametresAuthentification parametres, Clock horloge) {
        this.journal = journal;
        this.envoi = envoi;
        this.parametres = parametres;
        this.horloge = horloge;
    }

    Instant maintenant() {
        return horloge.instant();
    }

    void noter(String identifiantSaisi, Long utilisateur, EvenementConnexion evenement, String detail, Contexte contexte) {
        journal.ajouter(new LigneConnexion(maintenant(), identifiantSaisi == null ? "" : identifiantSaisi, utilisateur,
                evenement, detail, contexte.adresseIp(), contexte.navigateur()));
    }

    /** RG-AUT-01, 05. */
    void code(String destinataire, String code) {
        envoi.envoyer(new Courriel(destinataire, "Votre code de connexion",
                "Votre code de connexion à la plateforme du BNIA : " + code + "\n\n"
                        + "Il est valable " + parametres.politique().validiteCode().toMinutes()
                        + " minutes, une seule fois. Si vous n'avez pas demandé à vous connecter, ignorez ce message.\n"));
    }

    /** RG-AUT-08. */
    void reinitialisation(String destinataire, String jeton) {
        envoi.envoyer(new Courriel(destinataire, "Réinitialiser votre mot de passe",
                "Pour choisir un nouveau mot de passe, ouvrez ce lien : " + parametres.urlPublique()
                        + "/reinitialisation?jeton=" + URLEncoder.encode(jeton, StandardCharsets.UTF_8) + "\n\n"
                        + "Il est valable " + parametres.politique().validiteLien().toMinutes()
                        + " minutes, une seule fois. Si vous n'avez rien demandé, ignorez ce message.\n"));
    }
}
