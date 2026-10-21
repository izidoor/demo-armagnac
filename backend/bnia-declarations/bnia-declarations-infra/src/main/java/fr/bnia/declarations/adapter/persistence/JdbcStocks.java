package fr.bnia.declarations.adapter.persistence;

import static fr.bnia.declarations.adapter.persistence.Lignes.SCHEMA;

import fr.bnia.commun.domain.VolumeAlcoolPur;
import fr.bnia.declarations.application.StocksLecture;
import fr.bnia.declarations.domain.ColonneDsm;
import fr.bnia.declarations.domain.PeriodeDeclaration;
import fr.bnia.declarations.domain.SensColonne;
import fr.bnia.declarations.domain.TypeDeclarant;
import fr.bnia.declarations.domain.partage.CleConditionnement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/**
 * Stocks déclarés (RG-DEC-42) : la dernière DSM validée de chaque opérateur, en une requête.
 */
@Repository
class JdbcStocks implements StocksLecture {

    /**
     * RG-DEC-17 : le stock de fin d'une ligne, colonne 1 plus les entrées moins les sorties ; l'expression SQL
     * est tirée du sens de chaque colonne ({@link ColonneDsm}), jamais réécrite à la main.
     */
    private static final String STOCK_FIN = Arrays.stream(ColonneDsm.values())
            .filter(c -> c.sens() != SensColonne.STOCK_FIN)
            .map(c -> (c.sens() == SensColonne.SORTIE ? "- " : "+ ") + "l.c%02d".formatted(c.numero()))
            .collect(Collectors.joining(" ", "(0 ", ")"));

    private final JdbcClient jdbcClient;

    JdbcStocks(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public List<LigneStock> stocksDeclares(Optional<Long> operateur, Optional<TypeDeclarant> type) {
        Map<String, Object> parametres = new HashMap<>();
        StringBuilder derniere = new StringBuilder("select distinct on (operateur_id) id, operateur_id, type, debut,"
                + " fin, validee_le from " + SCHEMA + ".dsm where etat = 'VALIDEE'");
        operateur.ifPresent(o -> {
            derniere.append(" and operateur_id = :operateur");
            parametres.put("operateur", o);
        });
        derniere.append(" order by operateur_id, fin desc");
        StringBuilder sql = new StringBuilder("select d.id, d.operateur_id, d.type, d.debut, d.fin, d.validee_le,"
                + " l.cle, l.millesime, l.denomination, l.compte, " + STOCK_FIN + " as stock"
                + " from (" + derniere + ") d join " + SCHEMA + ".ligne_dsm l on l.dsm_id = d.id"
                + " where " + STOCK_FIN + " <> 0");
        type.ifPresent(t -> {
            sql.append(" and d.type = :type");
            parametres.put("type", t.name());
        });
        sql.append(" order by d.operateur_id, l.rang");
        return jdbcClient.sql(sql.toString()).params(parametres).query((rs, n) -> ligne(rs)).list();
    }

    private static LigneStock ligne(ResultSet rs) throws SQLException {
        Timestamp valideeLe = rs.getTimestamp("validee_le");
        return new LigneStock(rs.getLong("operateur_id"), TypeDeclarant.valueOf(rs.getString("type")),
                rs.getLong("id"), new PeriodeDeclaration(rs.getObject("debut", LocalDate.class),
                        rs.getObject("fin", LocalDate.class)),
                valideeLe == null ? null : valideeLe.toInstant(),
                Lignes.identite(CleConditionnement.valueOf(rs.getString("cle")), (Integer) rs.getObject("millesime"),
                        rs.getString("denomination"), rs.getString("compte")),
                VolumeAlcoolPur.litres(rs.getBigDecimal("stock")));
    }
}
