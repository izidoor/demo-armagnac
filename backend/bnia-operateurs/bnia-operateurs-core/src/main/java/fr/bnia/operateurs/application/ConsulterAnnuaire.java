package fr.bnia.operateurs.application;

import fr.bnia.commun.application.UseCase;
import fr.bnia.operateurs.domain.TypeCible;
import fr.bnia.operateurs.domain.partage.Acteur;
import fr.bnia.operateurs.domain.partage.Droits;
import fr.bnia.operateurs.domain.partage.Module;
import java.util.List;

/**
 * Lectures des personnes d'un opérateur (vision UC-03, 17) : l'annuaire (RG-OPE-52), le journal
 * (RG-OPE-34), et les droits de l'acteur pour les menus du front (« mes droits », RG-OPE-31).
 */
@UseCase
public class ConsulterAnnuaire {

    private final AnnuaireLecture annuaire;
    private final JournalActions journal;
    private final Habilitations habilitations;

    public ConsulterAnnuaire(AnnuaireLecture annuaire, JournalActions journal, Habilitations habilitations) {
        this.annuaire = annuaire;
        this.journal = journal;
        this.habilitations = habilitations;
    }

    /** RG-OPE-31, 52 : tout membre de l'opérateur ; les rôles de l'opérateur BNIA, pour tout opérateur. */
    public List<PersonneAnnuaire> annuaire(Acteur acteur, long operateur) {
        habilitations.exigerLecture(acteur, operateur, Module.CONTACTS);
        return annuaire.annuaire(operateur);
    }

    /** RG-OPE-31, 34 : les actions sur les personnes et les accès de l'opérateur, du plus récent au plus ancien. */
    public List<LigneJournal> journal(Acteur acteur, long operateur) {
        habilitations.exigerLecture(acteur, operateur, Module.ACCES);
        return journal.deLOperateur(operateur);
    }

    /** RG-OPE-31, 34 : le journal d'une personne de l'opérateur (contact, membre ou invitation). */
    public List<LigneJournal> journalDe(Acteur acteur, long operateur, TypeCible cible, long id) {
        habilitations.exigerLecture(acteur, operateur, Module.ACCES);
        return journal.deLaCible(cible, id).stream()
                .filter(l -> l.operateur() != null && l.operateur() == operateur)
                .toList();
    }

    /** RG-OPE-31 : les droits de l'acteur sur l'opérateur, aucun s'il n'y a pas accès. */
    public Droits mesDroits(Acteur acteur, long operateur) {
        return habilitations.droits(acteur, operateur);
    }
}
