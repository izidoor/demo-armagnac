package fr.bnia.declarations.application;

import fr.bnia.declarations.domain.ActionJournalDeclarations;
import fr.bnia.declarations.domain.Auteur;
import fr.bnia.declarations.domain.DeclarationStocksMouvements;
import fr.bnia.operateurs.application.partage.OperateursLecture;
import fr.bnia.operateurs.domain.partage.Acteur;
import java.time.Clock;
import java.time.Instant;

/**
 * Écrit le journal des déclarations (RG-DEC-23) : date, acteur et période copiés. Partagé par les cas
 * d'usage du contexte.
 */
final class Journaliste {

    private final JournalDeclarations journal;
    private final OperateursLecture operateurs;
    private final Clock horloge;

    Journaliste(JournalDeclarations journal, OperateursLecture operateurs, Clock horloge) {
        this.journal = journal;
        this.operateurs = operateurs;
        this.horloge = horloge;
    }

    void noter(Acteur acteur, DeclarationStocksMouvements dsm, ActionJournalDeclarations action, String detail) {
        Auteur auteur = auteur(acteur);
        journal.ajouter(new EntreeJournal(maintenant(), auteur.utilisateur(), auteur.nom(), dsm.operateur(), dsm.id(),
                dsm.periode().libelle(), action, detail));
    }

    /** Action sur le point de départ, sans DSM. */
    void noter(Acteur acteur, long operateur, ActionJournalDeclarations action, String detail) {
        Auteur auteur = auteur(acteur);
        journal.ajouter(new EntreeJournal(maintenant(), auteur.utilisateur(), auteur.nom(), operateur, null, null, action,
                detail));
    }

    /** Copie du nom de l'acteur, à l'instant de l'action (ADR-011 amendé). */
    Auteur auteur(Acteur acteur) {
        return new Auteur(acteur.utilisateur(), operateurs.nomUtilisateur(acteur.utilisateur())
                .orElse("Utilisateur n° " + acteur.utilisateur()));
    }

    Instant maintenant() {
        return horloge.instant();
    }

    Clock horloge() {
        return horloge;
    }
}
