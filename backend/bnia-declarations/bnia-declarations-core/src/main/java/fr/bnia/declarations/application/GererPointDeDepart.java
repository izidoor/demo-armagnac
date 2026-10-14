package fr.bnia.declarations.application;

import fr.bnia.commun.application.UseCase;
import fr.bnia.commun.domain.ConflitUnicite;
import fr.bnia.commun.domain.ElementIntrouvable;
import fr.bnia.declarations.domain.ActionJournalDeclarations;
import fr.bnia.declarations.domain.CalendrierDeclaratif.DsmPassee;
import fr.bnia.declarations.domain.DeclarationStocksMouvements;
import fr.bnia.declarations.domain.IdentiteLigne;
import fr.bnia.declarations.domain.ImportExcel;
import fr.bnia.declarations.domain.ImportRefuse;
import fr.bnia.declarations.domain.LigneStockInitial;
import fr.bnia.declarations.domain.PeriodeDeclaration;
import fr.bnia.declarations.domain.PointDeDepartDeclaratif;
import fr.bnia.declarations.domain.SaisieLigneStock;
import fr.bnia.declarations.domain.TypeDeclarant;
import fr.bnia.declarations.domain.Vieillissement;
import fr.bnia.declarations.domain.partage.EtatDsm;
import fr.bnia.operateurs.application.partage.OperateursLecture;
import fr.bnia.operateurs.domain.partage.Acteur;
import fr.bnia.operateurs.domain.partage.OperateurVue;
import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Point de départ déclaratif d'un opérateur (analyse UC-DEC-3, spec 05 §5.1) : fixé et modifié par le BNIA
 * depuis la fiche opérateur, saisi ou importé du modèle Excel, lu par l'opérateur (RG-DEC-01, 06 à 08, 23, 37, 38) ;
 * un nouveau point de départ après une interruption (RG-DEC-09).
 */
@UseCase
public class GererPointDeDepart {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final PointDeDepartRepository points;
    private final DsmRepository dsms;
    private final ClasseursDsm classeurs;
    private final Autorisations autorisations;
    private final Journaliste journaliste;

    public GererPointDeDepart(PointDeDepartRepository points, DsmRepository dsms, ClasseursDsm classeurs,
            JournalDeclarations journal, OperateursLecture operateurs, Clock horloge) {
        this.points = points;
        this.dsms = dsms;
        this.classeurs = classeurs;
        this.autorisations = new Autorisations(operateurs);
        this.journaliste = new Journaliste(journal, operateurs, horloge);
    }

    /**
     * Un point de départ, s'il est figé (RG-DEC-08) et si un nouveau peut le remplacer (RG-DEC-09).
     *
     * @param renouvelable le point en vigueur est figé et l'opérateur n'a aucune DSM en cours ; faux pour un
     *                     point précédent
     */
    public record PointDeDepart(PointDeDepartDeclaratif point, boolean fige, boolean renouvelable) {
    }

    /** RG-DEC-01 : tout lecteur des déclarations de l'opérateur. */
    public Optional<PointDeDepart> consulter(Acteur acteur, long operateur) {
        autorisations.exigerLecture(acteur, operateur);
        return points.enVigueur(operateur).map(p -> {
            boolean fige = fige(p);
            return new PointDeDepart(p, fige, fige && dsms.nonValideeDe(operateur).isEmpty());
        });
    }

    /** RG-DEC-01, 09 : tous les points de départ de l'opérateur, du plus récent au plus ancien. */
    public List<PointDeDepart> historique(Acteur acteur, long operateur) {
        autorisations.exigerLecture(acteur, operateur);
        return points.deLOperateur(operateur).stream().map(p -> new PointDeDepart(p, fige(p), false)).toList();
    }

    /**
     * Le stock proposé pour un nouveau point de départ (RG-DEC-09) : le stock de fin de la dernière DSM validée,
     * vieilli à la date de début, modifiable avant d'être fixé.
     *
     * @param debut   la date de début retenue
     * @param derniere la période de la dernière DSM validée
     */
    public record Proposition(LocalDate debut, PeriodeDeclaration derniere, List<LigneStockInitial> lignes) {
    }

    /**
     * RG-DEC-01, 09, 14 : le BNIA prépare un nouveau point de départ. Sans date, le lendemain de la fin de la
     * dernière DSM validée ; le vieillissement compte chaque 1er avril franchi depuis.
     *
     * @return vide si l'opérateur n'a aucune DSM validée : son point de départ se modifie
     */
    public Optional<Proposition> proposer(Acteur acteur, long operateur, Optional<LocalDate> debut) {
        autorisations.exigerBnia(acteur, operateur);
        return derniereValidee(operateur).flatMap(d -> dsms.parId(d.id())).map(derniere -> {
            LocalDate date = debut.orElse(derniere.periode().fin().plusDays(1));
            return new Proposition(date, derniere.periode(), Vieillissement.apresInterruption(derniere, date));
        });
    }

    /**
     * RG-DEC-01, 06, 07, 09, 23 : le BNIA fixe le premier point de départ d'un producteur ou d'un négociant, ou un
     * nouveau quand le précédent est figé, que l'opérateur n'a aucune DSM en cours, à une date postérieure à sa
     * dernière DSM validée ; le journal trace le trou éventuel.
     */
    public PointDeDepart fixer(Acteur acteur, long operateur, LocalDate debut, List<SaisieLigneStock> lignes) {
        OperateurVue vue = autorisations.exigerBnia(acteur, operateur);
        Fixe fixe = fixer(acteur, vue, debut, lignes);
        journaliste.noter(acteur, operateur, ActionJournalDeclarations.POINT_DE_DEPART_FIXE, fixe.detail());
        return new PointDeDepart(fixe.point(), false, false);
    }

    /** Un point de départ fixé, et le détail de son entrée au journal. */
    private record Fixe(PointDeDepartDeclaratif point, String detail) {
    }

    private Fixe fixer(Acteur acteur, OperateurVue vue, LocalDate debut, List<SaisieLigneStock> lignes) {
        long operateur = vue.id();
        TypeDeclarant type = TypeDeclarant.de(vue.type());
        Optional<PointDeDepartDeclaratif> existant = points.enVigueur(operateur);
        if (existant.isEmpty()) {
            PointDeDepartDeclaratif point = points.inserer(PointDeDepartDeclaratif.fixer(operateur, type, debut,
                    lignes, journaliste.auteur(acteur), journaliste.maintenant()));
            return new Fixe(point, detail(point));
        }
        if (!fige(existant.get())) {
            throw new ConflitUnicite(null, "Le point de départ de " + vue.raisonSociale() + " est déjà fixé au "
                    + DATE.format(existant.get().debut()) + " : modifiez-le.");
        }
        // Figé : une DSM validée commence à son début, la dernière existe donc.
        PeriodeDeclaration derniere = derniereValidee(operateur).orElseThrow().periode();
        PointDeDepartDeclaratif point = points.inserer(PointDeDepartDeclaratif.fixerApres(operateur, type, debut,
                lignes, dsms.nonValideeDe(operateur).map(DeclarationStocksMouvements::periode), derniere,
                journaliste.auteur(acteur), journaliste.maintenant()));
        LocalDate lendemain = derniere.fin().plusDays(1);
        String interruption = point.debut().isAfter(lendemain)
                ? ", après une interruption du " + DATE.format(lendemain) + " au "
                        + DATE.format(point.debut().minusDays(1))
                : ", à la suite de la DSM de " + derniere.libelle();
        return new Fixe(point, "Nouveau point de départ : " + detail(point) + interruption);
    }

    /** RG-DEC-09 : la DSM validée la plus récente de l'opérateur. */
    private Optional<DsmPassee> derniereValidee(long operateur) {
        return dsms.dsmsDe(operateur).stream()
                .filter(d -> d.etat() == EtatDsm.VALIDEE)
                .max(Comparator.comparing(d -> d.periode().fin()));
    }

    /**
     * RG-DEC-01, 07, 08, 23 : modifiable tant qu'il n'est pas figé ; la DSM en cours suit son stock initial ;
     * sa date de début ne change pas tant qu'une DSM est en cours.
     */
    public PointDeDepart modifier(Acteur acteur, long operateur, long id, LocalDate debut,
            List<SaisieLigneStock> lignes, Long version) {
        OperateurVue vue = autorisations.exigerBnia(acteur, operateur);
        PointDeDepartDeclaratif point = points.parId(id).filter(p -> p.operateur() == operateur)
                .orElseThrow(() -> new ElementIntrouvable("Point de départ introuvable : n° " + id + "."));
        PointDeDepartDeclaratif modifie = modifier(acteur, vue, point, debut, lignes, version);
        journaliste.noter(acteur, operateur, ActionJournalDeclarations.POINT_DE_DEPART_MODIFIE, detail(modifie));
        return new PointDeDepart(modifie, false, false);
    }

    /** Un point de départ importé, et les avertissements de l'import (RG-DEC-38). */
    public record PointImporte(PointDeDepart point, List<String> avertissements) {
    }

    /**
     * RG-DEC-01, 08, 09, 23, 37, 38 : le BNIA importe le modèle Excel en stock initial : le stock de fin de chaque
     * ligne du fichier, au lendemain de sa période ou à la date choisie. Fixe le point de départ s'il n'y en a pas,
     * le modifie s'il n'est pas figé (la DSM en cours suit), en fixe un nouveau s'il l'est (RG-DEC-09). Tout ou
     * rien ({@link ImportRefuse}).
     *
     * @param debut   la date choisie ; vide : le lendemain de la période du fichier
     * @param version la version du point de départ en vigueur, s'il y en a un ; {@code null} : pas de contrôle
     */
    public PointImporte importer(Acteur acteur, long operateur, FichierDepose fichier, Optional<LocalDate> debut,
            Long version) {
        OperateurVue vue = autorisations.exigerBnia(acteur, operateur);
        ImportExcel.StockImporte stock = ImportExcel.stockInitial(classeurs.lire(fichier.contenu()),
                TypeDeclarant.de(vue.type()), debut);
        List<SaisieLigneStock> lignes = stock.lignes().stream().map(GererPointDeDepart::saisie).toList();
        Optional<PointDeDepartDeclaratif> existant = points.enVigueur(operateur);
        Fixe fixe = existant.isPresent() && !fige(existant.get())
                ? new Fixe(modifier(acteur, vue, existant.get(), stock.debut(), lignes, version), null)
                : fixer(acteur, vue, stock.debut(), lignes);
        PointDeDepartDeclaratif point = fixe.point();
        journaliste.noter(acteur, operateur, ActionJournalDeclarations.POINT_DE_DEPART_IMPORTE,
                fichier.nom() + " : " + (fixe.detail() == null ? detail(point) : fixe.detail()));
        return new PointImporte(new PointDeDepart(point, false, false), stock.avertissements());
    }

    private static SaisieLigneStock saisie(LigneStockInitial ligne) {
        IdentiteLigne identite = ligne.identite();
        return new SaisieLigneStock(identite.cle(), identite.millesime() == null ? null : identite.millesime().annee(),
                identite.denomination() == null ? null : identite.denomination().texte(), identite.compte().toString(),
                ligne.volume());
    }

    private PointDeDepartDeclaratif modifier(Acteur acteur, OperateurVue vue, PointDeDepartDeclaratif point,
            LocalDate debut, List<SaisieLigneStock> lignes, Long version) {
        long operateur = vue.id();
        TypeDeclarant type = TypeDeclarant.de(vue.type());
        Optional<DeclarationStocksMouvements> enCours = dsms.nonValideeDe(operateur)
                .filter(d -> !d.periode().debut().isBefore(point.debut()));
        point.modifier(type, debut, lignes, fige(point), enCours.map(DeclarationStocksMouvements::periode),
                journaliste.auteur(acteur), journaliste.maintenant(), version);
        PointDeDepartDeclaratif modifie = points.mettreAJour(point);
        enCours.filter(d -> d.periode().debut().equals(modifie.debut())).ifPresent(dsm -> {
            dsm.suivrePointDeDepart(modifie.lignes());
            dsms.mettreAJour(dsm);
        });
        return modifie;
    }

    private boolean fige(PointDeDepartDeclaratif point) {
        return dsms.existeValideeCommencantLe(point.operateur(), point.debut());
    }

    private static String detail(PointDeDepartDeclaratif point) {
        int n = point.lignes().size();
        return "Début au " + DATE.format(point.debut()) + ", " + n + (n > 1 ? " lignes" : " ligne") + " de stock initial";
    }
}
