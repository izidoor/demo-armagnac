package fr.bnia.referentiels.adapter.persistence;

import fr.bnia.referentiels.application.FiltreValeurs;
import fr.bnia.referentiels.application.RegionLigne;
import fr.bnia.referentiels.application.RegionRepository;
import fr.bnia.referentiels.application.RegionsLecture;
import fr.bnia.referentiels.domain.Code;
import fr.bnia.referentiels.domain.Libelle;
import fr.bnia.referentiels.domain.Region;
import java.util.List;
import org.springframework.data.annotation.Id;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
class RegionJdbc extends ValeursJdbc<Region, RegionJdbc.Ligne> implements RegionRepository, RegionsLecture {

    RegionJdbc(JdbcAggregateTemplate template, JdbcClient jdbcClient) {
        super(template, jdbcClient, Ligne.class, "region");
    }

    @Override
    public List<RegionLigne> lister(FiltreValeurs filtre) {
        Sql.Where where = Sql.where(filtre, "code", "libelle");
        return jdbcClient.sql("select id, code, libelle, en_region_delimitee, actif from " + table + where.clause()
                        + " order by libelle, code")
                .params(where.parametres())
                .query(RegionLigne.class)
                .list();
    }

    @Override
    protected Ligne versLigne(Region region) {
        return new Ligne(region.id(), region.code().valeur(), region.libelle().valeur(), region.estEnRegionDelimitee(),
                region.estActif());
    }

    @Override
    protected Region versDomaine(Ligne ligne) {
        return Region.reconstituer(ligne.id(), Code.de(ligne.code()), Libelle.de(ligne.libelle()), ligne.enRegionDelimitee(),
                ligne.actif());
    }

    @Table(schema = SCHEMA, name = "region")
    record Ligne(@Id Long id, String code, String libelle, boolean enRegionDelimitee, boolean actif) {
    }
}
