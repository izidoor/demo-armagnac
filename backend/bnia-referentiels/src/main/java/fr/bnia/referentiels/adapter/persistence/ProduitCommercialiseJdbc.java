package fr.bnia.referentiels.adapter.persistence;

import fr.bnia.referentiels.application.ProduitCommercialiseRepository;
import fr.bnia.referentiels.domain.Code;
import fr.bnia.referentiels.domain.Libelle;
import fr.bnia.referentiels.domain.ProduitCommercialise;
import org.springframework.data.annotation.Id;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
class ProduitCommercialiseJdbc extends ValeursSimplesJdbc<ProduitCommercialise, ProduitCommercialiseJdbc.Ligne> implements ProduitCommercialiseRepository {

    ProduitCommercialiseJdbc(JdbcAggregateTemplate template, JdbcClient jdbcClient) {
        super(template, jdbcClient, Ligne.class, "produit_commercialise");
    }

    @Override
    protected Ligne versLigne(ProduitCommercialise valeur) {
        return new Ligne(valeur.id(), valeur.code().valeur(), valeur.libelle().valeur(), valeur.estActif());
    }

    @Override
    protected ProduitCommercialise versDomaine(Ligne ligne) {
        return ProduitCommercialise.reconstituer(ligne.id(), Code.de(ligne.code()), Libelle.de(ligne.libelle()), ligne.actif());
    }

    @Table(schema = SCHEMA, name = "produit_commercialise")
    record Ligne(@Id Long id, String code, String libelle, boolean actif) {
    }
}
