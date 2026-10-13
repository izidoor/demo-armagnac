package fr.bnia.declarations.domain;

/** Les cinq onglets du format unique de la DSM, dans l'ordre (§4.4.3.1 p.23-24, spec 05 §3.1). */
public enum OngletDsm {

    RECAPITULATIF("Récapitulatif"),
    MOUVEMENTS_ENTREE("Mouvements en entrée"),
    MOUVEMENTS_SORTIE("Mouvements en sortie"),
    VENTES_FRANCE("Ventes France"),
    VENTES_EXPORT("Ventes à l'exportation");

    private final String libelle;

    OngletDsm(String libelle) {
        this.libelle = libelle;
    }

    public String libelle() {
        return libelle;
    }
}
