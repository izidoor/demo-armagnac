package fr.bnia.declarations.adapter.persistence;

import static fr.bnia.declarations.adapter.persistence.Lignes.SCHEMA;

import fr.bnia.declarations.application.FactureDsm;
import fr.bnia.declarations.application.FacturesDsm;
import java.time.LocalDate;
import java.util.List;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/** Projection des factures et avoirs des DSM (A-DEC-4) ; un document reçu deux fois n'est gardé qu'une fois. */
@Repository
class JdbcFacturesDsm implements FacturesDsm {

    private final JdbcClient jdbcClient;

    JdbcFacturesDsm(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public void inserer(FactureDsm facture) {
        jdbcClient.sql("insert into " + SCHEMA + ".facture_dsm (dsm_id, type, numero, date, ht, tva, ttc)"
                        + " values (:dsm, :type, :numero, :date, :ht, :tva, :ttc) on conflict (type, numero) do nothing")
                .param("dsm", facture.dsm())
                .param("type", facture.type().name())
                .param("numero", facture.numero())
                .param("date", facture.date())
                .param("ht", facture.ht())
                .param("tva", facture.tva())
                .param("ttc", facture.ttc())
                .update();
    }

    @Override
    public List<FactureDsm> deLaDsm(long dsm) {
        return jdbcClient.sql("select dsm_id, type, numero, date, ht, tva, ttc from " + SCHEMA + ".facture_dsm"
                        + " where dsm_id = :dsm order by date, type desc, numero")
                .param("dsm", dsm)
                .query((rs, n) -> new FactureDsm(rs.getLong("dsm_id"), FactureDsm.Type.valueOf(rs.getString("type")),
                        rs.getString("numero"), rs.getObject("date", LocalDate.class), rs.getBigDecimal("ht"),
                        rs.getBigDecimal("tva"), rs.getBigDecimal("ttc")))
                .list();
    }
}
