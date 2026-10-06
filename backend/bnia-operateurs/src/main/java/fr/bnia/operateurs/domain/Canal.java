package fr.bnia.operateurs.domain;

/**
 * D'où vient une action : certaines règles en dépendent (RG-OPE-30, 35, 36). Le libellé sert d'auteur
 * au journal tant que {@code acces} n'identifie pas la personne.
 */
public enum Canal {

    BACK_OFFICE("BNIA"),
    PORTAIL("Portail"),
    /** Événement venu de {@code acces} (« Mon compte »). */
    COMPTE("Mon compte");

    private final String auteur;

    Canal(String auteur) {
        this.auteur = auteur;
    }

    public String auteur() {
        return auteur;
    }
}
