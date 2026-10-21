package fr.bnia.facturation.application;

import fr.bnia.commun.application.UseCase;
import fr.bnia.commun.domain.ElementIntrouvable;
import fr.bnia.facturation.api.Acteur;
import fr.bnia.facturation.domain.NoteInterne;
import java.time.Clock;
import java.util.List;

/**
 * Carnet de notes internes d'une pièce, comme celui d'une DSM : lu par le personnel du BNIA qui lit la facturation,
 * écrit par qui la gère, sur un brouillon comme sur une pièce émise ; l'auteur modifie ou supprime la sienne.
 * RG-FAC-21, 26.
 */
@UseCase
public class AnnoterPiece {

    private final FactureRepository factures;
    private final NotesInternes notes;
    private final Controles controles;
    private final Clock horloge;

    public AnnoterPiece(FactureRepository factures, NotesInternes notes, DroitsFacturation droits, Clock horloge) {
        this.factures = factures;
        this.notes = notes;
        this.controles = new Controles(droits);
        this.horloge = horloge;
    }

    /** RG-FAC-21, 26 : les notes de la pièce, du plus ancien au plus récent ; personnel du BNIA seulement. */
    public List<NoteInterne> lister(Acteur acteur, long piece) {
        controles.exigerLectureBnia(acteur);
        exigerPiece(piece);
        return notes.deLaPiece(piece);
    }

    /** RG-FAC-21 : une note interne, quel que soit l'état de la pièce ; jamais imprimée ni transmise. */
    public NoteInterne ecrire(Acteur acteur, long piece, String texte) {
        controles.exigerGestion(acteur);
        exigerPiece(piece);
        return notes.inserer(NoteInterne.ecrire(piece, controles.auteur(acteur), texte, horloge.instant()));
    }

    /** RG-FAC-21 : l'auteur modifie sa note. */
    public NoteInterne modifier(Acteur acteur, long piece, long id, String texte) {
        controles.exigerGestion(acteur);
        NoteInterne note = note(piece, id);
        note.modifier(acteur.utilisateur(), texte, horloge.instant());
        notes.mettreAJour(note);
        return note;
    }

    /** RG-FAC-21 : l'auteur supprime sa note, sans trace. */
    public void supprimer(Acteur acteur, long piece, long id) {
        controles.exigerGestion(acteur);
        note(piece, id).exigerSupprimable(acteur.utilisateur());
        notes.supprimer(id);
    }

    private NoteInterne note(long piece, long id) {
        exigerPiece(piece);
        return notes.parId(id).filter(n -> n.piece() == piece)
                .orElseThrow(() -> new ElementIntrouvable("Note interne introuvable : n° " + id + "."));
    }

    private void exigerPiece(long piece) {
        if (factures.parId(piece).isEmpty()) {
            throw new ElementIntrouvable("Pièce introuvable : n° " + piece + ".");
        }
    }
}
