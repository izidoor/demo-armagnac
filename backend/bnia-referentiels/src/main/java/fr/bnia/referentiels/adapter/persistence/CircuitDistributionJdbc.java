package fr.bnia.referentiels.adapter.persistence;

import fr.bnia.referentiels.application.CircuitDistributionRepository;
import fr.bnia.referentiels.domain.Code;
import fr.bnia.referentiels.domain.Libelle;
import fr.bnia.referentiels.domain.CircuitDistribution;
import org.springframework.data.annotation.Id;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
class CircuitDistributionJdbc extends ValeursSimplesJdbc<CircuitDistribution, CircuitDistributionJdbc.Ligne> implements CircuitDistributionRepository {

    CircuitDistributionJdbc(JdbcAggregateTemplate template, JdbcClient jdbcClient) {
        super(template, jdbcClient, Ligne.class, "circuit_distribution");
    }

    @Override
    protected Ligne versLigne(CircuitDistribution valeur) {
        return new Ligne(valeur.code().valeur(), valeur.libelle().valeur(), valeur.estActif());
    }

    @Override
    protected CircuitDistribution versDomaine(Ligne ligne) {
        return CircuitDistribution.reconstituer(Code.de(ligne.code()), Libelle.de(ligne.libelle()), ligne.actif());
    }

    @Table(schema = SCHEMA, name = "circuit_distribution")
    record Ligne(@Id String code, String libelle, boolean actif) {
    }
}
