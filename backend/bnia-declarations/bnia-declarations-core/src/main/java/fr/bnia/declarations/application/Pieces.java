package fr.bnia.declarations.application;

import fr.bnia.declarations.domain.Drm;
import fr.bnia.declarations.domain.Justificatif;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

/**
 * Port d'écriture des pièces d'une DSM, justificatifs et DRM (spec 05 §3.2.2, §3.2.3), enregistrées à part de
 * la DSM comme les commentaires : un dépôt ne change pas sa version. Chaque lecture d'une DSM tient en une
 * requête.
 */
public interface Pieces {

    /** @return le justificatif, avec son identifiant */
    Justificatif inserer(Justificatif justificatif);

    void mettreAJour(Justificatif justificatif);

    Optional<Justificatif> justificatif(long id);

    /** Dans l'ordre de dépôt. */
    List<Justificatif> justificatifsDe(long dsm);

    void supprimerJustificatif(long id);

    /** @return la DRM, avec son identifiant */
    Drm inserer(Drm drm);

    Optional<Drm> drm(long dsm, YearMonth mois);

    /** Dans l'ordre des mois. */
    List<Drm> drmsDe(long dsm);

    void supprimerDrm(long id);
}
