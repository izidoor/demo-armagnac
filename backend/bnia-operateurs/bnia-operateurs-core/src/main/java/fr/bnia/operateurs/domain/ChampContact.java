package fr.bnia.operateurs.domain;

/** Champs d'un contact, tels que le journal les nomme (RG-OPE-34). */
public enum ChampContact {

    TITRE("titre", true),
    NOM("nom", false),
    PRENOM("prénom", false),
    FONCTION("fonction", false),
    CENTRES_INTERET("centres d'intérêt", true),
    TELEPHONE_PROFESSIONNEL("téléphone professionnel", false),
    TELEPHONE_SECONDAIRE("téléphone secondaire", false),
    EMAIL("e-mail", false),
    ADRESSE("adresse", false),
    CODE_POSTAL("code postal", false),
    VILLE("ville", false),
    PAYS("pays", true),
    OBSERVATION("observation", false),
    ADMINISTRATEUR("administrateur", false),
    MODULES("modules", false);

    private final String libelle;
    private final boolean reference;

    ChampContact(String libelle, boolean reference) {
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
