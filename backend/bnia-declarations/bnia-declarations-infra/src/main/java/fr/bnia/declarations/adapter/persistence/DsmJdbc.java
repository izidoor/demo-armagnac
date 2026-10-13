package fr.bnia.declarations.adapter.persistence;

import static fr.bnia.declarations.adapter.persistence.Lignes.SCHEMA;

import fr.bnia.commun.domain.ConflitUnicite;
import fr.bnia.commun.domain.VolumeAlcoolPur;
import fr.bnia.declarations.application.DsmLecture;
import fr.bnia.declarations.application.DsmLigne;
import fr.bnia.declarations.application.DsmRepository;
import fr.bnia.declarations.application.FiltreDsm;
import fr.bnia.declarations.domain.Auteur;
import fr.bnia.declarations.domain.CalendrierDeclaratif.DsmPassee;
import fr.bnia.declarations.domain.CleConditionnement;
import fr.bnia.declarations.domain.ColonneDsm;
import fr.bnia.declarations.domain.DeclarationStocksMouvements;
import fr.bnia.declarations.domain.EtatDsm;
import fr.bnia.declarations.domain.IdentiteLigne;
import fr.bnia.declarations.domain.LigneDsm;
import fr.bnia.declarations.domain.OrigineLigne;
import fr.bnia.declarations.domain.PeriodeDeclaration;
import fr.bnia.declarations.domain.TypeDeclarant;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/**
 * La DSM et ses lignes forment un agrégat Spring Data JDBC : chargées ensemble, et à chaque mise à jour les
 * lignes sont supprimées puis réinsérées. Leur identifiant est tiré de la séquence avant l'enregistrement
 * et réinséré tel quel, pour rester stable. Volumes en table large, {@code c01} à {@code c25} (A-DEC-6).
 */
@Repository
class DsmJdbc implements DsmRepository, DsmLecture {

    /** La date de dernière modification est celle de la dernière entrée du journal de la DSM (spec 05 §3.2). */
    private static final String RESUME = "id, operateur_id, type, debut, fin, etat, envoyee_le, validee_le,"
            + " (select max(j.date) from " + SCHEMA + ".journal j where j.dsm_id = dsm.id) as modifiee_le";

    private final JdbcAggregateTemplate template;
    private final JdbcClient jdbcClient;

    DsmJdbc(JdbcAggregateTemplate template, JdbcClient jdbcClient) {
        this.template = template;
        this.jdbcClient = jdbcClient;
    }

    @Override
    public DeclarationStocksMouvements inserer(DeclarationStocksMouvements dsm) {
        try {
            return versDomaine(template.insert(versLigne(dsm)));
        } catch (DuplicateKeyException enDouble) {
            throw new ConflitUnicite("RG-DEC-05", "Une DSM est déjà en cours, ou déjà déclarée pour "
                    + dsm.periode().libelle() + " : rechargez l'écran.");
        }
    }

    @Override
    public DeclarationStocksMouvements mettreAJour(DeclarationStocksMouvements dsm) {
        Lignes.mettreAJour(template, versLigne(dsm), "La DSM de " + dsm.periode().libelle());
        return parId(dsm.id()).orElseThrow();
    }

    @Override
    public Optional<DeclarationStocksMouvements> parId(long id) {
        return Optional.ofNullable(template.findById(id, Ligne.class)).map(DsmJdbc::versDomaine);
    }

    @Override
    public void supprimer(long id) {
        template.deleteById(id, Ligne.class);
    }

    @Override
    public List<DsmPassee> dsmsDe(long operateur) {
        return jdbcClient.sql("select id, type, debut, fin, etat from " + SCHEMA + ".dsm where operateur_id = :operateur")
                .param("operateur", operateur)
                .query((rs, n) -> new DsmPassee(rs.getLong("id"), new PeriodeDeclaration(
                        rs.getObject("debut", LocalDate.class), rs.getObject("fin", LocalDate.class)),
                        EtatDsm.valueOf(rs.getString("etat"))))
                .list();
    }

    @Override
    public Optional<DeclarationStocksMouvements> nonValideeDe(long operateur) {
        return jdbcClient.sql("select id from " + SCHEMA + ".dsm where operateur_id = :operateur and etat <> 'VALIDEE'")
                .param("operateur", operateur)
                .query(Long.class)
                .optional()
                .flatMap(this::parId);
    }

    @Override
    public boolean existeValideeCommencantLe(long operateur, LocalDate debut) {
        return jdbcClient.sql("select exists (select 1 from " + SCHEMA + ".dsm where operateur_id = :operateur"
                        + " and debut = :debut and etat = 'VALIDEE')")
                .param("operateur", operateur)
                .param("debut", debut)
                .query(Boolean.class)
                .single();
    }

    @Override
    public List<DsmLigne> deLOperateur(long operateur) {
        return jdbcClient.sql("select " + RESUME + " from " + SCHEMA + ".dsm where operateur_id = :operateur"
                        + " order by debut desc")
                .param("operateur", operateur)
                .query(DsmJdbc::resume)
                .list();
    }

    @Override
    public List<DsmLigne> rechercher(FiltreDsm filtre) {
        StringBuilder sql = new StringBuilder("select " + RESUME + " from " + SCHEMA + ".dsm where true");
        Map<String, Object> parametres = new HashMap<>();
        filtre.operateur().ifPresent(o -> {
            sql.append(" and operateur_id = :operateur");
            parametres.put("operateur", o);
        });
        filtre.type().ifPresent(t -> {
            sql.append(" and type = :type");
            parametres.put("type", t.name());
        });
        if (!filtre.etats().isEmpty()) {
            sql.append(" and etat in (:etats)");
            parametres.put("etats", filtre.etats().stream().map(Enum::name).toList());
        }
        filtre.mois().ifPresent(m -> {
            sql.append(" and debut <= :jour and fin >= :jour");
            parametres.put("jour", m.atDay(1));
        });
        return jdbcClient.sql(sql.toString()).params(parametres).query(DsmJdbc::resume).list();
    }

    private static DsmLigne resume(ResultSet rs, int n) throws SQLException {
        return new DsmLigne(rs.getLong("id"), rs.getLong("operateur_id"), TypeDeclarant.valueOf(rs.getString("type")),
                new PeriodeDeclaration(rs.getObject("debut", LocalDate.class), rs.getObject("fin", LocalDate.class)),
                EtatDsm.valueOf(rs.getString("etat")), instant(rs.getTimestamp("envoyee_le")),
                instant(rs.getTimestamp("validee_le")), instant(rs.getTimestamp("modifiee_le")));
    }

    @Override
    public List<String> denominations(long operateur) {
        return jdbcClient.sql("select min(denomination) from ("
                        + " select l.denomination, l.denomination_normalisee from " + SCHEMA + ".ligne_dsm l"
                        + " join " + SCHEMA + ".dsm d on d.id = l.dsm_id where d.operateur_id = :operateur"
                        + " union all select s.denomination, s.denomination_normalisee from " + SCHEMA
                        + ".ligne_stock_initial s join " + SCHEMA + ".point_de_depart p on p.id = s.point_de_depart_id"
                        + " where p.operateur_id = :operateur) t"
                        + " where denomination is not null group by denomination_normalisee order by 1")
                .param("operateur", operateur)
                .query(String.class)
                .list();
    }

    private static Instant instant(Timestamp t) {
        return t == null ? null : t.toInstant();
    }

    private Ligne versLigne(DeclarationStocksMouvements dsm) {
        List<LigneDsmLigne> lignes = dsm.lignes().stream().map(this::versLigne).toList();
        Auteur validateur = dsm.valideePar();
        return new Ligne(dsm.id(), dsm.operateur(), dsm.type(), dsm.periode().debut(), dsm.periode().fin(), dsm.etat(),
                dsm.dateDia(), dsm.envoyeeLe(), dsm.valideeLe(), validateur == null ? null : validateur.utilisateur(),
                validateur == null ? null : validateur.nom(), lignes, dsm.id() == null ? null : dsm.version());
    }

    private LigneDsmLigne versLigne(LigneDsm ligne) {
        long id = ligne.id() != null ? ligne.id()
                : jdbcClient.sql("select nextval(pg_get_serial_sequence('" + SCHEMA + ".ligne_dsm', 'id'))")
                        .query(Long.class).single();
        IdentiteLigne identite = ligne.identite();
        BigDecimal[] c = new BigDecimal[26];
        for (ColonneDsm colonne : ColonneDsm.values()) {
            if (colonne != ColonneDsm.C26) {
                c[colonne.numero()] = ligne.volume(colonne).litres();
            }
        }
        return new LigneDsmLigne(id, identite.cle(), Lignes.millesime(identite), Lignes.denomination(identite),
                Lignes.denominationNormalisee(identite), identite.compte().toString(), ligne.origine(),
                c[1], c[2], c[3], c[4], c[5], c[6], c[7], c[8], c[9], c[10], c[11], c[12], c[13], c[14], c[15], c[16],
                c[17], c[18], c[19], c[20], c[21], c[22], c[23], c[24], c[25]);
    }

    private static DeclarationStocksMouvements versDomaine(Ligne ligne) {
        Auteur validateur = ligne.valideeParId() == null ? null : new Auteur(ligne.valideeParId(), ligne.nomValideePar());
        return DeclarationStocksMouvements.reconstituer(ligne.id(), ligne.operateurId(), ligne.type(),
                new PeriodeDeclaration(ligne.debut(), ligne.fin()), ligne.etat(), ligne.dateDia(),
                ligne.lignes().stream().map(DsmJdbc::versDomaine).toList(), ligne.envoyeeLe(), ligne.valideeLe(),
                validateur, ligne.version());
    }

    private static LigneDsm versDomaine(LigneDsmLigne l) {
        BigDecimal[] c = {null, l.c01(), l.c02(), l.c03(), l.c04(), l.c05(), l.c06(), l.c07(), l.c08(), l.c09(), l.c10(),
                l.c11(), l.c12(), l.c13(), l.c14(), l.c15(), l.c16(), l.c17(), l.c18(), l.c19(), l.c20(), l.c21(), l.c22(),
                l.c23(), l.c24(), l.c25()};
        Map<ColonneDsm, VolumeAlcoolPur> volumes = new EnumMap<>(ColonneDsm.class);
        for (int numero = 1; numero <= 25; numero++) {
            volumes.put(ColonneDsm.numero(numero), VolumeAlcoolPur.litres(c[numero]));
        }
        return new LigneDsm(l.ligneId(), Lignes.identite(l.cle(), l.millesime(), l.denomination(), l.compte()),
                l.origine(), volumes);
    }

    @Table(schema = SCHEMA, name = "dsm")
    record Ligne(
            @Id Long id,
            Long operateurId,
            TypeDeclarant type,
            LocalDate debut,
            LocalDate fin,
            EtatDsm etat,
            LocalDate dateDia,
            Instant envoyeeLe,
            Instant valideeLe,
            Long valideeParId,
            String nomValideePar,
            @MappedCollection(idColumn = "dsm_id", keyColumn = "rang") List<LigneDsmLigne> lignes,
            @Version Long version) {
    }

    /** {@code ligneId} n'est pas un {@code @Id} : tiré de la séquence avant l'enregistrement, réinséré tel quel. */
    @Table(schema = SCHEMA, name = "ligne_dsm")
    record LigneDsmLigne(
            @Column("id") long ligneId,
            CleConditionnement cle,
            Integer millesime,
            String denomination,
            String denominationNormalisee,
            String compte,
            OrigineLigne origine,
            BigDecimal c01, BigDecimal c02, BigDecimal c03, BigDecimal c04, BigDecimal c05,
            BigDecimal c06, BigDecimal c07, BigDecimal c08, BigDecimal c09, BigDecimal c10,
            BigDecimal c11, BigDecimal c12, BigDecimal c13, BigDecimal c14, BigDecimal c15,
            BigDecimal c16, BigDecimal c17, BigDecimal c18, BigDecimal c19, BigDecimal c20,
            BigDecimal c21, BigDecimal c22, BigDecimal c23, BigDecimal c24, BigDecimal c25) {
    }
}
