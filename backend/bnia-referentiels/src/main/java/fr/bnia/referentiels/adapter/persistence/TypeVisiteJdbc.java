package fr.bnia.referentiels.adapter.persistence;

import fr.bnia.referentiels.application.TypeVisiteRepository;
import fr.bnia.referentiels.domain.Code;
import fr.bnia.referentiels.domain.Libelle;
import fr.bnia.referentiels.domain.TypeVisite;
import org.springframework.data.annotation.Id;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
class TypeVisiteJdbc extends ValeursSimplesJdbc<TypeVisite, TypeVisiteJdbc.Ligne> implements TypeVisiteRepository {

    TypeVisiteJdbc(JdbcAggregateTemplate template, JdbcClient jdbcClient) {
        super(template, jdbcClient, Ligne.class, "type_visite");
    }

    @Override
    protected Ligne versLigne(TypeVisite valeur) {
        return new Ligne(valeur.id(), valeur.code().valeur(), valeur.libelle().valeur(), valeur.estActif());
    }

    @Override
    protected TypeVisite versDomaine(Ligne ligne) {
        return TypeVisite.reconstituer(ligne.id(), Code.de(ligne.code()), Libelle.de(ligne.libelle()), ligne.actif());
    }

    @Table(schema = SCHEMA, name = "type_visite")
    record Ligne(@Id Long id, String code, String libelle, boolean actif) {
    }
}
