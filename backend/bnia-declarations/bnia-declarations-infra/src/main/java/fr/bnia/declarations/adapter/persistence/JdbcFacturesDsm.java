package fr.bnia.declarations.adapter.persistence;

import static fr.bnia.declarations.adapter.persistence.Lignes.SCHEMA;

import fr.bnia.declarations.application.FactureDsm;
import fr.bnia.declarations.application.FacturesDsm;
import java.time.LocalDate;
import java.util.List;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/**
 * Projection des documents de facturation des DSM (A-DEC-4) : brouillons demandés, factures et avoirs reçus ; un
 * document reçu deux fois n'est gardé qu'une fois.
 */
@Repository
class JdbcFacturesDsm implements FacturesDsm {

    private final JdbcClient jdbcClient;

    JdbcFacturesDsm(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public boolean inserer(FactureDsm facture) {
        return jdbcClient.sql("insert into " + SCHEMA + ".facture_dsm (dsm_id, piece, type, numero, date, ht, tva, ttc)"
                        + " values (:dsm, :piece, :type, :numero, :date, :ht, :tva, :ttc) on conflict do nothing")
                .param("dsm", facture.dsm())
                .param("piece", facture.piece())
                .param("type", facture.type().name())
                .param("numero", facture.numero())
                .param("date", facture.date())
                .param("ht", facture.ht())
                .param("tva", facture.tva())
                .param("ttc", facture.ttc())
                .update() == 1;
    }

    @Override
    public void retirerBrouillon(long piece) {
        jdbcClient.sql("delete from " + SCHEMA + ".facture_dsm where piece = :piece and type = 'BROUILLON'")
                .param("piece", piece)
                .update();
    }

    @Override
    public List<FactureDsm> deLaDsm(long dsm) {
        return jdbcClient.sql("select dsm_id, piece, type, numero, date, ht, tva, ttc from " + SCHEMA + ".facture_dsm"
                        + " where dsm_id = :dsm order by date nulls first, type desc, numero")
                .param("dsm", dsm)
                .query((rs, n) -> new FactureDsm(rs.getLong("dsm_id"), rs.getLong("piece"),
                        FactureDsm.Type.valueOf(rs.getString("type")), rs.getString("numero"),
                        rs.getObject("date", LocalDate.class), rs.getBigDecimal("ht"), rs.getBigDecimal("tva"),
                        rs.getBigDecimal("ttc")))
                .list();
    }
}
