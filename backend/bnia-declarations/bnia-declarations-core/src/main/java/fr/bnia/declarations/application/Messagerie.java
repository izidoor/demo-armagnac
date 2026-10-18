package fr.bnia.declarations.application;

import fr.bnia.operateurs.domain.partage.Acteur;

/**
 * Port vers la conversation d'une DSM (ADR-023, A-MSG-1), dans les termes de {@code declarations} ; implémenté
 * par l'infra sur l'API de {@code messagerie}, dans la transaction de l'appelant.
 */
public interface Messagerie {

    /** RG-DEC-20, RG-MSG-08 : le motif d'une demande de compléments, protégé, au nom de l'agent du BNIA. */
    void deposerMotif(long dsm, Acteur auteur, String motif);

    /** RG-DEC-22, RG-MSG-07 : la conversation d'un brouillon supprimé disparaît avec lui. */
    void supprimerConversation(long dsm);
}
