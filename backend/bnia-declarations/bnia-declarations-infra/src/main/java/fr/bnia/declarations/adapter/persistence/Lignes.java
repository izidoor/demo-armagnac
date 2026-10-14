package fr.bnia.declarations.adapter.persistence;

import fr.bnia.commun.domain.Millesime;
import fr.bnia.commun.domain.ModificationConcurrente;
import fr.bnia.declarations.domain.Denomination;
import fr.bnia.declarations.domain.IdentiteLigne;
import fr.bnia.declarations.domain.partage.CleConditionnement;
import fr.bnia.declarations.domain.partage.CompteDAgeDsm;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;

/** Outils de mapping communs aux lignes de DSM et de stock initial, et verrou optimiste. */
final class Lignes {

    static final String SCHEMA = "declarations";

    private Lignes() {
    }

    /** L'identité d'une ligne relue : déjà contrôlée à l'enregistrement. */
    static IdentiteLigne identite(CleConditionnement cle, Integer millesime, String denomination, String compte) {
        return new IdentiteLigne(cle, millesime == null ? null : Millesime.de(millesime),
                denomination == null ? null : Denomination.de(denomination), CompteDAgeDsm.de(compte));
    }

    static Integer millesime(IdentiteLigne identite) {
        return identite.millesime() == null ? null : identite.millesime().annee();
    }

    static String denomination(IdentiteLigne identite) {
        return identite.denomination() == null ? null : identite.denomination().texte();
    }

    static String denominationNormalisee(IdentiteLigne identite) {
        return identite.denomination() == null ? null : identite.denomination().normalisee();
    }

    /**
     * Une ligne versionnée ({@code @Version}) n'est mise à jour que si sa version n'a pas changé depuis son
     * chargement ; sinon, la modification concurrente est refusée (409).
     *
     * @param objet « La DSM de juin 2026 », pour le message
     */
    static <T> T mettreAJour(JdbcAggregateTemplate template, T ligne, String objet) {
        try {
            return template.update(ligne);
        } catch (OptimisticLockingFailureException concurrente) {
            throw new ModificationConcurrente(null, objet + " a été modifiée entre-temps par quelqu'un d'autre :"
                    + " rechargez l'écran, puis recommencez.");
        }
    }
}
