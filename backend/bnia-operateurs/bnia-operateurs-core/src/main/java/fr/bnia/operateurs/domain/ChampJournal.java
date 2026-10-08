package fr.bnia.operateurs.domain;

/** Champs d'une personne ou d'un accès, tels que le journal les nomme (RG-OPE-34). */
public enum ChampJournal {

    TITRE("titre", true),
    NOM("nom", false),
    PRENOM("prénom", false),
    EMAIL("e-mail", false),
    FONCTION("fonction", false),
    TELEPHONE_PROFESSIONNEL("téléphone professionnel", false),
    TELEPHONE_SECONDAIRE("téléphone secondaire", false),
    ADRESSE("adresse", false),
    CODE_POSTAL("code postal", false),
    VILLE("ville", false),
    PAYS("pays", true),
    CENTRES_INTERET("centres d'intérêt", true),
    OBSERVATION("observation", false),
    ROLE("rôle", false),
    STATUT("statut", false),
    MOTIF("motif", false);

    private final String libelle;
    private final boolean reference;

    ChampJournal(String libelle, boolean reference) {
        this.libelle = libelle;
        this.reference = reference;
    }

    public String libelle() {
        return libelle;
    }

    /** Vrai si les valeurs sont des identifiants de {@code referentiels}. */
    public boolean reference() {
        return reference;
    }
}
