package fr.bnia.declarations.adapter.persistence;

import static fr.bnia.declarations.adapter.persistence.Lignes.SCHEMA;

import fr.bnia.commun.domain.ConflitUnicite;
import fr.bnia.commun.domain.VolumeAlcoolPur;
import fr.bnia.declarations.application.PointDeDepartRepository;
import fr.bnia.declarations.domain.Auteur;
import fr.bnia.declarations.domain.IdentiteLigne;
import fr.bnia.declarations.domain.LigneStockInitial;
import fr.bnia.declarations.domain.PointDeDepartDeclaratif;
import fr.bnia.declarations.domain.partage.CleConditionnement;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/** Le point de départ et son stock initial forment un agrégat Spring Data JDBC. */
@Repository
class PointDeDepartJdbc implements PointDeDepartRepository {

    private final JdbcAggregateTemplate template;
    private final JdbcClient jdbcClient;

    PointDeDepartJdbc(JdbcAggregateTemplate template, JdbcClient jdbcClient) {
        this.template = template;
        this.jdbcClient = jdbcClient;
    }

    @Override
    public PointDeDepartDeclaratif inserer(PointDeDepartDeclaratif point) {
        try {
            return versDomaine(template.insert(versLigne(point)));
        } catch (DuplicateKeyException enDouble) {
            throw new ConflitUnicite(null, "Un point de départ commence déjà à cette date : rechargez l'écran.");
        }
    }

    @Override
    public PointDeDepartDeclaratif mettreAJour(PointDeDepartDeclaratif point) {
        try {
            return versDomaine(Lignes.mettreAJour(template, versLigne(point), "Le point de départ"));
        } catch (DuplicateKeyException enDouble) {
            throw new ConflitUnicite(null, "Un point de départ commence déjà à cette date.");
        }
    }

    @Override
    public Optional<PointDeDepartDeclaratif> parId(long id) {
        return Optional.ofNullable(template.findById(id, Ligne.class)).map(PointDeDepartJdbc::versDomaine);
    }

    @Override
    public Optional<PointDeDepartDeclaratif> enVigueur(long operateur) {
        return jdbcClient.sql("select id from " + SCHEMA + ".point_de_depart where operateur_id = :operateur"
                        + " order by debut desc limit 1")
                .param("operateur", operateur)
                .query(Long.class)
                .optional()
                .flatMap(this::parId);
    }

    @Override
    public List<PointDeDepartDeclaratif> deLOperateur(long operateur) {
        return jdbcClient.sql("select id from " + SCHEMA + ".point_de_depart where operateur_id = :operateur"
                        + " order by debut desc")
                .param("operateur", operateur)
                .query(Long.class)
                .list()
                .stream()
                .map(id -> parId(id).orElseThrow())
                .toList();
    }

    private static Ligne versLigne(PointDeDepartDeclaratif point) {
        List<StockLigne> lignes = point.lignes().stream().map(l -> {
            IdentiteLigne identite = l.identite();
            return new StockLigne(identite.cle(), Lignes.millesime(identite), Lignes.denomination(identite),
                    Lignes.denominationNormalisee(identite), identite.compte().toString(), l.volume().litres());
        }).toList();
        return new Ligne(point.id(), point.operateur(), point.debut(), point.fixePar().utilisateur(),
                point.fixePar().nom(), point.fixeLe(), lignes, point.id() == null ? null : point.version());
    }

    private static PointDeDepartDeclaratif versDomaine(Ligne ligne) {
        return PointDeDepartDeclaratif.reconstituer(ligne.id(), ligne.operateurId(), ligne.debut(),
                ligne.lignes().stream().map(l -> new LigneStockInitial(
                        Lignes.identite(l.cle(), l.millesime(), l.denomination(), l.compte()),
                        VolumeAlcoolPur.litres(l.volume()))).toList(),
                new Auteur(ligne.fixeParId(), ligne.nomFixePar()), ligne.fixeLe(), ligne.version());
    }

    @Table(schema = SCHEMA, name = "point_de_depart")
    record Ligne(
            @Id Long id,
            Long operateurId,
            LocalDate debut,
            Long fixeParId,
            String nomFixePar,
            Instant fixeLe,
            @MappedCollection(idColumn = "point_de_depart_id", keyColumn = "rang") List<StockLigne> lignes,
            @Version Long version) {
    }

    @Table(schema = SCHEMA, name = "ligne_stock_initial")
    record StockLigne(
            CleConditionnement cle,
            Integer millesime,
            String denomination,
            String denominationNormalisee,
            String compte,
            BigDecimal volume) {
    }
}
