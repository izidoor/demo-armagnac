package fr.bnia.declarations.application;

import fr.bnia.declarations.domain.Commentaire;
import java.util.List;
import java.util.Optional;

/**
 * Port d'écriture des commentaires d'une DSM (spec 05 §3.2.4), enregistrés à part de la DSM : les écrire ne
 * change pas sa version (décision du 2026-10-13).
 */
public interface Commentaires {

    /** @return le commentaire, avec son identifiant */
    Commentaire inserer(Commentaire commentaire);

    void mettreAJour(Commentaire commentaire);

    Optional<Commentaire> parId(long id);

    /** Du plus ancien au plus récent, internes compris. */
    List<Commentaire> deLaDsm(long dsm);

    void supprimer(long id);

    /** RG-DEC-22 : les commentaires d'un brouillon supprimé disparaissent avec lui. */
    void supprimerDeLaDsm(long dsm);
}
