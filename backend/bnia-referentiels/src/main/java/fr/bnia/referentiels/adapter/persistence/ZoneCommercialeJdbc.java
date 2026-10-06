package fr.bnia.referentiels.adapter.persistence;

import fr.bnia.referentiels.application.ZoneCommercialeRepository;
import fr.bnia.referentiels.domain.Code;
import fr.bnia.referentiels.domain.Libelle;
import fr.bnia.referentiels.domain.ZoneCommerciale;
import org.springframework.data.annotation.Id;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
class ZoneCommercialeJdbc extends ValeursSimplesJdbc<ZoneCommerciale, ZoneCommercialeJdbc.Ligne>
        implements ZoneCommercialeRepository {

    ZoneCommercialeJdbc(JdbcAggregateTemplate template, JdbcClient jdbcClient) {
        super(template, jdbcClient, Ligne.class, "zone_commerciale");
    }

    @Override
    protected Ligne versLigne(ZoneCommerciale zone) {
        return new Ligne(zone.code().valeur(), zone.libelle().valeur(), zone.estActif());
    }

    @Override
    protected ZoneCommerciale versDomaine(Ligne ligne) {
        return ZoneCommerciale.reconstituer(Code.de(ligne.code()), Libelle.de(ligne.libelle()), ligne.actif());
    }

    @Table(schema = SCHEMA, name = "zone_commerciale")
    record Ligne(@Id String code, String libelle, boolean actif) {
    }
}
