package fr.bnia.declarations.application;

import fr.bnia.commun.application.UseCase;
import fr.bnia.commun.domain.ElementIntrouvable;
import fr.bnia.declarations.domain.ActionJournalDeclarations;
import fr.bnia.declarations.domain.CalendrierDeclaratif;
import fr.bnia.declarations.domain.CalendrierDeclaratif.Situation;
import fr.bnia.declarations.domain.DeclarationStocksMouvements;
import fr.bnia.declarations.domain.LigneStockInitial;
import fr.bnia.declarations.domain.PeriodeDeclaration;
import fr.bnia.declarations.domain.PointDeDepartDeclaratif;
import fr.bnia.declarations.domain.SaisieDsm;
import fr.bnia.operateurs.application.partage.OperateursLecture;
import fr.bnia.operateurs.domain.partage.Acteur;
import fr.bnia.operateurs.domain.partage.OperateurVue;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * DSM d'un opérateur (analyse UC-DEC-4, spec 05 §5.1) : l'opérateur l'initie pour la période attendue, la
 * remplit et l'enregistre en brouillon ; l'opérateur ou le BNIA supprime un brouillon. Lot 1 : RG-DEC-01 à
 * 05, 10 à 13, 15 à 19, 22, 23.
 */
@UseCase
public class GererDsm {

    private final DsmRepository dsms;
    private final PointDeDepartRepository points;
    private final DsmLecture lecture;
    private final JournalDeclarations journal;
    private final Autorisations autorisations;
    private final Journaliste journaliste;

    public GererDsm(DsmRepository dsms, PointDeDepartRepository points, DsmLecture lecture,
            JournalDeclarations journal, OperateursLecture operateurs, Clock horloge) {
        this.dsms = dsms;
        this.points = points;
        this.lecture = lecture;
        this.journal = journal;
        this.autorisations = new Autorisations(operateurs);
        this.journaliste = new Journaliste(journal, operateurs, horloge);
    }

    /** RG-DEC-02 à 05 : la prochaine DSM de l'opérateur, ou ce qui en empêche l'initiation (spec 05 §6.2). */
    public Situation situation(Acteur acteur, long operateur) {
        OperateurVue vue = autorisations.exigerLecture(acteur, operateur);
        return situation(vue);
    }

    private Situation situation(OperateurVue vue) {
        return CalendrierDeclaratif.prochaine(vue.type(), vue.statutDeclarationStock(), points.enVigueur(vue.id()),
                dsms.dsmsDe(vue.id()), LocalDate.now(journaliste.horloge()));
    }

    /** RG-DEC-01 à 05, 12, 13, 23 : l'opérateur initie la DSM de la période attendue, reprise du stock initial. */
    public VueDsm initier(Acteur acteur, long operateur) {
        OperateurVue vue = autorisations.exigerOperateur(acteur, operateur);
        PeriodeDeclaration periode = situation(vue).exigerInitiable();
        PointDeDepartDeclaratif point = points.enVigueur(operateur).orElseThrow();
        List<LigneStockInitial> stock = periode.debut().equals(point.debut()) ? point.lignes() : List.of();
        DeclarationStocksMouvements dsm = dsms.inserer(DeclarationStocksMouvements.initier(operateur, vue.type(),
                vue.statutDeclarationStock(), periode, stock));
        journaliste.noter(acteur, dsm, ActionJournalDeclarations.DSM_INITIEE, null);
        return VueDsm.de(dsm, vue);
    }

    /** RG-DEC-01, 16 à 18 : la DSM, ses totaux et ses anomalies. */
    public VueDsm consulter(Acteur acteur, long operateur, long id) {
        OperateurVue vue = autorisations.exigerLecture(acteur, operateur);
        return VueDsm.de(dsm(operateur, id), vue);
    }

    /**
     * Vue provisoire (décision du 2026-10-13) : la DSM telle qu'elle serait enregistrée, avec ses anomalies,
     * sans rien enregistrer ni journaliser. Pour l'opérateur qui la saisit.
     */
    public VueDsm apercevoir(Acteur acteur, long operateur, long id, SaisieDsm saisie) {
        OperateurVue vue = autorisations.exigerOperateur(acteur, operateur);
        return VueDsm.de(dsm(operateur, id).apercu(saisie), vue);
    }

    /** RG-DEC-01, 10 à 13, 15 à 19, 21, 23 : l'opérateur enregistre sa saisie ; les anomalies ne l'empêchent pas. */
    public VueDsm enregistrer(Acteur acteur, long operateur, long id, SaisieDsm saisie, Long version) {
        OperateurVue vue = autorisations.exigerOperateur(acteur, operateur);
        DeclarationStocksMouvements dsm = dsm(operateur, id);
        dsm.enregistrer(saisie, version);
        DeclarationStocksMouvements enregistree = dsms.mettreAJour(dsm);
        journaliste.noter(acteur, enregistree, ActionJournalDeclarations.DSM_ENREGISTREE, null);
        return VueDsm.de(enregistree, vue);
    }

    /**
     * RG-DEC-01, 22, 23 : seul un brouillon se supprime, par l'opérateur ou par le BNIA, avec son journal ; une
     * entrée « brouillon supprimé » reste au journal de l'opérateur.
     */
    public void supprimer(Acteur acteur, long operateur, long id) {
        autorisations.exigerGestion(acteur, operateur);
        DeclarationStocksMouvements dsm = dsm(operateur, id);
        dsm.exigerSupprimable();
        journal.supprimerDeLaDsm(id);
        dsms.supprimer(id);
        journaliste.noter(acteur, dsm, ActionJournalDeclarations.BROUILLON_SUPPRIME, null);
    }

    /** RG-DEC-01 : les DSM de l'opérateur, de la plus récente à la plus ancienne. */
    public List<DsmLigne> lister(Acteur acteur, long operateur) {
        autorisations.exigerLecture(acteur, operateur);
        return lecture.deLOperateur(operateur);
    }

    /** H-DEC-8 : les dénominations déjà employées par l'opérateur, suggérées à la saisie. */
    public List<String> denominations(Acteur acteur, long operateur) {
        autorisations.exigerLecture(acteur, operateur);
        return lecture.denominations(operateur);
    }

    /** RG-DEC-01, 23 : le journal d'une DSM. */
    public List<EntreeJournal> journal(Acteur acteur, long operateur, long id) {
        autorisations.exigerLecture(acteur, operateur);
        dsm(operateur, id);
        return journal.deLaDsm(id);
    }

    /** RG-DEC-01, 23 : le journal de l'opérateur, brouillons supprimés compris. */
    public List<EntreeJournal> journalDeLOperateur(Acteur acteur, long operateur) {
        autorisations.exigerLecture(acteur, operateur);
        return journal.deLOperateur(operateur);
    }

    private DeclarationStocksMouvements dsm(long operateur, long id) {
        Optional<DeclarationStocksMouvements> dsm = dsms.parId(id).filter(d -> d.operateur() == operateur);
        return dsm.orElseThrow(() -> new ElementIntrouvable("DSM introuvable : n° " + id + "."));
    }
}
