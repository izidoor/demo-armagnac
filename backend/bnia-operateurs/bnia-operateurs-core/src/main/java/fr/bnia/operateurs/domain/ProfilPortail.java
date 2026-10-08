package fr.bnia.operateurs.domain;

/**
 * Ce qu'un opérateur modifie lui-même de son identité au portail (RG-OPE-36) : titre, nom commercial,
 * adresse et pays, téléphones, e-mail général, sites, GPS, accord de diffusion général. Le reste
 * (type, code, raison sociale, groupe, identifiants administratifs, membre BNIA…) est en lecture, et le
 * commentaire interne masqué.
 */
public record ProfilPortail(
        Long titre,
        String nomCommercial,
        Adresse adresse,
        String telephone1,
        String telephone2,
        String portable,
        Email emailGeneral,
        SiteWeb siteWeb,
        SiteWeb siteCommercial,
        CoordonneesGps gps,
        boolean accordDiffusionGeneral) {
}
