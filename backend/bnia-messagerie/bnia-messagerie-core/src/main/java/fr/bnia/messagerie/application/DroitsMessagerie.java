package fr.bnia.messagerie.application;

import fr.bnia.messagerie.api.Acteur;
import fr.bnia.messagerie.api.TypeObjet;
import fr.bnia.messagerie.domain.Cote;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Port de l'hôte (A-MSG-3), implémenté par l'infra sur la partie publiée d'{@code operateurs} : le côté d'un
 * acteur, ses droits sur le module de chaque type d'objet (que seul l'adapter connaît), le rattachement des auteurs,
 * les noms affichés. La règle d'écriture (RG-MSG-01, 02) reste dans les cas d'usage.
 */
public interface DroitsMessagerie {

    /**
     * RG-MSG-03 : le côté d'où l'acteur voit cet opérateur : {@code OPERATEUR} s'il en est membre actif,
     * {@code BNIA} s'il appartient au personnel du BNIA, vide sinon.
     */
    Optional<Cote> cote(Acteur acteur, long operateur);

    /** RG-MSG-01 : lire le module de ce type d'objet sur cet opérateur. */
    boolean lit(Acteur acteur, TypeObjet type, long operateur);

    /** RG-MSG-01 : gérer le module de ce type d'objet sur cet opérateur. */
    boolean gere(Acteur acteur, TypeObjet type, long operateur);

    /** RG-MSG-02 : gérer le module « Messagerie » sur cet opérateur (conversations libres). */
    boolean gereMessagerie(Acteur acteur, long operateur);

    /** RG-MSG-11, 15 : l'acteur appartient au personnel du BNIA (rattachement actif dans l'opérateur BNIA). */
    boolean personnelBnia(Acteur acteur);

    /** RG-MSG-11, 15 : les types d'objet dont l'acteur lit le module sur tous les opérateurs (personnel du BNIA). */
    Set<TypeObjet> typesLusParLeBnia(Acteur acteur);

    /** RG-MSG-11, 15 : les types d'objet dont l'acteur lit le module sur cet opérateur, comme membre. */
    Set<TypeObjet> typesLus(Acteur acteur, long operateur);

    /** RG-MSG-02, 13 : l'utilisateur a un rattachement actif à cet opérateur. */
    boolean membreActif(long utilisateur, long operateur);

    /** Copie du nom de l'auteur, à l'instant de l'action ; vide s'il n'existe plus. */
    Optional<String> nom(long utilisateur);

    /** Code et raison sociale de ces opérateurs, en une lecture, pour les listes ; un inconnu est ignoré. */
    Map<Long, OperateurNomme> operateurs(Collection<Long> ids);

    /** Un opérateur tel qu'affiché dans la messagerie. */
    record OperateurNomme(long id, String code, String raisonSociale) {
    }
}
