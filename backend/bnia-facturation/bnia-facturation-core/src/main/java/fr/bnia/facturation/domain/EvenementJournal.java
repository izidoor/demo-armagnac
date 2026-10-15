package fr.bnia.facturation.domain;

/** Événements du journal d'une pièce (spec 08 §3.9, RG-FAC-20) : liste fermée. */
public enum EvenementJournal {
    DEMANDEE("Facture demandée"),
    CREEE("Avoir créé"),
    MODIFIEE("Modifiée"),
    EMISE("Émise"),
    CONSULTEE("Consultée"),
    ANNULEE("Annulée"),
    AVOIR_EMIS("Avoir émis"),
    DEPOSEE("Déposée dans Zeendoc"),
    REJETEE("Rejetée par Zeendoc"),
    EN_ANOMALIE("Dépôt en anomalie"),
    PAYEE("Payée"),
    NOTE("Note");

    private final String libelle;

    EvenementJournal(String libelle) {
        this.libelle = libelle;
    }

    public String libelle() {
        return libelle;
    }
}
