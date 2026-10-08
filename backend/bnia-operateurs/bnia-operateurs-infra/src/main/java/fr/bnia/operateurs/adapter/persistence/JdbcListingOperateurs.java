package fr.bnia.operateurs.adapter.persistence;

import fr.bnia.operateurs.application.Doublon;
import fr.bnia.operateurs.application.FiltreOperateurs;
import fr.bnia.operateurs.application.ListingOperateurs;
import fr.bnia.operateurs.application.NumerotationLecture;
import fr.bnia.operateurs.application.OperateurLigne;
import fr.bnia.operateurs.application.OperateurResume;
import fr.bnia.operateurs.domain.Cvi;
import fr.bnia.operateurs.domain.NumeroAccise;
import fr.bnia.operateurs.domain.SerieCode;
import fr.bnia.operateurs.domain.Siret;
import fr.bnia.operateurs.domain.Textes;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/** Lectures du back-office et numérotation des codes, en SQL direct (ADR-004, A-OPE-5). */
@Repository
class JdbcListingOperateurs implements ListingOperateurs, NumerotationLecture {

    private static final String TABLE = OperateurJdbc.SCHEMA + ".operateur o";
    private static final String RESUME = "o.id, o.code, o.raison_sociale, o.ville, o.actif";

    private final JdbcClient jdbcClient;

    JdbcListingOperateurs(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public List<OperateurLigne> lister(FiltreOperateurs filtre) {
        List<String> conditions = new ArrayList<>();
        Map<String, Object> parametres = new HashMap<>();
        if (filtre.type() != null) {
            conditions.add("o.type = :type");
            parametres.put("type", filtre.type().name());
        }
        contient(conditions, parametres, "o.code", "code", filtre.code());
        contient(conditions, parametres, "o.raison_sociale", "raisonSociale", filtre.raisonSociale());
        contient(conditions, parametres, "o.nom_commercial", "nomCommercial", filtre.nomCommercial());
        contient(conditions, parametres, "o.ville", "ville", filtre.ville());
        String codePostal = Textes.facultatif(filtre.codePostal());
        if (codePostal != null) {
            conditions.add("o.code_postal like :codePostal escape '\\'");
            parametres.put("codePostal", Sql.motifCommencePar(codePostal));
        }
        if (filtre.actif() != null) {
            conditions.add("o.actif = :actif");
            parametres.put("actif", filtre.actif());
        }
        if (filtre.incomplet() != null) {
            conditions.add(filtre.incomplet() ? Sql.INCOMPLET : "not " + Sql.INCOMPLET);
        }
        String where = conditions.isEmpty() ? "" : " where " + String.join(" and ", conditions);
        return jdbcClient.sql("select o.id, o.type, o.code, o.raison_sociale, o.nom_commercial, o.code_postal, o.ville,"
                        + " o.telephone1, o.email_general, o.actif, " + Sql.INCOMPLET + " as incomplet from " + TABLE
                        + where + " order by o.raison_sociale, o.code")
                .params(parametres)
                .query(OperateurLigne.class)
                .list();
    }

    @Override
    public List<OperateurResume> duGroupe(String groupeNormalise, long sauf) {
        return jdbcClient.sql("select " + RESUME + " from " + TABLE
                        + " where o.groupe_normalise = :groupe and o.id <> :sauf order by o.code")
                .param("groupe", groupeNormalise)
                .param("sauf", sauf)
                .query(OperateurResume.class)
                .list();
    }

    @Override
    public List<String> groupes(String recherche) {
        return jdbcClient.sql("select distinct on (o.groupe_normalise) o.groupe from " + TABLE
                        + " where o.groupe is not null and (cast(:motif as text) is null"
                        + " or o.groupe_normalise like :motif escape '\\')"
                        + " order by o.groupe_normalise, o.groupe")
                .param("motif", recherche == null ? null : Sql.motifContient(Textes.normalise(recherche)), Types.VARCHAR)
                .query(String.class)
                .list();
    }

    @Override
    public List<Doublon> doublons(String raisonSocialeNormalisee, Cvi cvi, Siret siret, NumeroAccise accise, Long sauf) {
        List<Doublon> doublons = new ArrayList<>();
        if (raisonSocialeNormalisee != null) {
            doublons.addAll(portant("raison_sociale_normalisee", raisonSocialeNormalisee, sauf,
                    Doublon.Champ.RAISON_SOCIALE, "RG-OPE-08"));
        }
        if (cvi != null) {
            doublons.addAll(portant("cvi", cvi.valeur(), sauf, Doublon.Champ.CVI, "RG-OPE-06"));
        }
        if (siret != null) {
            doublons.addAll(portant("siret", siret.valeur(), sauf, Doublon.Champ.SIRET, "RG-OPE-06"));
        }
        if (accise != null) {
            doublons.addAll(portant("accise", accise.valeur(), sauf, Doublon.Champ.ACCISE, "RG-OPE-06"));
        }
        return doublons;
    }

    /** @param colonne constante, jamais une saisie utilisateur ; les valeurs y sont stockées normalisées */
    private List<Doublon> portant(String colonne, String valeur, Long sauf, Doublon.Champ champ, String regle) {
        return jdbcClient.sql("select " + RESUME + " from " + TABLE
                        + " where o." + colonne + " = :valeur and o.id is distinct from :sauf order by o.code")
                .param("valeur", valeur)
                .param("sauf", sauf, Types.BIGINT)
                .query((rs, i) -> new Doublon(champ, regle, resume(rs)))
                .list();
    }

    @Override
    public OptionalInt plusGrandNumero(SerieCode serie) {
        int max = jdbcClient.sql("select coalesce(max(cast(substring(upper(o.code) from :debut) as integer)), -1)"
                        + " from " + TABLE + " where upper(o.code) ~ :motif")
                .param("debut", serie.prefixe().length() + 1)
                .param("motif", serie.motif())
                .query(Integer.class)
                .single();
        return max < 0 ? OptionalInt.empty() : OptionalInt.of(max);
    }

    private static OperateurResume resume(ResultSet rs) throws SQLException {
        return new OperateurResume(rs.getLong("id"), rs.getString("code"), rs.getString("raison_sociale"),
                rs.getString("ville"), rs.getBoolean("actif"));
    }

    /** @param colonne constante, jamais une saisie utilisateur */
    private static void contient(List<String> conditions, Map<String, Object> parametres, String colonne,
            String parametre, String texte) {
        String valeur = Textes.facultatif(texte);
        if (valeur != null) {
            conditions.add(colonne + " ilike :" + parametre + " escape '\\'");
            parametres.put(parametre, Sql.motifContient(valeur));
        }
    }
}
