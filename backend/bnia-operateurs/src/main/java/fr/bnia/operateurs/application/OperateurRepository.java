package fr.bnia.operateurs.application;

import fr.bnia.operateurs.domain.CodeOperateur;
import fr.bnia.operateurs.domain.Operateur;
import java.util.Optional;

/** Charge et enregistre l'agrégat {@code Operateur} d'un bloc (ADR-004, ADR-012). */
public interface OperateurRepository {

    Optional<Operateur> parId(long id);

    /** L'opérateur avec son identifiant attribué par la base. */
    Operateur inserer(Operateur operateur);

    void mettreAJour(Operateur operateur);

    /**
     * RG-OPE-25.
     *
     * @throws fr.bnia.commun.domain.ElementUtilise si d'autres données y font référence
     */
    void supprimer(long id);

    /** RG-OPE-04 : sans distinction de casse ; {@code sauf} exclut l'opérateur modifié. */
    boolean existeCode(CodeOperateur code, Long sauf);

    /** RG-OPE-07 : sans distinction de casse ; {@code sauf} exclut l'opérateur modifié. */
    boolean existeIdentifiantOdg(String identifiantOdg, Long sauf);
}
