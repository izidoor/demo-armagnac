package fr.bnia.operateurs.adapter.persistence;

/**
 * Utilitaires SQL du contexte.
 */
final class Sql {

    private static final String ADRESSE_COMPLETE =
            "o.adresse_ligne1 is not null and o.code_postal is not null and o.ville is not null"
                    + " and o.pays_id is not null";

    /**
     * RG-OPE-02 : condition SQL d'une fiche facturable, sur la table {@code operateur} aliasée {@code o}. Même
     * liste que {@code Operateur.manquePourFacturer} : SIRET, adresse, destinataire des factures qui donne un
     * e-mail (RG-OPE-23).
     */
    static final String FACTURABLE = "(o.siret is not null and " + ADRESSE_COMPLETE
            + " and (o.destinataire_factures_email is not null or exists (select 1 from "
            + OperateurJdbc.SCHEMA + ".fiche_professionnelle f"
            + " left join " + OperateurJdbc.SCHEMA + ".contact c on c.fiche_id = f.id"
            + " left join " + OperateurJdbc.SCHEMA + ".rattachement r on r.fiche_id = f.id"
            + " left join " + OperateurJdbc.SCHEMA + ".utilisateur u on u.id = r.utilisateur_id"
            + " where f.id = o.destinataire_factures_fiche_id and coalesce(c.email, u.email) is not null)))";

    /** RG-OPE-02 : condition SQL d'une fiche certifiable ; même liste que {@code Operateur.manquePourCertifier}. */
    static final String CERTIFIABLE = "(" + ADRESSE_COMPLETE + " and o.region_id is not null)";

    private Sql() {
    }

    /** Motif {@code like} « contient », caractères spéciaux échappés par {@code \}. */
    static String motifContient(String texte) {
        return "%" + echapper(texte) + "%";
    }

    private static String echapper(String texte) {
        return texte.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}
