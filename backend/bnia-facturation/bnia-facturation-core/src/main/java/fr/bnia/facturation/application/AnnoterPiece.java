package fr.bnia.facturation.application;

import fr.bnia.commun.application.UseCase;
import fr.bnia.commun.domain.ElementIntrouvable;
import fr.bnia.commun.domain.RegleEnfreinte;
import fr.bnia.facturation.api.Acteur;
import fr.bnia.facturation.domain.CoteJournal;
import fr.bnia.facturation.domain.EntreeJournal;
import fr.bnia.facturation.domain.EvenementJournal;
import java.time.Clock;

/** Ajouter une note au journal d'une pièce. RG-FAC-20, 21, 26. */
@UseCase
public class AnnoterPiece {

    private final FactureRepository factures;
    private final JournalFacturation journal;
    private final Controles controles;
    private final Clock horloge;

    public AnnoterPiece(FactureRepository factures, JournalFacturation journal, DroitsFacturation droits,
            Clock horloge) {
        this.factures = factures;
        this.journal = journal;
        this.controles = new Controles(droits);
        this.horloge = horloge;
    }

    /**
     * RG-FAC-21 : une note interne, côté vendeur, sur un brouillon comme sur une pièce émise ; jamais imprimée ni
     * transmise.
     *
     * @return l'entrée ajoutée au journal
     */
    public EntreeJournal annoter(Acteur acteur, long id, String texte) {
        controles.exigerGestion(acteur);
        if (factures.parId(id).isEmpty()) {
            throw new ElementIntrouvable("Pièce introuvable : n° " + id + ".");
        }
        if (texte == null || texte.isBlank()) {
            throw new RegleEnfreinte("RG-FAC-21", "Une note a un texte.");
        }
        EntreeJournal note = EntreeJournal.par(controles.auteur(acteur), id, horloge.instant(), EvenementJournal.NOTE,
                CoteJournal.VENDEUR, texte.strip());
        journal.ajouter(note);
        return note;
    }
}
