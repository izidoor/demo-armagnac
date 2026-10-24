package fr.bnia.contratsarmagnac.application;

import fr.bnia.commun.domain.ElementIntrouvable;
import fr.bnia.contratsarmagnac.domain.Auteur;
import fr.bnia.contratsarmagnac.domain.ContratAchatArmagnac;
import fr.bnia.contratsarmagnac.domain.Fait;
import fr.bnia.contratsarmagnac.domain.Partie;
import fr.bnia.operateurs.application.partage.OperateursLecture;
import fr.bnia.operateurs.domain.partage.Acteur;
import java.time.Clock;
import java.time.Instant;
import java.util.List;

/**
 * Chargement et enregistrement d'un contrat, partagés par les cas d'usage d'écriture : après l'enregistrement, ses
 * faits vont au journal (RG-CA-19), aux e-mails et aux notifications push de l'autre partie (RG-CA-18).
 */
final class Registre {

    private final ContratRepository contrats;
    private final JournalContrats journal;
    private final OperateursLecture operateurs;
    private final Courrier courrier;
    private final Clock horloge;

    Registre(ContratRepository contrats, JournalContrats journal, EnvoiCourriels envoi,
            EnvoiNotifications notifications, OperateursLecture operateurs, ParametresContrats parametres,
            Clock horloge) {
        this.contrats = contrats;
        this.journal = journal;
        this.operateurs = operateurs;
        this.courrier = new Courrier(envoi, notifications, operateurs, parametres);
        this.horloge = horloge;
    }

    ContratAchatArmagnac charger(long id) {
        return contrats.parId(id).orElseThrow(() -> new ElementIntrouvable("Contrat introuvable."));
    }

    /** L'auteur d'une action : sa partie, l'utilisateur et la copie de son nom, l'instant (ADR-011). */
    Auteur auteur(Acteur acteur, Partie partie) {
        return new Auteur(partie, acteur.utilisateur(), operateurs.nomUtilisateur(acteur.utilisateur()).orElse("?"),
                Instant.now(horloge));
    }

    ContratAchatArmagnac inserer(ContratAchatArmagnac contrat) {
        List<Fait> faits = contrat.releverFaits();
        return enregistre(contrats.inserer(contrat), faits);
    }

    ContratAchatArmagnac mettreAJour(ContratAchatArmagnac contrat) {
        List<Fait> faits = contrat.releverFaits();
        return enregistre(contrats.mettreAJour(contrat), faits);
    }

    /** RG-CA-18, 19 : journal, e-mails et push des faits, une fois le contrat enregistré (son identifiant est connu). */
    private ContratAchatArmagnac enregistre(ContratAchatArmagnac contrat, List<Fait> faits) {
        faits.forEach(f -> {
            journal.ajouter(contrat.id(), f);
            courrier.notifier(contrat, f);
        });
        return contrat;
    }
}
