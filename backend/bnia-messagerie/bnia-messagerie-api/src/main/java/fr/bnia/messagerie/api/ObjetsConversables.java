package fr.bnia.messagerie.api;

import java.util.Optional;

/**
 * Implémenté par l'infra de chaque contexte métier, un par type d'objet (A-MSG-2) : décrit un objet à {@code
 * messagerie}, qui ne connaît pas ses tables.
 */
public interface ObjetsConversables {

    TypeObjet type();

    /**
     * RG-MSG-04, 17 : l'opérateur et le libellé de l'objet ; vide s'il n'existe pas ou ne se prête pas à une
     * conversation (brouillon de facture, par exemple).
     */
    Optional<DescriptionObjet> decrire(String identifiant);
}
