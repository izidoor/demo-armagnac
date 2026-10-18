package fr.bnia.messagerie.api;

/**
 * Les objets métier conversables (spec 15 §3.2), liste fermée : chacun a son contexte, qui implémente {@link
 * ObjetsConversables}, et son module de droits, que seul le port de l'hôte connaît (A-MSG-3). Le contrat d'achat
 * et la demande de certificat s'ajouteront avec leur contexte.
 */
public enum TypeObjet {

    /** Une DSM, dans tout état ({@code declarations}). */
    DSM("DSM"),
    /** Une facture ou un avoir émis dont le client est un opérateur ({@code facturation}, RG-MSG-17). */
    FACTURE("Facture");

    private final String libelle;

    TypeObjet(String libelle) {
        this.libelle = libelle;
    }

    public String libelle() {
        return libelle;
    }
}
