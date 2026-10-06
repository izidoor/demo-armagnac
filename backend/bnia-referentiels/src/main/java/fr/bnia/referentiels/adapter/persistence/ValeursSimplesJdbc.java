package fr.bnia.referentiels.adapter.persistence;

import fr.bnia.referentiels.application.FiltreValeurs;
import fr.bnia.referentiels.application.ValeurSimpleLigne;
import fr.bnia.referentiels.application.ValeursSimplesLecture;
import fr.bnia.referentiels.domain.ValeurSimple;
import java.util.List;
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
        Sql.Where where = Sql.where(filtre, "code", "libelle");
        return jdbcClient.sql("select code, libelle, actif from " + table + where.clause() + " order by libelle, code")
                .params(where.parametres())
                .query(ValeurSimpleLigne.class)
                .list();
    }
}
