package fr.bnia.facturation.application;

import fr.bnia.facturation.domain.NoteInterne;
import java.util.List;
import java.util.Optional;

/** Port d'écriture du carnet de notes internes d'une pièce (RG-FAC-21), à part de la pièce et de son journal. */
public interface NotesInternes {

    /** @return la note, avec son identifiant */
    NoteInterne inserer(NoteInterne note);

    void mettreAJour(NoteInterne note);

    Optional<NoteInterne> parId(long id);

    /** Du plus ancien au plus récent. */
    List<NoteInterne> deLaPiece(long piece);

    void supprimer(long id);

    /** RG-FAC-18 : les notes d'un brouillon abandonné disparaissent avec lui. */
    void supprimerDeLaPiece(long piece);
}
