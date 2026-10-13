package fr.bnia.declarations.application;

import fr.bnia.commun.application.UseCase;
import fr.bnia.commun.domain.ConflitUnicite;
import fr.bnia.commun.domain.ElementIntrouvable;
import fr.bnia.declarations.domain.ActionJournalDeclarations;
import fr.bnia.declarations.domain.DeclarationStocksMouvements;
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
 * depuis la fiche opérateur, lu par l'opérateur (RG-DEC-01, 06 à 08, 23).
 */
@UseCase
public class GererPointDeDepart {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final PointDeDepartRepository points;
    private final DsmRepository dsms;
    private final Autorisations autorisations;
    private final Journaliste journaliste;

    public GererPointDeDepart(PointDeDepartRepository points, DsmRepository dsms, JournalDeclarations journal,
            OperateursLecture operateurs, Clock horloge) {
        this.points = points;
        this.dsms = dsms;
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
        TypeDeclarant type = TypeDeclarant.de(vue.type());
        Optional<PointDeDepartDeclaratif> existant = points.enVigueur(operateur);
        if (existant.isPresent()) {
            throw new ConflitUnicite(null, "Le point de départ de " + vue.raisonSociale() + " est déjà fixé au "
                    + DATE.format(existant.get().debut()) + " : modifiez-le.");
        }
        PointDeDepartDeclaratif point = points.inserer(PointDeDepartDeclaratif.fixer(operateur, type, debut, lignes,
                journaliste.auteur(acteur), journaliste.maintenant()));
        journaliste.noter(acteur, operateur, ActionJournalDeclarations.POINT_DE_DEPART_FIXE, detail(point));
        return new PointDeDepart(point, false);
    }

    /**
     * RG-DEC-01, 07, 08, 23 : modifiable tant qu'il n'est pas figé ; la DSM en cours suit son stock initial ;
     * sa date de début ne change pas tant qu'une DSM est en cours.
     */
    public PointDeDepart modifier(Acteur acteur, long operateur, long id, LocalDate debut,
            List<SaisieLigneStock> lignes, Long version) {
        OperateurVue vue = autorisations.exigerBnia(acteur, operateur);
        TypeDeclarant type = TypeDeclarant.de(vue.type());
        PointDeDepartDeclaratif point = points.parId(id).filter(p -> p.operateur() == operateur)
                .orElseThrow(() -> new ElementIntrouvable("Point de départ introuvable : n° " + id + "."));
        Optional<DeclarationStocksMouvements> enCours = dsms.nonValideeDe(operateur)
                .filter(d -> !d.periode().debut().isBefore(point.debut()));
        point.modifier(type, debut, lignes, fige(point), enCours.map(DeclarationStocksMouvements::periode),
                journaliste.auteur(acteur), journaliste.maintenant(), version);
        PointDeDepartDeclaratif modifie = points.mettreAJour(point);
        enCours.filter(d -> d.periode().debut().equals(modifie.debut())).ifPresent(dsm -> {
            dsm.suivrePointDeDepart(modifie.lignes());
            dsms.mettreAJour(dsm);
        });
        journaliste.noter(acteur, operateur, ActionJournalDeclarations.POINT_DE_DEPART_MODIFIE, detail(modifie));
        return new PointDeDepart(modifie, false);
    }

    private boolean fige(PointDeDepartDeclaratif point) {
        return dsms.existeValideeCommencantLe(point.operateur(), point.debut());
    }

    private static String detail(PointDeDepartDeclaratif point) {
        int n = point.lignes().size();
        return "Début au " + DATE.format(point.debut()) + ", " + n + (n > 1 ? " lignes" : " ligne") + " de stock initial";
    }
}
