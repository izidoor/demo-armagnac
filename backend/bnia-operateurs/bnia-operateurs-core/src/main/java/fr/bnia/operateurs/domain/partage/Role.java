package fr.bnia.operateurs.domain.partage;

/**
 * Rôle d'un rattachement (spec 02 §3.2.7, vision §2.5) : liste fixe, définie par l'application. Un rôle
 * tenu dans l'opérateur BNIA vaut pour tous les opérateurs (RG-OPE-32).
 */
public enum Role {

    OBSERVATEUR("Observateur"),
    AGENT("Agent"),
    ADMIN("Admin");

    private final String libelle;

    Role(String libelle) {
        this.libelle = libelle;
    }

    public String libelle() {
        return libelle;
    }

    /** Tableau rôles × modules (spec 02 §3.2.7) : tout rôle lit ; qui gère dépend du module. */
    public boolean gere(Module module) {
        return switch (module) {
            case CONTACTS, PARAMETRAGE, DECLARATIONS, FACTURATION -> this != OBSERVATEUR;
            case ACCES -> this == ADMIN;
        };
    }
}
