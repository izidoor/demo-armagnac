package fr.bnia.declarations.domain;

/**
 * Actions du journal des déclarations (RG-DEC-23) : liste fermée. Le détail (nom de fichier, mois de DRM,
 * type de pièce, format d'édition) accompagne l'action, jamais de valeurs avant et après.
 */
public enum ActionJournalDeclarations {

    DSM_INITIEE("DSM initiée"),
    DSM_ENREGISTREE("DSM enregistrée"),
    DSM_IMPORTEE("DSM importée"),
    DSM_ENVOYEE("DSM envoyée"),
    DSM_PRISE_EN_INSTRUCTION("DSM prise en instruction"),
    COMPLEMENTS_DEMANDES("Compléments demandés"),
    DSM_VALIDEE("DSM validée"),
    DSM_EDITEE("DSM éditée"),
    BROUILLON_SUPPRIME("Brouillon supprimé"),
    JUSTIFICATIF_DEPOSE("Justificatif déposé"),
    JUSTIFICATIF_SUPPRIME("Justificatif supprimé"),
    DRM_DEPOSEE("DRM déposée"),
    DRM_SUPPRIMEE("DRM supprimée"),
    POINT_DE_DEPART_FIXE("Point de départ fixé"),
    POINT_DE_DEPART_MODIFIE("Point de départ modifié"),
    POINT_DE_DEPART_IMPORTE("Point de départ importé"),
    /** RG-DEC-43 (spec 08 §3.13). */
    FACTURATION_DEMANDEE("Facturation demandée"),
    /** RG-DEC-34 : reçu de {@code facturation} ; détail : le numéro. */
    FACTURE_EMISE("Facture émise"),
    /** RG-DEC-34 : brouillon abandonné dans {@code facturation} ; la DSM redevient à facturer. */
    FACTURATION_ABANDONNEE("Facturation abandonnée");

    private final String libelle;

    ActionJournalDeclarations(String libelle) {
        this.libelle = libelle;
    }

    public String libelle() {
        return libelle;
    }
}
