package fr.bnia.facturation.application;

import fr.bnia.commun.application.StockageDocuments;
import fr.bnia.commun.application.Transactions;
import fr.bnia.commun.application.UseCase;
import fr.bnia.commun.domain.ElementIntrouvable;
import fr.bnia.commun.domain.RegleEnfreinte;
import fr.bnia.facturation.api.Acteur;
import fr.bnia.facturation.domain.CoteJournal;
import fr.bnia.facturation.domain.EntreeJournal;
import fr.bnia.facturation.domain.EvenementJournal;
import fr.bnia.facturation.domain.Facture;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

/** Transmettre une sélection de pièces émises à Zeendoc (UC-FAC-7, simulé pour le prototype). RG-FAC-26, 32. */
@UseCase
public class TransmettreAZeendoc {

    /**
     * Le compte rendu d'une pièce de la sélection.
     *
     * @param numero vide si la pièce est introuvable
     * @param motif  vide si elle a été transmise
     */
    public record CompteRendu(long id, String numero, String motif) {

        public boolean transmise() {
            return motif == null;
        }
    }

    private final FactureRepository factures;
    private final JournalFacturation journal;
    private final StockageDocuments stockage;
    private final Zeendoc zeendoc;
    private final Transactions transactions;
    private final Controles controles;
    private final Clock horloge;

    public TransmettreAZeendoc(FactureRepository factures, JournalFacturation journal, StockageDocuments stockage,
            Zeendoc zeendoc, Transactions transactions, DroitsFacturation droits, Clock horloge) {
        this.factures = factures;
        this.journal = journal;
        this.stockage = stockage;
        this.zeendoc = zeendoc;
        this.transactions = transactions;
        this.controles = new Controles(droits);
        this.horloge = horloge;
    }

    /**
     * RG-FAC-32 : chaque pièce est transmise dans sa propre transaction, dans l'ordre de la sélection ; un refus
     * (pièce non émise, déjà transmise) n'arrête pas les autres. Une pièce sélectionnée deux fois ne compte qu'une.
     */
    public List<CompteRendu> transmettre(Acteur acteur, List<Long> ids) {
        controles.exigerGestion(acteur);
        List<CompteRendu> comptesRendus = new ArrayList<>();
        for (long id : new LinkedHashSet<>(ids)) {
            try {
                String numero = transactions.dansUneNouvelle(() -> transmettreUne(acteur, id));
                comptesRendus.add(new CompteRendu(id, numero, null));
            } catch (RegleEnfreinte | ElementIntrouvable refus) {
                comptesRendus.add(new CompteRendu(id, factures.parId(id).flatMap(Facture::numero).orElse(null),
                        refus.getMessage()));
            }
        }
        return comptesRendus;
    }

    private String transmettreUne(Acteur acteur, long id) {
        Facture facture = factures.parId(id)
                .orElseThrow(() -> new ElementIntrouvable("Pièce introuvable : n° " + id + "."));
        Instant maintenant = horloge.instant();
        facture.transmettre(maintenant);
        String numero = facture.numero().orElseThrow();
        zeendoc.deposer(numero, stockage.lire(facture.pdf().orElseThrow().cle()).contenu());
        factures.mettreAJour(facture);
        journal.ajouter(EntreeJournal.par(controles.auteur(acteur), id, maintenant, EvenementJournal.TRANSMISE,
                CoteJournal.VENDEUR, null));
        return numero;
    }
}
