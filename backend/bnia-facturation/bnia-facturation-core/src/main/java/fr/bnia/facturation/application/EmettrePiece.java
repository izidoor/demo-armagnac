package fr.bnia.facturation.application;

import fr.bnia.commun.application.StockageDocuments;
import fr.bnia.commun.application.UseCase;
import fr.bnia.commun.domain.DocumentDepose;
import fr.bnia.commun.domain.ElementIntrouvable;
import fr.bnia.commun.domain.RegleEnfreinte;
import fr.bnia.facturation.api.Acteur;
import fr.bnia.facturation.api.FactureEmise;
import fr.bnia.facturation.domain.Auteur;
import fr.bnia.facturation.domain.CoteJournal;
import fr.bnia.facturation.domain.EntreeJournal;
import fr.bnia.facturation.domain.EvenementJournal;
import fr.bnia.facturation.domain.Facture;
import fr.bnia.facturation.domain.Numerotation;
import fr.bnia.facturation.domain.SensPiece;
import fr.bnia.facturation.domain.Totaux;
import fr.bnia.facturation.domain.Vendeur;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;

/** Émettre un brouillon (UC-FAC-2). RG-FAC-05, 09 à 15, 25, 26. */
@UseCase
public class EmettrePiece {

    private final FactureRepository factures;
    private final VendeurRepository vendeurs;
    private final NumerotationRepository numerotations;
    private final JournalFacturation journal;
    private final Clients clients;
    private final EditionPiece edition;
    private final StockageDocuments stockage;
    private final PublicationEvenements evenements;
    private final Controles controles;
    private final Clock horloge;

    public EmettrePiece(FactureRepository factures, VendeurRepository vendeurs, NumerotationRepository numerotations,
            JournalFacturation journal, Clients clients, EditionPiece edition, StockageDocuments stockage,
            PublicationEvenements evenements, DroitsFacturation droits, Clock horloge) {
        this.factures = factures;
        this.vendeurs = vendeurs;
        this.numerotations = numerotations;
        this.journal = journal;
        this.clients = clients;
        this.edition = edition;
        this.stockage = stockage;
        this.evenements = evenements;
        this.controles = new Controles(droits);
        this.horloge = horloge;
    }

    /**
     * RG-FAC-09 : vérifie le brouillon (acheteur relu, RG-FAC-11), puis attribue le numéro (RG-FAC-12), la date
     * et l'échéance (RG-FAC-13), recopie le vendeur, produit le PDF (RG-FAC-15), journalise {@code EMISE} et publie
     * {@link FactureEmise} dans la même transaction (RG-FAC-25). Un refus ne consomme pas de numéro.
     *
     * @return la pièce émise
     */
    public Facture emettre(Acteur acteur, long id) {
        controles.exigerGestion(acteur);
        Auteur auteur = controles.auteur(acteur);
        Facture piece = factures.parId(id).orElseThrow(() -> new ElementIntrouvable("Pièce introuvable : n° " + id + "."));
        piece.exigerBrouillon();
        if (piece.acheteur().client() != null) {
            piece.relireAcheteur(clients.acheteur(piece.acheteur().client()).orElseThrow(() -> new RegleEnfreinte(
                    "RG-FAC-11", "Client introuvable : " + piece.acheteur().client().code() + ".")));
        }
        Optional<Vendeur> vendeur = vendeurs.charger();
        piece.exigerEmettable(vendeur);
        LocalDate aujourdhui = LocalDate.now(horloge);
        Numerotation numerotation = numerotations.verrouiller(aujourdhui.getYear()).suivante();
        piece.emettre(numerotation, aujourdhui, vendeur.orElseThrow());
        numerotations.enregistrer(numerotation);
        DocumentDepose pdf = stockage.deposerPdf(nomDuPdf(piece), edition.pdf(piece));
        piece.joindrePdf(pdf);
        Facture emise = factures.mettreAJour(piece);
        Instant maintenant = horloge.instant();
        journal.ajouter(EntreeJournal.par(auteur, id, maintenant, EvenementJournal.EMISE, CoteJournal.VENDEUR,
                emise.numero().orElseThrow()));
        if (emise.sens() == SensPiece.FACTURE) {
            Totaux totaux = emise.totaux();
            evenements.publier(new FactureEmise(id, emise.origine(), emise.acheteur().client(),
                    emise.numero().orElseThrow(), aujourdhui, totaux.ht(), totaux.tva(), totaux.ttc(), acteur));
        }
        return emise;
    }

    /** « FACTURE-2026000001.pdf ». */
    private static String nomDuPdf(Facture piece) {
        return piece.sens().libelle().toUpperCase(java.util.Locale.ROOT) + "-" + piece.numero().orElseThrow() + ".pdf";
    }
}
