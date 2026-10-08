package fr.bnia.operateurs.application;

import fr.bnia.operateurs.domain.CodeOperateur;
import fr.bnia.operateurs.domain.Operateur;
import java.util.Optional;

/** Charge et enregistre l'agrégat {@code Operateur} d'un bloc (ADR-004, ADR-012). */
public interface OperateurRepository {

    Optional<Operateur> parId(long id);

    /** L'opérateur avec son identifiant attribué par la base. */
    Operateur inserer(Operateur operateur);

    /**
     * @throws fr.bnia.commun.domain.ModificationConcurrente si l'opérateur a changé depuis son chargement (RG-OPE-51)
     */
    void mettreAJour(Operateur operateur);

    /**
     * RG-OPE-25 : avec ses personnes (contacts, fiches, rattachements, invitations).
     *
     * @throws fr.bnia.commun.domain.ElementUtilise si d'autres données y font référence
     */
    void supprimer(long id);

    /** RG-OPE-04 : sans distinction de casse ; {@code sauf} exclut l'opérateur modifié. */
    boolean existeCode(CodeOperateur code, Long sauf);

    /** RG-OPE-07 : sans distinction de casse ; {@code sauf} exclut l'opérateur modifié. */
    boolean existeIdentifiantOdg(String identifiantOdg, Long sauf);

    /**
     * RG-OPE-35 : verrouille la ligne de l'opérateur jusqu'à la fin de la transaction
     * ({@code select … for update}), avant de compter ou de changer ses ADMIN (A-OPE-6).
     */
    void verrouiller(long id);

    /** RG-OPE-26 : faux s'il est inactif ou n'existe pas. */
    boolean estActif(long id);

    /** RG-OPE-39 : l'opérateur BNIA, unique, créé par la migration. */
    long idBnia();
}
