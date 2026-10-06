package fr.bnia.referentiels.adapter.persistence;

import fr.bnia.referentiels.application.ContenanceLigne;
import fr.bnia.referentiels.application.ContenanceRepository;
import fr.bnia.referentiels.application.ContenancesLecture;
import fr.bnia.referentiels.application.FiltreValeurs;
import fr.bnia.referentiels.domain.Contenance;
import fr.bnia.referentiels.domain.Libelle;
import fr.bnia.referentiels.domain.Volume;
import java.math.BigDecimal;
import java.sql.Types;
import java.util.List;
import java.util.Optional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.annotation.Id;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/** Volume en {@code numeric(10,3)}, unique, toujours à trois décimales comme {@link Volume}. */
@Repository
class ContenanceJdbc implements ContenanceRepository, ContenancesLecture {

    private static final String TABLE = ValeursJdbc.SCHEMA + ".contenance";

    private final JdbcAggregateTemplate template;
    private final JdbcClient jdbcClient;

    ContenanceJdbc(JdbcAggregateTemplate template, JdbcClient jdbcClient) {
        this.template = template;
        this.jdbcClient = jdbcClient;
    }

    @Override
    public Optional<Contenance> parId(long id) {
        return Optional.ofNullable(template.findById(id, Ligne.class)).map(ContenanceJdbc::versDomaine);
    }

    @Override
    public boolean existeVolume(Volume volume, Long sauf) {
        return jdbcClient.sql("select exists (select 1 from " + TABLE
                        + " where volume = :volume and id is distinct from :sauf)")
                .param("volume", volume.litres())
                .param("sauf", sauf, Types.BIGINT)
                .query(Boolean.class)
                .single();
    }

    @Override
    public Contenance inserer(Contenance contenance) {
        return versDomaine(template.insert(versLigne(contenance)));
    }

    @Override
    public void mettreAJour(Contenance contenance) {
        template.update(versLigne(contenance));
    }

    @Override
    public void supprimer(long id) {
        try {
            template.deleteById(id, Ligne.class);
        } catch (DataIntegrityViolationException utilisee) {
            throw ValeursJdbc.valeurUtilisee();
        }
    }

    @Override
    public List<ContenanceLigne> lister(FiltreValeurs filtre) {
        Sql.Where where = Sql.where(filtre, "libelle", "volume::text");
        return jdbcClient.sql("select id, volume, libelle, actif from " + TABLE + where.clause() + " order by volume")
                .params(where.parametres())
                .query(Ligne.class)
                .list()
                .stream()
                .map(ligne -> {
                    Contenance contenance = versDomaine(ligne);
                    return new ContenanceLigne(ligne.id(), contenance.volume().litres(), contenance.libelle(),
                            ligne.libelle(), contenance.estActif());
                })
                .toList();
    }

    private static Ligne versLigne(Contenance contenance) {
        return new Ligne(contenance.id(), contenance.volume().litres(),
                contenance.libelleSaisi().map(Libelle::valeur).orElse(null), contenance.estActif());
    }

    private static Contenance versDomaine(Ligne ligne) {
        return Contenance.reconstituer(ligne.id(), Volume.de(ligne.volume()),
                ligne.libelle() == null ? null : Libelle.de(ligne.libelle()), ligne.actif());
    }

    @Table(schema = ValeursJdbc.SCHEMA, name = "contenance")
    record Ligne(@Id Long id, BigDecimal volume, String libelle, boolean actif) {
    }
}
