package fr.bnia.referentiels.adapter.persistence;

import fr.bnia.referentiels.application.TitreRepository;
import fr.bnia.referentiels.domain.Code;
import fr.bnia.referentiels.domain.Libelle;
import fr.bnia.referentiels.domain.Titre;
import org.springframework.data.annotation.Id;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
class TitreJdbc extends ValeursSimplesJdbc<Titre, TitreJdbc.Ligne> implements TitreRepository {

    TitreJdbc(JdbcAggregateTemplate template, JdbcClient jdbcClient) {
        super(template, jdbcClient, Ligne.class, "titre");
    }

    @Override
    protected Ligne versLigne(Titre valeur) {
        return new Ligne(valeur.id(), valeur.code().valeur(), valeur.libelle().valeur(), valeur.estActif());
    }

    @Override
    protected Titre versDomaine(Ligne ligne) {
        return Titre.reconstituer(ligne.id(), Code.de(ligne.code()), Libelle.de(ligne.libelle()), ligne.actif());
    }

    @Table(schema = SCHEMA, name = "titre")
    record Ligne(@Id Long id, String code, String libelle, boolean actif) {
    }
}
