package fr.bnia.referentiels.domain.partage;

/**
 * Niveau de détail du certificat export d'un pays (§4.8.5 p.42, C.7.2.1 p.123) :
 * {@link #CUMUL_DEGRE} correspond au certificat simplifié (une ligne par degré),
 * {@link #DETAIL_COMPTE_AGE} et {@link #DETAIL_MILLESIME} au certificat détaillé.
 * Valeur par défaut : {@link #CUMUL_DEGRE} (H-REF-2).
 */
public enum NiveauDetailCertificat {
    CUMUL_DEGRE,
    DETAIL_COMPTE_AGE,
    DETAIL_MILLESIME;

    public static final NiveauDetailCertificat PAR_DEFAUT = CUMUL_DEGRE;
}
