package fr.bnia.notifications.application;

import fr.bnia.commun.application.UseCase;
import fr.bnia.commun.domain.RegleEnfreinte;
import fr.bnia.notifications.domain.Abonnement;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Les notifications de mes appareils (spec 11 §5.2), dans Mon compte : ouvert à tout utilisateur connecté, sans
 * module (RG-OPE-33). RG-NOT-01, 02, 05 à 07.
 */
@UseCase
public class GererAbonnements {

    private final AbonnementRepository abonnements;
    private final ServicePush service;
    private final Clock horloge;

    public GererAbonnements(AbonnementRepository abonnements, ServicePush service, Clock horloge) {
        this.abonnements = abonnements;
        this.service = service;
        this.horloge = horloge;
    }

    /** RG-NOT-07 : la clé publique VAPID, vide si l'installation n'envoie pas de notification push. */
    public Optional<String> clePublique() {
        return service.clePublique();
    }

    /**
     * RG-NOT-01, 02 : abonne cet appareil ; une adresse déjà connue passe à l'acteur, avec ses nouvelles clés (un
     * appareil où un autre utilisateur s'était abonné sans se déconnecter).
     */
    public void abonner(Acteur acteur, SaisieAbonnement saisie) {
        if (service.clePublique().isEmpty()) {
            throw new RegleEnfreinte("RG-NOT-07", "Cette installation n'envoie pas de notification push.");
        }
        abonnements.enregistrer(Abonnement.nouveau(acteur.utilisateur(), saisie.adresse(), saisie.cleP256dh(),
                saisie.secretAuth(), Instant.now(horloge)));
    }

    /**
     * RG-NOT-05 : désabonne cet appareil (désactivation, déconnexion). Seulement les appareils de l'acteur : l'adresse
     * d'un autre utilisateur, ou inconnue, ne change rien.
     */
    public void desabonner(Acteur acteur, String adresse) {
        abonnements.parAdresse(adresse == null ? "" : adresse.strip())
                .filter(a -> a.utilisateur() == acteur.utilisateur())
                .ifPresent(a -> abonnements.supprimer(a.adresse()));
    }

    /** RG-NOT-06 : une notification d'essai à tous mes appareils abonnés. */
    public void essayer(Acteur acteur) {
        List<Abonnement> appareils = abonnements.de(Set.of(acteur.utilisateur()));
        if (appareils.isEmpty()) {
            throw new RegleEnfreinte("RG-NOT-06", "Aucun de vos appareils n'est abonné aux notifications.");
        }
        service.envoyer(appareils, new MessagePush("Notifications activées",
                "Les contrats et les retiraisons qui vous attendent s'afficheront ici.", "/"));
    }
}
