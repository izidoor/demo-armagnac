package fr.bnia.commun.application;

/**
 * Les formats de fichier que le stockage commun reconnaît, chacun par sa signature et non par son extension
 * (ADR-018 amendé le 2026-10-17) : l'appelant dit ceux qu'il admet.
 */
public enum FormatDocument {

    PDF("application/pdf", "un PDF"),
    PNG("image/png", "une image PNG"),
    JPEG("image/jpeg", "une image JPEG"),
    XLSX("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "un classeur Excel (.xlsx)");

    private final String type;
    private final String designation;

    FormatDocument(String type, String designation) {
        this.type = type;
        this.designation = designation;
    }

    /** Le type MIME enregistré avec le document. */
    public String type() {
        return type;
    }

    /** Pour les messages : « un PDF », « une image PNG »… */
    public String designation() {
        return designation;
    }
}
