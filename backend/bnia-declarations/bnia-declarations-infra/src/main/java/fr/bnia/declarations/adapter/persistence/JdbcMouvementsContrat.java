package fr.bnia.declarations.adapter.persistence;

import static fr.bnia.declarations.adapter.persistence.Lignes.SCHEMA;

import fr.bnia.commun.domain.VolumeAlcoolPur;
import fr.bnia.declarations.application.MouvementsContrat;
import fr.bnia.declarations.domain.ColonneDsm;
import fr.bnia.declarations.domain.MouvementContrat;
import fr.bnia.declarations.domain.PeriodeDeclaration;
import fr.bnia.declarations.domain.SensMouvementContrat;
import fr.bnia.declarations.domain.partage.CleConditionnement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/**
 * Mouvements de contrat en SQL direct (spec 06 §3.7, A-CA-3) : copies des retiraisons validées, contrat et autre
 * partie sans clé étrangère (A-CA-4) ; la DSM d'inscription revient à vide si son brouillon est supprimé.
 */
@Repository
class JdbcMouvementsContrat implements MouvementsContrat {

    private static final String COLONNES = "id, operateur_id, sens, date_enlevement, cle, millesime, denomination,"
            + " compte, volume_ap, colonne, contrat_id, numero_contrat, retiraison, autre_partie_id, dsm_id";

    private final JdbcClient jdbcClient;

    JdbcMouvementsContrat(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public List<MouvementContrat> inserer(List<MouvementContrat> mouvements) {
        return mouvements.stream().map(m -> {
            long id = jdbcClient.sql("insert into " + SCHEMA + ".mouvement_contrat (operateur_id, sens,"
                            + " date_enlevement, cle, millesime, denomination, compte, volume_ap, colonne, contrat_id,"
                            + " numero_contrat, retiraison, autre_partie_id, dsm_id) values (:operateur, :sens, :date,"
                            + " :cle, :millesime, :denomination, :compte, :volume, :colonne, :contrat, :numero,"
                            + " :retiraison, :autre, :dsm) returning id")
                    .param("operateur", m.operateur())
                    .param("sens", m.sens().name())
                    .param("date", m.dateEnlevement())
                    .param("cle", m.identite().cle().name())
                    .param("millesime", Lignes.millesime(m.identite()), Types.INTEGER)
                    .param("denomination", Lignes.denomination(m.identite()), Types.VARCHAR)
                    .param("compte", m.identite().compte().toString())
                    .param("volume", m.volumeAp().litres())
                    .param("colonne", m.colonne().numero())
                    .param("contrat", m.contrat())
                    .param("numero", m.numeroContrat())
                    .param("retiraison", m.retiraison())
                    .param("autre", m.autrePartie())
                    .param("dsm", m.dsm().orElse(null), Types.BIGINT)
                    .query(Long.class)
                    .single();
            return new MouvementContrat(id, m.operateur(), m.sens(), m.dateEnlevement(), m.identite(), m.volumeAp(),
                    m.colonne(), m.contrat(), m.numeroContrat(), m.retiraison(), m.autrePartie(), m.dsm());
        }).toList();
    }

    @Override
    public void inscrire(long mouvement, long dsm) {
        jdbcClient.sql("update " + SCHEMA + ".mouvement_contrat set dsm_id = :dsm where id = :id")
                .param("id", mouvement)
                .param("dsm", dsm)
                .update();
    }

    @Override
    public List<MouvementContrat> deLOperateur(long operateur) {
        return jdbcClient.sql("select " + COLONNES + " from " + SCHEMA + ".mouvement_contrat"
                        + " where operateur_id = :operateur order by date_enlevement, id")
                .param("operateur", operateur)
                .query((rs, n) -> mouvement(rs))
                .list();
    }

    @Override
    public List<MouvementContrat> enAttente(long operateur, PeriodeDeclaration periode) {
        return jdbcClient.sql("select " + COLONNES + " from " + SCHEMA + ".mouvement_contrat"
                        + " where operateur_id = :operateur and dsm_id is null"
                        + " and date_enlevement between :debut and :fin order by date_enlevement, id")
                .param("operateur", operateur)
                .param("debut", periode.debut())
                .param("fin", periode.fin())
                .query((rs, n) -> mouvement(rs))
                .list();
    }

    @Override
    public List<MouvementContrat> inscritsDans(long dsm) {
        return jdbcClient.sql("select " + COLONNES + " from " + SCHEMA + ".mouvement_contrat where dsm_id = :dsm"
                        + " order by numero_contrat, retiraison, id")
                .param("dsm", dsm)
                .query((rs, n) -> mouvement(rs))
                .list();
    }

    private static MouvementContrat mouvement(ResultSet rs) throws SQLException {
        Integer millesime = (Integer) rs.getObject("millesime");
        Long dsm = (Long) rs.getObject("dsm_id");
        return new MouvementContrat(rs.getLong("id"), rs.getLong("operateur_id"),
                SensMouvementContrat.valueOf(rs.getString("sens")), rs.getObject("date_enlevement", LocalDate.class),
                Lignes.identite(CleConditionnement.valueOf(rs.getString("cle")), millesime,
                        rs.getString("denomination"), rs.getString("compte")),
                VolumeAlcoolPur.litres(rs.getBigDecimal("volume_ap")), ColonneDsm.numero(rs.getInt("colonne")),
                rs.getLong("contrat_id"), rs.getString("numero_contrat"), rs.getInt("retiraison"),
                rs.getLong("autre_partie_id"), Optional.ofNullable(dsm));
    }
}
