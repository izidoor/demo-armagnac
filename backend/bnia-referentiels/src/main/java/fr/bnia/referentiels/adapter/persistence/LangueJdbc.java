package fr.bnia.referentiels.adapter.persistence;

import fr.bnia.referentiels.application.LangueRepository;
import fr.bnia.referentiels.domain.Code;
import fr.bnia.referentiels.domain.Libelle;
import fr.bnia.referentiels.domain.Langue;
import org.springframework.data.annotation.Id;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
class LangueJdbc extends ValeursSimplesJdbc<Langue, LangueJdbc.Ligne> implements LangueRepository {

    LangueJdbc(JdbcAggregateTemplate template, JdbcClient jdbcClient) {
        super(template, jdbcClient, Ligne.class, "langue");
    }

    @Override
    protected Ligne versLigne(Langue valeur) {
        return new Ligne(valeur.code().valeur(), valeur.libelle().valeur(), valeur.estActif());
    }

    @Override
    protected Langue versDomaine(Ligne ligne) {
        return Langue.reconstituer(Code.de(ligne.code()), Libelle.de(ligne.libelle()), ligne.actif());
    }

    @Table(schema = SCHEMA, name = "langue")
    record Ligne(@Id String code, String libelle, boolean actif) {
    }
}
