package fr.bnia.declarations.application;

import fr.bnia.commun.application.StockageDocuments;
import fr.bnia.commun.application.StockageDocuments.DocumentLu;
import fr.bnia.commun.application.UseCase;
import fr.bnia.commun.domain.DocumentDepose;
import fr.bnia.commun.domain.ElementIntrouvable;
import fr.bnia.commun.domain.RegleEnfreinte;
import fr.bnia.declarations.domain.ActionJournalDeclarations;
import fr.bnia.declarations.domain.ControlePieces;
import fr.bnia.declarations.domain.Cote;
import fr.bnia.declarations.domain.DeclarationStocksMouvements;
import fr.bnia.declarations.domain.Drm;
import fr.bnia.declarations.domain.Justificatif;
import fr.bnia.declarations.domain.Manque;
import fr.bnia.declarations.domain.TypeMouvement;
import fr.bnia.declarations.domain.TypePiece;
import fr.bnia.operateurs.application.partage.OperateursLecture;
import fr.bnia.operateurs.domain.partage.Acteur;
import java.time.Clock;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Pièces d'une DSM (spec 05 §3.2.2, §3.2.3, §6.3, onglet « Pièces ») : justificatifs et, pour un producteur,
 * DRM ; dépôt, consultation, téléchargement, suppression. RG-DEC-01, 23, 26 à 29. Le fichier va au stockage
 * commun (ADR-018), dans la même transaction que sa référence.
 */
@UseCase
public class GererPieces {

    private static final DateTimeFormatter MOIS = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.FRENCH);

    private final DsmRepository dsms;
    private final Pieces pieces;
    private final StockageDocuments stockage;
    private final Autorisations autorisations;
    private final Journaliste journaliste;

    public GererPieces(DsmRepository dsms, Pieces pieces, StockageDocuments stockage, JournalDeclarations journal,
            OperateursLecture operateurs, Clock horloge) {
        this.dsms = dsms;
        this.pieces = pieces;
        this.stockage = stockage;
        this.autorisations = new Autorisations(operateurs);
        this.journaliste = new Journaliste(journal, operateurs, horloge);
    }

    /** Un mois de la période d'un producteur et sa DRM, si elle est déposée (RG-DEC-27). */
    public record MoisDrm(YearMonth mois, Optional<Drm> drm) {
    }

    /** Les pièces d'une DSM : justificatifs, mois de DRM attendus, manques (RG-DEC-29). */
    public record PiecesDsm(List<Justificatif> justificatifs, List<MoisDrm> mois, List<Manque> manques) {
    }

    /** RG-DEC-01, 27, 29 : les pièces de la DSM, lues en deux requêtes. */
    public PiecesDsm pieces(Acteur acteur, long operateur, long dsm) {
        autorisations.exigerLecture(acteur, operateur);
        DeclarationStocksMouvements declaration = dsm(operateur, dsm);
        List<Justificatif> justificatifs = pieces.justificatifsDe(dsm);
        List<Drm> drms = pieces.drmsDe(dsm);
        Map<YearMonth, Drm> parMois = drms.stream().collect(Collectors.toMap(Drm::mois, Function.identity()));
        List<MoisDrm> mois = declaration.moisAttendus().stream()
                .map(m -> new MoisDrm(m, Optional.ofNullable(parMois.get(m))))
                .toList();
        return new PiecesDsm(justificatifs, mois, ControlePieces.manques(declaration, justificatifs, drms));
    }

    /** RG-DEC-01, 23, 26, 28 : dépose un justificatif, sur la DSM ({@code ligne} absente) ou sur une de ses lignes. */
    public Justificatif deposerJustificatif(Acteur acteur, long operateur, long dsm, Long ligne,
            TypeMouvement typeMouvement, TypePiece typePiece, String reference, FichierDepose fichier) {
        DeclarationStocksMouvements declaration = modifiable(acteur, operateur, dsm);
        if (ligne != null && declaration.lignes().stream().noneMatch(l -> ligne.equals(l.id()))) {
            throw new ElementIntrouvable("La ligne n° " + ligne + " n'est pas une ligne de la DSM de "
                    + declaration.periode().libelle() + " : rechargez l'écran.");
        }
        typeMouvement.exigerPiece(typePiece);
        DocumentDepose document = stockage.deposerPdf(fichier.nom(), fichier.contenu());
        Justificatif justificatif = pieces.inserer(Justificatif.deposer(dsm, ligne, typeMouvement, typePiece,
                reference, document, journaliste.auteur(acteur), journaliste.maintenant()));
        journaliste.noter(acteur, declaration, ActionJournalDeclarations.JUSTIFICATIF_DEPOSE,
                typePiece.libelle() + " : " + document.nom());
        return justificatif;
    }

    /** RG-DEC-01, 23, 28 : supprime un justificatif et son document. */
    public void supprimerJustificatif(Acteur acteur, long operateur, long dsm, long id) {
        DeclarationStocksMouvements declaration = modifiable(acteur, operateur, dsm);
        Justificatif justificatif = justificatif(dsm, id);
        pieces.supprimerJustificatif(id);
        stockage.supprimer(justificatif.document().cle());
        journaliste.noter(acteur, declaration, ActionJournalDeclarations.JUSTIFICATIF_SUPPRIME,
                justificatif.typePiece().libelle() + " : " + justificatif.document().nom());
    }

    /**
     * RG-DEC-01, 23, 27, 28 : dépose la DRM d'un mois de la période ; un second dépôt pour le même mois est
     * refusé tant que le premier n'est pas supprimé.
     */
    public Drm deposerDrm(Acteur acteur, long operateur, long dsm, YearMonth mois, FichierDepose fichier) {
        DeclarationStocksMouvements declaration = modifiable(acteur, operateur, dsm);
        declaration.exigerMoisDrm(mois);
        if (pieces.drm(dsm, mois).isPresent()) {
            throw new RegleEnfreinte("RG-DEC-27", "La DRM de " + mois.format(MOIS) + " est déjà déposée :"
                    + " supprimez-la avant d'en déposer une autre.");
        }
        DocumentDepose document = stockage.deposerPdf(fichier.nom(), fichier.contenu());
        Drm drm = pieces.inserer(new Drm(null, dsm, mois, document, journaliste.auteur(acteur),
                journaliste.maintenant()));
        journaliste.noter(acteur, declaration, ActionJournalDeclarations.DRM_DEPOSEE, mois.format(MOIS));
        return drm;
    }

    /** RG-DEC-01, 23, 28 : supprime la DRM d'un mois et son document. */
    public void supprimerDrm(Acteur acteur, long operateur, long dsm, YearMonth mois) {
        DeclarationStocksMouvements declaration = modifiable(acteur, operateur, dsm);
        Drm drm = drm(dsm, mois);
        pieces.supprimerDrm(drm.id());
        stockage.supprimer(drm.document().cle());
        journaliste.noter(acteur, declaration, ActionJournalDeclarations.DRM_SUPPRIMEE, mois.format(MOIS));
    }

    /** RG-DEC-01 : tout lecteur de la DSM télécharge ses pièces. */
    public DocumentLu telechargerJustificatif(Acteur acteur, long operateur, long dsm, long id) {
        autorisations.exigerLecture(acteur, operateur);
        dsm(operateur, dsm);
        return stockage.lire(justificatif(dsm, id).document().cle());
    }

    /** RG-DEC-01 : tout lecteur de la DSM télécharge ses DRM. */
    public DocumentLu telechargerDrm(Acteur acteur, long operateur, long dsm, YearMonth mois) {
        autorisations.exigerLecture(acteur, operateur);
        dsm(operateur, dsm);
        return stockage.lire(drm(dsm, mois).document().cle());
    }

    /** RG-DEC-01, 28 : droits, puis état permis selon le côté de l'acteur. */
    private DeclarationStocksMouvements modifiable(Acteur acteur, long operateur, long dsm) {
        Cote cote = autorisations.exigerPieces(acteur, operateur);
        DeclarationStocksMouvements declaration = dsm(operateur, dsm);
        declaration.exigerPiecesModifiables(cote);
        return declaration;
    }

    private Justificatif justificatif(long dsm, long id) {
        return pieces.justificatif(id).filter(j -> j.dsm() == dsm)
                .orElseThrow(() -> new ElementIntrouvable("Justificatif introuvable : n° " + id + "."));
    }

    private Drm drm(long dsm, YearMonth mois) {
        return pieces.drm(dsm, mois)
                .orElseThrow(() -> new ElementIntrouvable("Aucune DRM déposée pour " + mois.format(MOIS) + "."));
    }

    private DeclarationStocksMouvements dsm(long operateur, long id) {
        return dsms.parId(id).filter(d -> d.operateur() == operateur)
                .orElseThrow(() -> new ElementIntrouvable("DSM introuvable : n° " + id + "."));
    }
}
