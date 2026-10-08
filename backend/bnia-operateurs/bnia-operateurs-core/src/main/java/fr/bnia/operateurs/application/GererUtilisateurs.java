package fr.bnia.operateurs.application;

import fr.bnia.commun.application.UseCase;
import fr.bnia.commun.domain.ElementIntrouvable;
import fr.bnia.operateurs.domain.ActionJournal;
import fr.bnia.operateurs.domain.Changement;
import fr.bnia.operateurs.domain.ChampJournal;
import fr.bnia.operateurs.domain.Rattachement;
import fr.bnia.operateurs.domain.StatutUtilisateur;
import fr.bnia.operateurs.domain.TypeCible;
import fr.bnia.operateurs.domain.Utilisateur;
import fr.bnia.operateurs.domain.partage.Acteur;
import fr.bnia.operateurs.domain.partage.Module;
import fr.bnia.referentiels.application.partage.ReferentielsLecture;
import java.time.Clock;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Utilisateurs, au back-office (vision UC-05, spec 02 §6.1 menu Accès) : liste (RG-OPE-53), blocage et
 * déblocage par un ADMIN du BNIA (RG-OPE-50).
 */
@UseCase
public class GererUtilisateurs {

    private final UtilisateurRepository utilisateurs;
    private final RattachementRepository rattachements;
    private final UtilisateursLecture lecture;
    private final Habilitations habilitations;
    private final Journaliste journaliste;
    private final Courrier courrier;

    public GererUtilisateurs(UtilisateurRepository utilisateurs, RattachementRepository rattachements,
            UtilisateursLecture lecture, JournalActions journal, EnvoiCourriels envoi, Habilitations habilitations,
            ParametresOperateurs parametres, ReferentielsLecture referentiels, Clock horloge) {
        this.utilisateurs = utilisateurs;
        this.rattachements = rattachements;
        this.lecture = lecture;
        this.habilitations = habilitations;
        this.journaliste = new Journaliste(journal, utilisateurs, referentiels, horloge);
        this.courrier = new Courrier(envoi, parametres);
    }

    /** RG-OPE-32, 53 : rôles de l'opérateur BNIA, module Accès en lecture. */
    public List<UtilisateurLigne> lister(Acteur acteur, FiltreUtilisateurs filtre) {
        habilitations.exigerBnia(acteur, Module.ACCES, false);
        return lecture.lister(filtre);
    }

    /** RG-OPE-32, 53. */
    public UtilisateurLigne consulter(Acteur acteur, long id) {
        habilitations.exigerBnia(acteur, Module.ACCES, false);
        return lecture.utilisateur(id).orElseThrow(() -> new ElementIntrouvable("Utilisateur " + id + " introuvable."));
    }

    /**
     * RG-OPE-34, 50 : motif obligatoire, conservé au journal ; effet immédiat dans tous les opérateurs
     * (connexions refusées, session coupée : RG-AUT-09) ; e-mail à l'utilisateur et aux ADMIN de ses
     * opérateurs, sans le motif.
     */
    public Utilisateur bloquer(Acteur acteur, long id, String motif) {
        habilitations.exigerAdminBnia(acteur);
        Utilisateur utilisateur = utilisateur(id);
        utilisateur.bloquer(motif);
        utilisateurs.mettreAJour(utilisateur);
        journaliste.noter(acteur, null, ActionJournal.UTILISATEUR_BLOQUE, TypeCible.UTILISATEUR, id,
                utilisateur.identite().nomComplet(), List.of(
                        new Changement(ChampJournal.STATUT, StatutUtilisateur.ACTIF.name(), StatutUtilisateur.BLOQUE.name()),
                        new Changement(ChampJournal.MOTIF, null, motif.strip())));
        prevenir(utilisateur, true);
        return utilisateur;
    }

    /** RG-OPE-34, 50 : rétablit la situation précédente ; mêmes e-mails qu'au blocage. */
    public Utilisateur debloquer(Acteur acteur, long id) {
        habilitations.exigerAdminBnia(acteur);
        Utilisateur utilisateur = utilisateur(id);
        utilisateur.debloquer();
        utilisateurs.mettreAJour(utilisateur);
        journaliste.noter(acteur, null, ActionJournal.UTILISATEUR_DEBLOQUE, TypeCible.UTILISATEUR, id,
                utilisateur.identite().nomComplet(), List.of(
                        new Changement(ChampJournal.STATUT, StatutUtilisateur.BLOQUE.name(), StatutUtilisateur.ACTIF.name())));
        prevenir(utilisateur, false);
        return utilisateur;
    }

    /** RG-OPE-50 : l'utilisateur et les ADMIN actifs de chacun de ses opérateurs, une fois chacun. */
    private void prevenir(Utilisateur utilisateur, boolean bloque) {
        Set<String> destinataires = new LinkedHashSet<>();
        destinataires.add(utilisateur.email().valeur());
        for (Rattachement sien : rattachements.deLUtilisateur(utilisateur.id())) {
            rattachements.deLOperateur(sien.operateur()).stream()
                    .filter(r -> r.estAdminActif() && r.utilisateur() != utilisateur.id())
                    .flatMap(r -> utilisateurs.parId(r.utilisateur()).stream())
                    .forEach(admin -> destinataires.add(admin.email().valeur()));
        }
        destinataires.forEach(d -> courrier.blocage(d, utilisateur.identite().nomComplet(), bloque));
    }

    private Utilisateur utilisateur(long id) {
        return utilisateurs.parId(id).orElseThrow(() -> new ElementIntrouvable("Utilisateur " + id + " introuvable."));
    }
}
