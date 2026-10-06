package fr.bnia.operateurs.application;

import fr.bnia.operateurs.domain.Contact;

/**
 * Un contact, avec ses désignations dans l'opérateur : l'écran avertit qu'elles tomberont si le contact
 * est désactivé ou supprimé (RG-OPE-22, 23, 33).
 */
public record FicheContact(Contact contact, boolean contactPrincipal, boolean destinataireFactures) {
}
