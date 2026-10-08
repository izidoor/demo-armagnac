package fr.bnia.referentiels.domain.partage;

/**
 * Langue du certificat export d'un pays : le français, associé à une langue étrangère
 * (§4.8.4 p.42, C.7.2.1 p.122). Valeur par défaut : {@link #FR_EN} (H-REF-2).
 */
public enum LangueCertificat {
    FR_EN,
    FR_ES,
    FR_IT;

    public static final LangueCertificat PAR_DEFAUT = FR_EN;
}
