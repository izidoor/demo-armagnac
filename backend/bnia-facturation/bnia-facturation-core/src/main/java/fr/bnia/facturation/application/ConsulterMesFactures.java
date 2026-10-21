package fr.bnia.facturation.application;

import fr.bnia.commun.application.StockageDocuments;
import fr.bnia.commun.application.StockageDocuments.DocumentLu;
import fr.bnia.commun.application.UseCase;
import fr.bnia.commun.domain.AccesRefuse;
import fr.bnia.commun.domain.ElementIntrouvable;
import fr.bnia.facturation.api.Acteur;
import fr.bnia.facturation.api.LigneVendue;
import fr.bnia.facturation.domain.CoteJournal;
import fr.bnia.facturation.domain.EntreeJournal;
import fr.bnia.facturation.domain.EtatPiece;
import fr.bnia.facturation.domain.EvenementJournal;
import fr.bnia.facturation.domain.Facture;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Les factures et avoirs d'un opérateur, au portail (UC-FAC-5). RG-FAC-26, 28 : les pièces émises dont il est le
 * client, jamais les brouillons ; chaque ouverture et chaque téléchargement est journalisé {@code CONSULTEE}, côté
 * client. Le journal reste au BNIA (RG-FAC-20). RG-FAC-34 : les lignes article de ses pièces émises d'une période.
 */
@UseCase
public class ConsulterMesFactures {

    private final FactureRepository factures;
    private final PiecesLecture lecture;
    private final VentesLecture ventes;
    private final JournalFacturation journal;
    private final StockageDocuments stockage;
    private final DroitsFacturation droits;
    private final Controles controles;
    private final Clock horloge;

    public ConsulterMesFactures(FactureRepository factures, PiecesLecture lecture, VentesLecture ventes,
            JournalFacturation journal, StockageDocuments stockage, DroitsFacturation droits, Clock horloge) {
        this.factures = factures;
        this.lecture = lecture;
        this.ventes = ventes;
        this.journal = journal;
        this.stockage = stockage;
        this.droits = droits;
        this.controles = new Controles(droits);
        this.horloge = horloge;
    }

    /** RG-FAC-28 : les pièces émises de l'opérateur, filtrées par numéro, sens, nature d'origine, période, date. */
    public List<PieceLigne> lister(Acteur acteur, long operateur, FiltrePieces filtre) {
        exigerLecture(acteur, operateur);
        return lecture.rechercher(new FiltrePieces(filtre.numero(), filtre.sens(), Optional.of(EtatPiece.EMISE),
                filtre.nature(), Optional.of(Long.toString(operateur)), Optional.empty(), filtre.periode(),
                filtre.du(), filtre.au()));
    }

    /**
     * RG-FAC-30, 34 : les lignes article des pièces émises de l'opérateur du {@code du} au {@code au} inclus (date
     * d'émission), filtrées par nature d'origine si elle est donnée ; celles d'un avoir en négatif.
     */
    public List<LigneVendue> lignes(Acteur acteur, long operateur, LocalDate du, LocalDate au,
            Optional<String> nature) {
        exigerLecture(acteur, operateur);
        ConsulterVentes.exigerPeriode(du, au);
        return ventes.ventes(du, au, nature, Optional.of(Long.toString(operateur)));
    }

    /** RG-FAC-28 : une pièce émise de l'opérateur ; l'ouverture est journalisée {@code CONSULTEE}. */
    public VuePiece consulter(Acteur acteur, long operateur, long id) {
        exigerLecture(acteur, operateur);
        Facture piece = piece(operateur, id);
        consultee(acteur, id, null);
        return new VuePiece(piece, List.of(), false);
    }

    /** RG-FAC-15, 28 : le PDF d'une pièce émise de l'opérateur ; le téléchargement est journalisé. */
    public DocumentLu pdf(Acteur acteur, long operateur, long id) {
        exigerLecture(acteur, operateur);
        DocumentLu document = stockage.lire(piece(operateur, id).pdf().orElseThrow().cle());
        consultee(acteur, id, "PDF");
        return document;
    }

    private void exigerLecture(Acteur acteur, long operateur) {
        if (!droits.litClient(acteur, operateur)) {
            throw new AccesRefuse("RG-FAC-26", "Vous n'avez pas accès aux factures de cet opérateur.");
        }
    }

    /**
     * RG-FAC-26 : une pièce émise dont l'opérateur est le client ; un brouillon, ou la pièce d'un autre, est
     * introuvable, sans dire qu'elle existe.
     */
    private Facture piece(long operateur, long id) {
        return factures.parId(id)
                .filter(p -> p.etat() == EtatPiece.EMISE)
                .filter(p -> p.acheteur().client() != null
                        && p.acheteur().client().identifiant().equals(Long.toString(operateur)))
                .orElseThrow(() -> new ElementIntrouvable("Facture introuvable : n° " + id + "."));
    }

    /**
     * RG-FAC-20, 28 : côté client au portail ; le personnel du BNIA qui passerait par là consulte côté vendeur.
     */
    private void consultee(Acteur acteur, long id, String detail) {
        CoteJournal cote = droits.litTout(acteur) ? CoteJournal.VENDEUR : CoteJournal.CLIENT;
        journal.ajouter(EntreeJournal.par(controles.auteur(acteur), id, horloge.instant(), EvenementJournal.CONSULTEE,
                cote, detail));
    }
}
