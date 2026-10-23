package fr.bnia.contratsarmagnac.domain;

/** Les actions gardées au journal du contrat (spec 06 §3.5, RG-CA-19). */
public enum ActionContrat {

    OUVERTURE("Ouverture du projet"),
    MODIFICATION("Modification"),
    APPROBATION("Approbation"),
    REFUS("Refus"),
    CONCLUSION("Conclusion"),
    RETIRAISON_DECLAREE("Retiraison déclarée"),
    RETIRAISON_MODIFIEE("Retiraison modifiée"),
    RETIRAISON_VALIDEE("Retiraison validée"),
    RETIRAISON_RETIREE("Retiraison retirée"),
    EXECUTION("Exécution"),
    ANNULATION("Annulation"),
    CLOTURE("Clôture sans plus de retiraison"),
    EDITION_PDF("Édition PDF");

    private final String libelle;

    ActionContrat(String libelle) {
        this.libelle = libelle;
    }

    public String libelle() {
        return libelle;
    }
}
