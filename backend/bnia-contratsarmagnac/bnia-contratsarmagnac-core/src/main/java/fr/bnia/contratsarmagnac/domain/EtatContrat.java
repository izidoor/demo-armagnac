package fr.bnia.contratsarmagnac.domain;

/** État d'un contrat (spec 06 §3.1, analyse §3) : projet, conclu par l'accord des parties, puis exécuté ou annulé. */
public enum EtatContrat {

    PROJET("Projet"),
    CONCLU("Conclu"),
    EXECUTE("Exécuté"),
    ANNULE("Annulé"),
    REFUSE("Refusé");

    private final String libelle;

    EtatContrat(String libelle) {
        this.libelle = libelle;
    }

    public String libelle() {
        return libelle;
    }
}
