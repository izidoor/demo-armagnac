package fr.bnia.operateurs.application;

import fr.bnia.operateurs.domain.ActionJournal;
import fr.bnia.operateurs.domain.Changement;
import fr.bnia.operateurs.domain.TypeCible;
import java.time.Instant;
import java.util.List;

/**
 * Entrée du journal des personnes et des accès, telle qu'enregistrée (RG-OPE-34). Acteur et cible sont
 * copiés : le journal survit à leur suppression. Les valeurs des références sont des libellés.
 *
 * @param acteur    identifiant de l'utilisateur, sans clé étrangère ; {@code null} pour l'application (amorçage, expiration)
 * @param operateur {@code null} pour une action sur un utilisateur (profil, e-mail, blocage)
 */
public record LigneJournal(
        Instant date,
        Long acteur,
        String nomActeur,
        Long operateur,
        ActionJournal action,
        TypeCible cible,
        long idCible,
        String nomCible,
        List<Changement> changements) {

    public LigneJournal {
        changements = List.copyOf(changements);
    }
}
