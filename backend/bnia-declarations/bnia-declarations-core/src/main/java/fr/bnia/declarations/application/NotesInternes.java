package fr.bnia.declarations.application;

import fr.bnia.declarations.domain.NoteInterne;
import java.util.List;
import java.util.Optional;

/**
 * Port d'écriture du carnet de notes internes d'une DSM (spec 15 §3.7), enregistré à part de la DSM : l'écrire ne
 * change pas sa version.
 */
public interface NotesInternes {

    /** @return la note, avec son identifiant */
    NoteInterne inserer(NoteInterne note);

    void mettreAJour(NoteInterne note);

    Optional<NoteInterne> parId(long id);

    /** Du plus ancien au plus récent. */
    List<NoteInterne> deLaDsm(long dsm);

    void supprimer(long id);

    /** RG-DEC-22 : les notes d'un brouillon supprimé disparaissent avec lui. */
    void supprimerDeLaDsm(long dsm);
}
