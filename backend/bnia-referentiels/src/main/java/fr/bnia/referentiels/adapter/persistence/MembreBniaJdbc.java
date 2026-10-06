package fr.bnia.referentiels.adapter.persistence;

import fr.bnia.referentiels.application.MembreBniaRepository;
import fr.bnia.referentiels.domain.Code;
import fr.bnia.referentiels.domain.Libelle;
import fr.bnia.referentiels.domain.MembreBnia;
import org.springframework.data.annotation.Id;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
class MembreBniaJdbc extends ValeursSimplesJdbc<MembreBnia, MembreBniaJdbc.Ligne> implements MembreBniaRepository {

    MembreBniaJdbc(JdbcAggregateTemplate template, JdbcClient jdbcClient) {
        super(template, jdbcClient, Ligne.class, "membre_bnia");
    }

    @Override
    protected Ligne versLigne(MembreBnia valeur) {
        return new Ligne(valeur.id(), valeur.code().valeur(), valeur.libelle().valeur(), valeur.estActif());
    }

    @Override
    protected MembreBnia versDomaine(Ligne ligne) {
        return MembreBnia.reconstituer(ligne.id(), Code.de(ligne.code()), Libelle.de(ligne.libelle()), ligne.actif());
    }

    @Table(schema = SCHEMA, name = "membre_bnia")
    record Ligne(@Id Long id, String code, String libelle, boolean actif) {
    }
}
