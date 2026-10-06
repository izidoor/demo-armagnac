package fr.bnia.referentiels.adapter.persistence;

import fr.bnia.referentiels.application.FiltreValeurs;
import fr.bnia.referentiels.application.ValeurSimpleLigne;
import fr.bnia.referentiels.application.ValeursSimplesLecture;
import fr.bnia.referentiels.domain.ValeurSimple;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.jdbc.core.simple.JdbcClient;

/**
 * Liste simple (code, libellé, actif) : écriture, et listing du back-office en SQL direct.
 */
abstract class ValeursSimplesJdbc<T extends ValeurSimple, R> extends ValeursJdbc<T, R>
        implements ValeursSimplesLecture<T> {

    protected ValeursSimplesJdbc(JdbcAggregateTemplate template, JdbcClient jdbcClient, Class<R> typeLigne,
            String table) {
        super(template, jdbcClient, typeLigne, table);
    }

    @Override
    public List<ValeurSimpleLigne> lister(FiltreValeurs filtre) {
        List<String> conditions = new ArrayList<>();
        Map<String, Object> parametres = new HashMap<>();
        if (filtre.actif() != null) {
            conditions.add("actif = :actif");
            parametres.put("actif", filtre.actif());
        }
        if (filtre.recherche() != null) {
            conditions.add("(code ilike :motif escape '\\' or libelle ilike :motif escape '\\')");
            parametres.put("motif", Sql.motifContient(filtre.recherche()));
        }
        String where = conditions.isEmpty() ? "" : " where " + String.join(" and ", conditions);
        return jdbcClient.sql("select code, libelle, actif from " + table + where + " order by libelle, code")
                .params(parametres)
                .query(ValeurSimpleLigne.class)
                .list();
    }
}
