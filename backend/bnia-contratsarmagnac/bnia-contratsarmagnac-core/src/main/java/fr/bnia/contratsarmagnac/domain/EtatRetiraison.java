package fr.bnia.contratsarmagnac.domain;

import java.util.Optional;

/** État d'une retiraison (spec 06 §3.4) : à valider par une partie, puis validée par les deux (RG-CA-13 à 15). */
public enum EtatRetiraison {

    A_VALIDER_VENDEUR("À valider par le vendeur"),
    A_VALIDER_ACHETEUR("À valider par l'acheteur"),
    VALIDEE("Validée");

    private final String libelle;

    EtatRetiraison(String libelle) {
        this.libelle = libelle;
    }

    public String libelle() {
        return libelle;
    }

    /** La partie qui doit valider ; vide une fois validée. */
    public Optional<Partie> attend() {
        return switch (this) {
            case A_VALIDER_VENDEUR -> Optional.of(Partie.VENDEUR);
            case A_VALIDER_ACHETEUR -> Optional.of(Partie.ACHETEUR);
            case VALIDEE -> Optional.empty();
        };
    }

    static EtatRetiraison aValiderPar(Partie partie) {
        return partie == Partie.VENDEUR ? A_VALIDER_VENDEUR : A_VALIDER_ACHETEUR;
    }
}
