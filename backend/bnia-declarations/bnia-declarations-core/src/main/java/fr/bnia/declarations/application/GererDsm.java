package fr.bnia.declarations.application;

import fr.bnia.commun.application.StockageDocuments;
import fr.bnia.commun.application.UseCase;
import fr.bnia.commun.domain.ElementIntrouvable;
import fr.bnia.declarations.domain.ActionJournalDeclarations;
import fr.bnia.declarations.domain.CalendrierDeclaratif.Situation;
import fr.bnia.declarations.domain.CalendrierDeclaratif;
import fr.bnia.declarations.domain.Commentaire;
import fr.bnia.declarations.domain.ControlePieces;
import fr.bnia.declarations.domain.Cote;
import fr.bnia.declarations.domain.DeclarationStocksMouvements;
import fr.bnia.declarations.domain.EtatDsm;
import fr.bnia.declarations.domain.Justificatif;
import fr.bnia.declarations.domain.LigneDsm;
import fr.bnia.declarations.domain.LigneStockInitial;
import fr.bnia.declarations.domain.Manque;
import fr.bnia.declarations.domain.PeriodeDeclaration;
import fr.bnia.declarations.domain.PointDeDepartDeclaratif;
import fr.bnia.declarations.domain.SaisieDsm;
import fr.bnia.declarations.domain.Vieillissement;
import fr.bnia.declarations.domain.partage.ComplementsDemandes;
import fr.bnia.declarations.domain.partage.DsmEnvoyee;
import fr.bnia.declarations.domain.partage.DsmValidee;
import fr.bnia.operateurs.application.partage.OperateursLecture;
import fr.bnia.operateurs.domain.partage.Acteur;
import fr.bnia.operateurs.domain.partage.OperateurVue;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * DSM d'un opérateur (analyse UC-DEC-4, 6, 8, 9, spec 05 §5.1) : l'opérateur l'initie pour la période
 * attendue, la remplit, l'enregistre et l'envoie ; le BNIA la prend en instruction, demande des compléments ou
 * la valide ; l'opérateur ou le BNIA supprime un brouillon. RG-DEC-01 à 05, 10 à 23.
 */
@UseCase
public class GererDsm {

    private final DsmRepository dsms;
    private final PointDeDepartRepository points;
    private final DsmLecture lecture;
    private final JournalDeclarations journal;
    private final Commentaires commentaires;
    private final Pieces pieces;
    private final StockageDocuments stockage;
    private final PublicationEvenements evenements;
    private final Autorisations autorisations;
    private final Journaliste journaliste;

    public GererDsm(DsmRepository dsms, PointDeDepartRepository points, DsmLecture lecture,
            JournalDeclarations journal, Commentaires commentaires, Pieces pieces, StockageDocuments stockage,
            PublicationEvenements evenements,
            OperateursLecture operateurs, Clock horloge) {
        this.dsms = dsms;
        this.points = points;
        this.lecture = lecture;
        this.journal = journal;
        this.commentaires = commentaires;
        this.pieces = pieces;
        this.stockage = stockage;
        this.evenements = evenements;
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

    /**
     * RG-DEC-01 à 05, 12 à 14, 23 : l'opérateur initie la DSM de la période attendue. Elle reprend le stock
     * initial du point de départ, ou le stock de fin de la DSM validée qui la précède, vieilli au 1er avril.
     */
    public VueDsm initier(Acteur acteur, long operateur) {
        OperateurVue vue = autorisations.exigerOperateur(acteur, operateur);
        PeriodeDeclaration periode = situation(vue).exigerInitiable();
        DeclarationStocksMouvements dsm = dsms.inserer(DeclarationStocksMouvements.initier(operateur, vue.type(),
                vue.statutDeclarationStock(), periode, stockDeDepart(operateur, periode)));
        journaliste.noter(acteur, dsm, ActionJournalDeclarations.DSM_INITIEE, null);
        return vue(acteur, dsm, vue);
    }

    /** RG-DEC-13, 14 : le stock initial pour la première période, sinon la reprise de la DSM précédente. */
    private List<LigneStockInitial> stockDeDepart(long operateur, PeriodeDeclaration periode) {
        PointDeDepartDeclaratif point = points.enVigueur(operateur).orElseThrow();
        if (periode.debut().equals(point.debut())) {
            return point.lignes();
        }
        LocalDate veille = periode.debut().minusDays(1);
        return dsms.dsmsDe(operateur).stream()
                .filter(d -> d.etat() == EtatDsm.VALIDEE && d.periode().fin().equals(veille))
                .findFirst()
                .flatMap(d -> dsms.parId(d.id()))
                .map(precedente -> Vieillissement.reprise(precedente, periode.debut()))
                .orElse(List.of());
    }

    /** RG-DEC-01, 16 à 18 : la DSM, ses totaux, ses anomalies et les actions de l'acteur. */
    public VueDsm consulter(Acteur acteur, long operateur, long id) {
        OperateurVue vue = autorisations.exigerLecture(acteur, operateur);
        return vue(acteur, dsm(operateur, id), vue);
    }

    /**
     * Vue provisoire (décision du 2026-10-13) : la DSM telle qu'elle serait enregistrée, avec ses anomalies,
     * sans rien enregistrer ni journaliser. Pour l'opérateur qui la saisit.
     */
    public VueDsm apercevoir(Acteur acteur, long operateur, long id, SaisieDsm saisie) {
        OperateurVue vue = autorisations.exigerOperateur(acteur, operateur);
        return vue(acteur, dsm(operateur, id).apercu(saisie), vue);
    }

    /**
     * RG-DEC-01, 10 à 13, 15 à 19, 21, 23 : l'opérateur enregistre sa saisie ; les anomalies ne l'empêchent pas.
     * Les commentaires et les justificatifs d'une ligne supprimée passent sur la DSM (décision du 2026-10-13).
     */
    public VueDsm enregistrer(Acteur acteur, long operateur, long id, SaisieDsm saisie, Long version) {
        OperateurVue vue = autorisations.exigerOperateur(acteur, operateur);
        DeclarationStocksMouvements dsm = dsm(operateur, id);
        Map<Long, LigneDsm> avant = dsm.lignes().stream()
                .collect(Collectors.toMap(LigneDsm::id, Function.identity()));
        dsm.enregistrer(saisie, version);
        DeclarationStocksMouvements enregistree = dsms.mettreAJour(dsm);
        enregistree.lignes().forEach(l -> avant.remove(l.id()));
        if (!avant.isEmpty()) {
            for (Commentaire commentaire : commentaires.deLaDsm(id)) {
                LigneDsm supprimee = commentaire.ligne() == null ? null : avant.get(commentaire.ligne());
                if (supprimee != null) {
                    commentaire.detacher(supprimee.identite().libelle());
                    commentaires.mettreAJour(commentaire);
                }
            }
            for (Justificatif justificatif : pieces.justificatifsDe(id)) {
                LigneDsm supprimee = justificatif.ligne() == null ? null : avant.get(justificatif.ligne());
                if (supprimee != null) {
                    justificatif.detacher(supprimee.identite().libelle());
                    pieces.mettreAJour(justificatif);
                }
            }
        }
        journaliste.noter(acteur, enregistree, ActionJournalDeclarations.DSM_ENREGISTREE, null);
        return vue(acteur, enregistree, vue);
    }

    /** RG-DEC-01, 18, 20, 23 : l'opérateur envoie sa DSM sans anomalie, ou la renvoie après compléments. */
    public VueDsm envoyer(Acteur acteur, long operateur, long id, Long version) {
        OperateurVue vue = autorisations.exigerOperateur(acteur, operateur);
        DeclarationStocksMouvements dsm = dsm(operateur, id);
        boolean renvoi = dsm.envoyer(journaliste.maintenant(), version);
        DeclarationStocksMouvements envoyee = dsms.mettreAJour(dsm);
        journaliste.noter(acteur, envoyee, ActionJournalDeclarations.DSM_ENVOYEE,
                renvoi ? "Renvoi après compléments" : null);
        evenements.publier(new DsmEnvoyee(id, operateur, envoyee.periode().debut(), envoyee.periode().fin(), renvoi));
        return vue(acteur, envoyee, vue);
    }

    /** RG-DEC-01, 20, 23 : le BNIA prend en instruction une DSM envoyée. */
    public VueDsm prendreEnInstruction(Acteur acteur, long operateur, long id, Long version) {
        OperateurVue vue = autorisations.exigerBnia(acteur, operateur);
        DeclarationStocksMouvements dsm = dsm(operateur, id);
        dsm.prendreEnInstruction(version);
        DeclarationStocksMouvements prise = dsms.mettreAJour(dsm);
        journaliste.noter(acteur, prise, ActionJournalDeclarations.DSM_PRISE_EN_INSTRUCTION, null);
        return vue(acteur, prise, vue);
    }

    /**
     * RG-DEC-01, 20, 23, 24 : le BNIA demande des compléments, avec un motif obligatoire, écrit en commentaire
     * de la DSM ; elle revient à l'opérateur.
     */
    public VueDsm demanderComplements(Acteur acteur, long operateur, long id, String motif, Long version) {
        OperateurVue vue = autorisations.exigerBnia(acteur, operateur);
        DeclarationStocksMouvements dsm = dsm(operateur, id);
        Commentaire commentaire = Commentaire.motif(id, journaliste.auteur(acteur), motif, journaliste.maintenant());
        dsm.demanderComplements(version);
        DeclarationStocksMouvements renvoyee = dsms.mettreAJour(dsm);
        commentaires.inserer(commentaire);
        journaliste.noter(acteur, renvoyee, ActionJournalDeclarations.COMPLEMENTS_DEMANDES, null);
        evenements.publier(new ComplementsDemandes(id, operateur, renvoyee.periode().debut(),
                renvoyee.periode().fin(), commentaire.texte()));
        return vue(acteur, renvoyee, vue);
    }

    /** RG-DEC-01, 20, 23, 29 : le BNIA valide une DSM en instruction, sans pièce manquante ; elle est figée. */
    public VueDsm valider(Acteur acteur, long operateur, long id, Long version) {
        OperateurVue vue = autorisations.exigerBnia(acteur, operateur);
        DeclarationStocksMouvements dsm = dsm(operateur, id);
        dsm.valider(journaliste.auteur(acteur), journaliste.maintenant(), version, manques(dsm));
        DeclarationStocksMouvements validee = dsms.mettreAJour(dsm);
        journaliste.noter(acteur, validee, ActionJournalDeclarations.DSM_VALIDEE, null);
        evenements.publier(new DsmValidee(id, operateur, validee.periode().debut(), validee.periode().fin()));
        return vue(acteur, validee, vue);
    }

    /**
     * RG-DEC-01, 22, 23 : seul un brouillon se supprime, par l'opérateur ou par le BNIA, avec ses commentaires,
     * ses pièces et leurs documents, et son journal ; une entrée « brouillon supprimé » reste au journal de
     * l'opérateur.
     */
    public void supprimer(Acteur acteur, long operateur, long id) {
        autorisations.exigerGestion(acteur, operateur);
        DeclarationStocksMouvements dsm = dsm(operateur, id);
        dsm.exigerSupprimable();
        journal.supprimerDeLaDsm(id);
        commentaires.supprimerDeLaDsm(id);
        List<Long> documents = Stream.concat(
                pieces.justificatifsDe(id).stream().map(j -> j.document().cle()),
                pieces.drmsDe(id).stream().map(d -> d.document().cle())).toList();
        dsms.supprimer(id);
        stockage.supprimer(documents);
        journaliste.noter(acteur, dsm, ActionJournalDeclarations.BROUILLON_SUPPRIME, null);
    }

    /** RG-DEC-01 : les DSM de l'opérateur, de la plus récente à la plus ancienne. */
    public List<DsmLigne> lister(Acteur acteur, long operateur) {
        autorisations.exigerLecture(acteur, operateur);
        return lecture.deLOperateur(operateur);
    }

    /** Le côté d'où l'acteur lit les DSM de l'opérateur, pour les libellés des états. */
    public Cote cote(Acteur acteur, long operateur) {
        autorisations.exigerLecture(acteur, operateur);
        return Autorisations.cote(autorisations.droits(acteur, operateur));
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

    private VueDsm vue(Acteur acteur, DeclarationStocksMouvements dsm, OperateurVue operateur) {
        return VueDsm.de(dsm, operateur, autorisations.droits(acteur, operateur.id()), manques(dsm));
    }

    /** RG-DEC-29 : pièces lues en deux requêtes, jamais une par ligne. */
    private List<Manque> manques(DeclarationStocksMouvements dsm) {
        if (dsm.id() == null) {
            return List.of();
        }
        return ControlePieces.manques(dsm, pieces.justificatifsDe(dsm.id()), pieces.drmsDe(dsm.id()));
    }

    private DeclarationStocksMouvements dsm(long operateur, long id) {
        Optional<DeclarationStocksMouvements> dsm = dsms.parId(id).filter(d -> d.operateur() == operateur);
        return dsm.orElseThrow(() -> new ElementIntrouvable("DSM introuvable : n° " + id + "."));
    }
}
