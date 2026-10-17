package fr.bnia.facturation.application;

import fr.bnia.commun.application.UseCase;
import fr.bnia.commun.domain.ElementIntrouvable;
import fr.bnia.facturation.domain.CoteJournal;
import fr.bnia.facturation.domain.EntreeJournal;
import fr.bnia.facturation.domain.EvenementJournal;
import fr.bnia.facturation.domain.Facture;
import java.time.Clock;
import java.time.LocalDate;

/**
 * Enregistrer le règlement d'une pièce relevé dans Zeendoc (UC-FAC-7, RG-FAC-33). Simulé pour le prototype : sans
 * relevé planifié, seule la démonstration l'appelle. Sans acteur, comme un traitement : le journal le dit de
 * « Zeendoc ».
 */
@UseCase
public class EnregistrerReglement {

    static final String ZEENDOC = "Zeendoc";

    private final FactureRepository factures;
    private final JournalFacturation journal;
    private final Clock horloge;

    public EnregistrerReglement(FactureRepository factures, JournalFacturation journal, Clock horloge) {
        this.factures = factures;
        this.journal = journal;
        this.horloge = horloge;
    }

    /** RG-FAC-33 : la pièce transmise, et non encore payée, est payée à cette date. */
    public void enregistrer(long id, LocalDate payeeLe) {
        Facture facture = factures.parId(id)
                .orElseThrow(() -> new ElementIntrouvable("Pièce introuvable : n° " + id + "."));
        facture.enregistrerReglement(payeeLe);
        factures.mettreAJour(facture);
        journal.ajouter(new EntreeJournal(id, horloge.instant(), EvenementJournal.PAYEE, null, ZEENDOC,
                CoteJournal.VENDEUR, null));
    }
}
