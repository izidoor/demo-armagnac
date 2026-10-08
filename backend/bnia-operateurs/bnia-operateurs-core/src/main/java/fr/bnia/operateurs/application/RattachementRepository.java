package fr.bnia.operateurs.application;

import fr.bnia.operateurs.domain.Rattachement;
import java.util.List;
import java.util.Optional;

/** Agrégat {@code Rattachement} (A-OPE-6). Au plus un par opérateur et utilisateur (RG-OPE-29). */
public interface RattachementRepository {

    Optional<Rattachement> parId(long id);

    /** RG-OPE-30 : le rattachement qui porte cette fiche. */
    Optional<Rattachement> parFiche(long fiche);

    Optional<Rattachement> de(long operateur, long utilisateur);

    List<Rattachement> deLUtilisateur(long utilisateur);

    List<Rattachement> deLOperateur(long operateur);

    Rattachement inserer(Rattachement rattachement);

    void mettreAJour(Rattachement rattachement);

    void supprimer(long id);
}
