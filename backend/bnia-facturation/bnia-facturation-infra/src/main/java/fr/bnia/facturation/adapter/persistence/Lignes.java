package fr.bnia.facturation.adapter.persistence;

import fr.bnia.facturation.domain.Adresse;

/** Mappings communs du contexte : schéma, adresses en colonnes préfixées ({@code @Embedded}). */
final class Lignes {

    static final String SCHEMA = "facturation";

    private Lignes() {
    }

    /** Une adresse en six colonnes : {@code <préfixe>ligne1} … {@code <préfixe>code_pays}. */
    record AdresseLigne(String ligne1, String ligne2, String codePostal, String ville, String pays, String codePays) {

        static AdresseLigne de(Adresse a) {
            return a == null ? null : new AdresseLigne(a.ligne1(), a.ligne2(), a.codePostal(), a.ville(), a.pays(),
                    a.codePays());
        }

        Adresse versDomaine() {
            return new Adresse(ligne1, ligne2, codePostal, ville, pays, codePays);
        }
    }

    static Adresse adresse(AdresseLigne ligne) {
        return ligne == null ? null : ligne.versDomaine();
    }
}
