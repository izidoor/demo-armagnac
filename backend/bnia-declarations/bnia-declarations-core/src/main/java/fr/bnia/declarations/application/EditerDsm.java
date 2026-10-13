package fr.bnia.declarations.application;

import fr.bnia.commun.application.UseCase;
import fr.bnia.commun.domain.ElementIntrouvable;
import fr.bnia.declarations.application.EditionsDsm.EditionDsm;
import fr.bnia.declarations.domain.ActionJournalDeclarations;
import fr.bnia.declarations.domain.DeclarationStocksMouvements;
import fr.bnia.operateurs.application.partage.OperateursLecture;
import fr.bnia.operateurs.domain.partage.Acteur;
import fr.bnia.operateurs.domain.partage.OperateurVue;
import fr.bnia.referentiels.application.partage.ReferentielsLecture;
import java.time.Clock;
import java.time.format.DateTimeFormatter;
import java.util.function.Function;

/**
 * Éditer une DSM (spec 05 §5.1, H-DEC-21) : dans tous les états, par tout lecteur de la DSM, observateur
 * compris (§6.3) ; chaque édition est inscrite au journal (RG-DEC-23). RG-DEC-01, 35, 36.
 */
@UseCase
public class EditerDsm {

    /** Un document produit, avec le nom de fichier proposé. */
    public record DocumentEdite(String nom, byte[] contenu) {
    }

    private static final DateTimeFormatter MOIS = DateTimeFormatter.ofPattern("yyyy-MM");

    private final DsmRepository dsms;
    private final EditionsDsm editions;
    private final ClasseursDsm classeurs;
    private final Autorisations autorisations;
    private final Journaliste journaliste;
    private final Cotisations cotisations;

    public EditerDsm(DsmRepository dsms, EditionsDsm editions, ClasseursDsm classeurs, JournalDeclarations journal,
            GrilleCotisationRepository grilles, FacturesDsm factures, OperateursLecture operateurs,
            ReferentielsLecture referentiels, ParametresDeclarations parametres, Clock horloge) {
        this.dsms = dsms;
        this.editions = editions;
        this.classeurs = classeurs;
        this.autorisations = new Autorisations(operateurs);
        this.journaliste = new Journaliste(journal, operateurs, horloge);
        this.cotisations = new Cotisations(grilles, factures, referentiels, parametres, horloge);
    }

    /** RG-DEC-01, 23, 35 : le PDF de la DSM, « provisoire » tant qu'elle n'est pas validée. */
    public DocumentEdite pdf(Acteur acteur, long operateur, long id) {
        return editer(acteur, operateur, id, "PDF", "pdf", editions::pdf);
    }

    /**
     * RG-DEC-01, 23, 36 : le classeur Excel de la DSM, au format du modèle d'import ; celui d'un brouillon sert de
     * modèle pré-rempli.
     */
    public DocumentEdite excel(Acteur acteur, long operateur, long id) {
        return editer(acteur, operateur, id, "Excel", "xlsx", classeurs::ecrire);
    }

    private DocumentEdite editer(Acteur acteur, long operateur, long id, String format, String extension,
            Function<EditionDsm, byte[]> production) {
        OperateurVue vue = autorisations.exigerLecture(acteur, operateur);
        DeclarationStocksMouvements dsm = dsms.parId(id).filter(d -> d.operateur() == operateur)
                .orElseThrow(() -> new ElementIntrouvable("DSM introuvable : n° " + id + "."));
        byte[] contenu = production.apply(new EditionDsm(dsm, vue, cotisations.de(dsm),
                Autorisations.cote(autorisations.droits(acteur, operateur))));
        journaliste.noter(acteur, dsm, ActionJournalDeclarations.DSM_EDITEE, format);
        return new DocumentEdite("DSM-" + vue.code() + "-" + dsm.periode().debut().format(MOIS) + "." + extension,
                contenu);
    }
}
