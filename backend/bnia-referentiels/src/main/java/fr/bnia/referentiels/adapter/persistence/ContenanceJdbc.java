package fr.bnia.referentiels.adapter.persistence;

import fr.bnia.referentiels.application.ContenanceLigne;
import fr.bnia.referentiels.application.ContenanceRepository;
import fr.bnia.referentiels.application.ContenancesLecture;
import fr.bnia.referentiels.application.FiltreValeurs;
import fr.bnia.referentiels.domain.Contenance;
import fr.bnia.referentiels.domain.Libelle;
import fr.bnia.referentiels.domain.Volume;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.annotation.Id;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/**
 * Le volume est l'identifiant de la ligne ({@code numeric(10,3)}), toujours à trois décimales comme
 * {@link Volume}.
 */
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
    public Optional<Contenance> parVolume(Volume volume) {
        return Optional.ofNullable(template.findById(volume.litres(), Ligne.class)).map(ContenanceJdbc::versDomaine);
    }

    @Override
    public boolean existeVolume(Volume volume) {
        return template.existsById(volume.litres(), Ligne.class);
    }

    @Override
    public void inserer(Contenance contenance) {
        template.insert(versLigne(contenance));
    }

    @Override
    public void mettreAJour(Contenance contenance) {
        template.update(versLigne(contenance));
    }

    @Override
    public List<ContenanceLigne> lister(FiltreValeurs filtre) {
        Sql.Where where = Sql.where(filtre, "libelle", "volume::text");
        return jdbcClient.sql("select volume, libelle, actif from " + TABLE + where.clause() + " order by volume")
                .params(where.parametres())
                .query(Ligne.class)
                .list()
                .stream()
                .map(ligne -> {
                    Contenance contenance = versDomaine(ligne);
                    return new ContenanceLigne(contenance.volume().litres(), contenance.libelle(), ligne.libelle(),
                            contenance.estActif());
                })
                .toList();
    }

    private static Ligne versLigne(Contenance contenance) {
        return new Ligne(contenance.volume().litres(), contenance.libelleSaisi().map(Libelle::valeur).orElse(null),
                contenance.estActif());
    }

    private static Contenance versDomaine(Ligne ligne) {
        return Contenance.reconstituer(Volume.de(ligne.volume()),
                ligne.libelle() == null ? null : Libelle.de(ligne.libelle()), ligne.actif());
    }

    @Table(schema = ValeursJdbc.SCHEMA, name = "contenance")
    record Ligne(@Id BigDecimal volume, String libelle, boolean actif) {
    }
}
