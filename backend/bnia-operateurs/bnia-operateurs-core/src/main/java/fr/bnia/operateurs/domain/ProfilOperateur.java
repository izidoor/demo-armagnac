package fr.bnia.operateurs.domain;

/**
 * Ce qu'un rôle d'un opérateur autre que le BNIA modifie de l'identité, par le module Contacts
 * (RG-OPE-36) : titre, nom commercial, adresse et pays, téléphones, e-mail général, sites, GPS, accord de
 * diffusion général. Le reste (type, code, raison sociale, groupe, identifiants administratifs, membre
 * BNIA…) est réservé au BNIA, et le commentaire interne lui est invisible.
 */
public record ProfilOperateur(
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
