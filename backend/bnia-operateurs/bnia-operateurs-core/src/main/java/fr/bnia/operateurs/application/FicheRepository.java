package fr.bnia.operateurs.application;

import fr.bnia.operateurs.domain.FicheProfessionnelle;
import java.util.Optional;

/** Agrégat {@code FicheProfessionnelle} (A-OPE-6). */
public interface FicheRepository {

    Optional<FicheProfessionnelle> parId(long id);

    FicheProfessionnelle inserer(FicheProfessionnelle fiche);

    /** @throws fr.bnia.commun.domain.ModificationConcurrente (RG-OPE-51) */
    void mettreAJour(FicheProfessionnelle fiche);

    void supprimer(long id);
}
