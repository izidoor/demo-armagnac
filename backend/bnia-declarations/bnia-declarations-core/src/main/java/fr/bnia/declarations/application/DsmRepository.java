package fr.bnia.declarations.application;

import fr.bnia.declarations.domain.CalendrierDeclaratif.DsmPassee;
import fr.bnia.declarations.domain.DeclarationStocksMouvements;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/** Port d'écriture de l'agrégat {@link DeclarationStocksMouvements}. */
public interface DsmRepository {

    /**
     * @return la DSM relue, avec ses identifiants
     * @throws fr.bnia.commun.domain.ConflitUnicite si l'opérateur a déjà une DSM non validée ou une DSM pour
     *                                              cette période (RG-DEC-04, 05)
     */
    DeclarationStocksMouvements inserer(DeclarationStocksMouvements dsm);

    /**
     * @return la DSM relue, avec les identifiants des lignes ajoutées
     * @throws fr.bnia.commun.domain.ModificationConcurrente si elle a changé depuis son chargement
     */
    DeclarationStocksMouvements mettreAJour(DeclarationStocksMouvements dsm);

    Optional<DeclarationStocksMouvements> parId(long id);

    void supprimer(long id);

    /** Les DSM de l'opérateur, pour le calendrier (RG-DEC-04, 05). */
    List<DsmPassee> dsmsDe(long operateur);

    /** RG-DEC-05 : la DSM non validée de l'opérateur, au plus une. */
    Optional<DeclarationStocksMouvements> nonValideeDe(long operateur);

    /** RG-DEC-08 : une DSM validée commence à cette date. */
    boolean existeValideeCommencantLe(long operateur, LocalDate debut);
}
