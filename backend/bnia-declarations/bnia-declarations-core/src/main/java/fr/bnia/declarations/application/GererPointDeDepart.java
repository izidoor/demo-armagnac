package fr.bnia.declarations.application;

import fr.bnia.commun.application.UseCase;
import fr.bnia.commun.domain.ConflitUnicite;
import fr.bnia.commun.domain.ElementIntrouvable;
import fr.bnia.declarations.domain.ActionJournalDeclarations;
import fr.bnia.declarations.domain.DeclarationStocksMouvements;
import fr.bnia.declarations.domain.IdentiteLigne;
import fr.bnia.declarations.domain.ImportExcel;
import fr.bnia.declarations.domain.ImportRefuse;
import fr.bnia.declarations.domain.LigneStockInitial;
import fr.bnia.declarations.domain.PointDeDepartDeclaratif;
import fr.bnia.declarations.domain.SaisieLigneStock;
import fr.bnia.declarations.domain.TypeDeclarant;
import fr.bnia.operateurs.application.partage.OperateursLecture;
import fr.bnia.operateurs.domain.partage.Acteur;
import fr.bnia.operateurs.domain.partage.OperateurVue;
import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * Point de départ déclaratif d'un opérateur (analyse UC-DEC-3, spec 05 §5.1) : fixé et modifié par le BNIA
 * depuis la fiche opérateur, saisi ou importé du modèle Excel, lu par l'opérateur (RG-DEC-01, 06 à 08, 23, 37, 38).
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

    /** Le point de départ en vigueur, et s'il est figé (RG-DEC-08). */
    public record PointDeDepart(PointDeDepartDeclaratif point, boolean fige) {
    }

    /** RG-DEC-01 : tout lecteur des déclarations de l'opérateur. */
    public Optional<PointDeDepart> consulter(Acteur acteur, long operateur) {
        autorisations.exigerLecture(acteur, operateur);
        return points.enVigueur(operateur).map(p -> new PointDeDepart(p, fige(p)));
    }

    /** RG-DEC-01, 06, 07, 23 : le BNIA fixe le premier point de départ d'un producteur ou d'un négociant. */
    public PointDeDepart fixer(Acteur acteur, long operateur, LocalDate debut, List<SaisieLigneStock> lignes) {
        OperateurVue vue = autorisations.exigerBnia(acteur, operateur);
        PointDeDepartDeclaratif point = fixer(acteur, vue, debut, lignes);
        journaliste.noter(acteur, operateur, ActionJournalDeclarations.POINT_DE_DEPART_FIXE, detail(point));
        return new PointDeDepart(point, false);
    }

    private PointDeDepartDeclaratif fixer(Acteur acteur, OperateurVue vue, LocalDate debut,
            List<SaisieLigneStock> lignes) {
        long operateur = vue.id();
        TypeDeclarant type = TypeDeclarant.de(vue.type());
        Optional<PointDeDepartDeclaratif> existant = points.enVigueur(operateur);
        if (existant.isPresent()) {
            throw new ConflitUnicite(null, "Le point de départ de " + vue.raisonSociale() + " est déjà fixé au "
                    + DATE.format(existant.get().debut()) + " : modifiez-le.");
        }
        return points.inserer(PointDeDepartDeclaratif.fixer(operateur, type, debut, lignes,
                journaliste.auteur(acteur), journaliste.maintenant()));
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
        return new PointDeDepart(modifie, false);
    }

    /** Un point de départ importé, et les avertissements de l'import (RG-DEC-38). */
    public record PointImporte(PointDeDepart point, List<String> avertissements) {
    }

    /**
     * RG-DEC-01, 08, 23, 37, 38 : le BNIA importe le modèle Excel en stock initial : le stock de fin de chaque
     * ligne du fichier, au lendemain de sa période ou à la date choisie. Fixe le point de départ s'il n'y en a pas,
     * le modifie sinon (tant qu'il n'est pas figé, la DSM en cours suit). Tout ou rien ({@link ImportRefuse}).
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
        PointDeDepartDeclaratif point = existant.isPresent()
                ? modifier(acteur, vue, existant.get(), stock.debut(), lignes, version)
                : fixer(acteur, vue, stock.debut(), lignes);
        journaliste.noter(acteur, operateur, ActionJournalDeclarations.POINT_DE_DEPART_IMPORTE,
                fichier.nom() + " : " + detail(point));
        return new PointImporte(new PointDeDepart(point, false), stock.avertissements());
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
