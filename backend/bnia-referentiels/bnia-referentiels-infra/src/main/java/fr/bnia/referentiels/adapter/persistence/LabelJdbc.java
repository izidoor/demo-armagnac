package fr.bnia.referentiels.adapter.persistence;

import fr.bnia.referentiels.application.LabelRepository;
import fr.bnia.referentiels.domain.Code;
import fr.bnia.referentiels.domain.Libelle;
import fr.bnia.referentiels.domain.Label;
import org.springframework.data.annotation.Id;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
class LabelJdbc extends ValeursSimplesJdbc<Label, LabelJdbc.Ligne> implements LabelRepository {

    LabelJdbc(JdbcAggregateTemplate template, JdbcClient jdbcClient) {
        super(template, jdbcClient, Ligne.class, "label");
    }

    @Override
    protected Ligne versLigne(Label valeur) {
        return new Ligne(valeur.id(), valeur.code().valeur(), valeur.libelle().valeur(), valeur.estActif());
    }

    @Override
    protected Label versDomaine(Ligne ligne) {
        return Label.reconstituer(ligne.id(), Code.de(ligne.code()), Libelle.de(ligne.libelle()), ligne.actif());
    }

    @Table(schema = SCHEMA, name = "label")
    record Ligne(@Id Long id, String code, String libelle, boolean actif) {
    }
}
