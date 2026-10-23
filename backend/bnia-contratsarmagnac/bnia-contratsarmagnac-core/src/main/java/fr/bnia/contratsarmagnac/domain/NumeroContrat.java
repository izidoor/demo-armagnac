package fr.bnia.contratsarmagnac.domain;

/** Numéro d'un contrat (RG-CA-06, C.4.1.1.1 p.112) : {@code CA-AAAA-NNNN}, année de l'ouverture, rang dans l'année. */
public final class NumeroContrat {

    private NumeroContrat() {
    }

    public static String de(int annee, int rang) {
        return "CA-%d-%04d".formatted(annee, rang);
    }
}
