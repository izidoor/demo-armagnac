package fr.bnia.operateurs.domain.partage;

import java.util.Optional;

/**
 * Périodicité de la déclaration de stocks et mouvements (C.1.1.1 p.101, §4.4.10 p.28) : seule la valeur
 * courante est conservée (RG-OPE-10).
 */
public enum PeriodiciteDsm {

    MENSUELLE("Mensuelle"),
    TRIMESTRIELLE("Trimestrielle"),
    SEMESTRIELLE("Semestrielle"),
    ANNUELLE("Annuelle");

    private final String libelle;

    PeriodiciteDsm(String libelle) {
        this.libelle = libelle;
    }

    public String libelle() {
        return libelle;
    }

    /**
     * RG-OPE-10 : annuelle pour un producteur, mensuelle pour un négociant, rien pour {@code AUTRE} et
     * {@code BNIA} (RG-OPE-01).
     */
    public static Optional<PeriodiciteDsm> parDefaut(TypeOperateur type) {
        return switch (type) {
            case PRODUCTEUR -> Optional.of(ANNUELLE);
            case NEGOCIANT -> Optional.of(MENSUELLE);
            case AUTRE, BNIA -> Optional.empty();
        };
    }
}
