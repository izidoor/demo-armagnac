package fr.bnia.facturation.application;

import fr.bnia.commun.application.StockageDocuments;
import fr.bnia.commun.application.StockageDocuments.DocumentLu;
import fr.bnia.commun.application.UseCase;
import fr.bnia.commun.domain.ElementIntrouvable;
import fr.bnia.facturation.api.Acteur;
import fr.bnia.facturation.domain.CoteJournal;
import fr.bnia.facturation.domain.EntreeJournal;
import fr.bnia.facturation.domain.EtatPiece;
import fr.bnia.facturation.domain.EvenementJournal;
import fr.bnia.facturation.domain.Facture;
import java.time.Clock;
import java.util.List;

/**
 * Historique, détail, PDF (et aperçu d'un brouillon) et journal des pièces au back-office (UC-FAC-4). RG-FAC-09
 * (ce qui empêche l'émission), 15, 20 (consultation journalisée côté vendeur), 26, 27.
 */
@UseCase
public class ConsulterFactures {

    private final FactureRepository factures;
    private final PiecesLecture lecture;
    private final VendeurRepository vendeurs;
    private final JournalFacturation journal;
    private final StockageDocuments stockage;
    private final ApercuPiece apercu;
    private final DroitsFacturation droits;
    private final Controles controles;
    private final Clock horloge;

    public ConsulterFactures(FactureRepository factures, PiecesLecture lecture, VendeurRepository vendeurs,
            JournalFacturation journal, StockageDocuments stockage, ApercuPiece apercu, DroitsFacturation droits,
            Clock horloge) {
        this.factures = factures;
        this.lecture = lecture;
        this.vendeurs = vendeurs;
        this.journal = journal;
        this.stockage = stockage;
        this.apercu = apercu;
        this.droits = droits;
        this.controles = new Controles(droits);
        this.horloge = horloge;
    }

    /** RG-FAC-26, 27 : l'historique filtré, brouillons compris. */
    public List<PieceLigne> lister(Acteur acteur, FiltrePieces filtre) {
        controles.exigerLectureBnia(acteur);
        return lecture.rechercher(filtre);
    }

    /**
     * RG-FAC-09, 20, 26 : la pièce, et pour un brouillon ce qui l'empêche d'être émis (acheteur tel qu'enregistré
     * à la demande). L'ouverture d'une pièce émise est journalisée {@code CONSULTEE}, côté vendeur.
     */
    public VuePiece consulter(Acteur acteur, long id) {
        controles.exigerLectureBnia(acteur);
        Facture piece = piece(id);
        if (piece.etat() == EtatPiece.BROUILLON) {
            return new VuePiece(piece, piece.empechements(vendeurs.charger()), droits.gere(acteur));
        }
        consultee(acteur, id, null);
        return new VuePiece(piece, List.of(), droits.gere(acteur));
    }

    /** RG-FAC-15, 20, 26 : le PDF d'une pièce émise, téléchargement journalisé {@code CONSULTEE}, côté vendeur. */
    public DocumentLu pdf(Acteur acteur, long id) {
        controles.exigerLectureBnia(acteur);
        Facture piece = piece(id);
        DocumentLu document = stockage.lire(piece.pdf()
                .orElseThrow(() -> new ElementIntrouvable("Un brouillon n'a pas de PDF : il s'édite à l'émission.")).cle());
        consultee(acteur, id, "PDF");
        return document;
    }

    /**
     * RG-FAC-15, 26 : l'aperçu PDF d'un brouillon, marqué « projet », avec le vendeur paramétré à cet instant ;
     * calculé à chaque demande, ni conservé ni journalisé. Une pièce émise a son PDF (RG-FAC-14).
     */
    public byte[] apercu(Acteur acteur, long id) {
        controles.exigerLectureBnia(acteur);
        Facture piece = piece(id);
        piece.exigerBrouillon();
        return apercu.apercu(piece, vendeurs.charger());
    }

    /** RG-FAC-20, 26 : le journal de la pièce, lu par le BNIA seulement. */
    public List<EntreeJournal> journal(Acteur acteur, long id) {
        controles.exigerLectureBnia(acteur);
        piece(id);
        return journal.deLaPiece(id);
    }

    private void consultee(Acteur acteur, long id, String detail) {
        journal.ajouter(EntreeJournal.par(controles.auteur(acteur), id, horloge.instant(), EvenementJournal.CONSULTEE,
                CoteJournal.VENDEUR, detail));
    }

    private Facture piece(long id) {
        return factures.parId(id).orElseThrow(() -> new ElementIntrouvable("Pièce introuvable : n° " + id + "."));
    }
}
