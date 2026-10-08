package fr.bnia.referentiels.adapter.persistence;

import fr.bnia.referentiels.application.CentreInteretRepository;
import fr.bnia.referentiels.domain.Code;
import fr.bnia.referentiels.domain.Libelle;
import fr.bnia.referentiels.domain.CentreInteret;
import org.springframework.data.annotation.Id;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
class CentreInteretJdbc extends ValeursSimplesJdbc<CentreInteret, CentreInteretJdbc.Ligne> implements CentreInteretRepository {

    CentreInteretJdbc(JdbcAggregateTemplate template, JdbcClient jdbcClient) {
        super(template, jdbcClient, Ligne.class, "centre_interet");
    }

    @Override
    protected Ligne versLigne(CentreInteret valeur) {
        return new Ligne(valeur.id(), valeur.code().valeur(), valeur.libelle().valeur(), valeur.estActif());
    }

    @Override
    protected CentreInteret versDomaine(Ligne ligne) {
        return CentreInteret.reconstituer(ligne.id(), Code.de(ligne.code()), Libelle.de(ligne.libelle()), ligne.actif());
    }

    @Table(schema = SCHEMA, name = "centre_interet")
    record Ligne(@Id Long id, String code, String libelle, boolean actif) {
    }
}
