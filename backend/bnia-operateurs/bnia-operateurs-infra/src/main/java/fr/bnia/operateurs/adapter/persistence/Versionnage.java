package fr.bnia.operateurs.adapter.persistence;

import fr.bnia.commun.domain.ModificationConcurrente;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;

/**
 * RG-OPE-51 : une ligne versionnée ({@code @Version}) n'est mise à jour que si sa version n'a pas changé
 * depuis son chargement ; sinon, la modification concurrente est refusée (409).
 */
final class Versionnage {

    private Versionnage() {
    }

    /** @param objet « La fiche de Jeanne Durand », pour le message */
    static <T> void mettreAJour(JdbcAggregateTemplate template, T ligne, String objet) {
        try {
            template.update(ligne);
        } catch (OptimisticLockingFailureException concurrente) {
            throw new ModificationConcurrente("RG-OPE-51", objet + " a été modifiée entre-temps par quelqu'un"
                    + " d'autre : rechargez l'écran, puis recommencez.");
        }
    }
}
