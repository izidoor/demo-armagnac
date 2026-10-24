package fr.bnia.notifications.adapter.rest;

import fr.bnia.commun.adapter.rest.Connecte;
import fr.bnia.notifications.application.Acteur;
import fr.bnia.notifications.application.GererAbonnements;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Les notifications push de mes appareils (spec 11 §6), dans Mon compte, au back-office comme au portail : chacun
 * n'abonne et ne désabonne que ses propres appareils (RG-NOT-05).
 */
@RestController
@RequestMapping("/api/notifications")
@Tag(name = "notifications")
class NotificationsController {

    private final GererAbonnements gerer;

    NotificationsController(GererAbonnements gerer) {
        this.gerer = gerer;
    }

    @GetMapping("/cle-publique")
    @Operation(operationId = "clePubliqueNotifications",
            summary = "La clé publique VAPID, pour abonner un appareil ; vide si l'installation n'envoie pas de"
                    + " notification push (RG-NOT-07)")
    ClePubliqueResponse clePublique(Connecte connecte) {
        return new ClePubliqueResponse(gerer.clePublique().orElse(null));
    }

    @PostMapping("/abonnements")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(operationId = "abonnerAppareil",
            summary = "Abonner cet appareil à mes notifications ; une adresse déjà connue passe à moi (RG-NOT-01, 02)")
    void abonner(Connecte connecte, @RequestBody SaisieAbonnementRequest requete) {
        gerer.abonner(acteur(connecte), requete.saisie());
    }

    @PostMapping("/desabonnement")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(operationId = "desabonnerAppareil",
            summary = "Désabonner cet appareil (désactivation, déconnexion) ; sans effet sur l'appareil d'un autre"
                    + " (RG-NOT-05)")
    void desabonner(Connecte connecte, @RequestBody AdresseAbonnementRequest requete) {
        gerer.desabonner(acteur(connecte), requete.adresse());
    }

    @PostMapping("/essai")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(operationId = "essayerNotifications",
            summary = "Envoyer une notification d'essai à tous mes appareils abonnés (RG-NOT-06)")
    void essayer(Connecte connecte) {
        gerer.essayer(acteur(connecte));
    }

    private static Acteur acteur(Connecte connecte) {
        return new Acteur(connecte.utilisateur());
    }
}
