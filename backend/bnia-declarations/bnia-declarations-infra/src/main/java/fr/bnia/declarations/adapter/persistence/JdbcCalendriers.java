package fr.bnia.declarations.adapter.persistence;

import static fr.bnia.declarations.adapter.persistence.Lignes.SCHEMA;

import fr.bnia.declarations.application.CalendriersLecture;
import fr.bnia.declarations.domain.CalendrierDeclaratif.DsmPassee;
import fr.bnia.declarations.domain.PeriodeDeclaration;
import fr.bnia.declarations.domain.partage.EtatDsm;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/**
 * Repères du calendrier de tous les opérateurs, en une requête (RG-DEC-40, 41) : le point de départ en vigueur
 * de chacun, sa DSM non validée et sa dernière DSM validée depuis ce point de départ.
 */
@Repository
class JdbcCalendriers implements CalendriersLecture {

    private final JdbcClient jdbcClient;

    JdbcCalendriers(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public Map<Long, Reperes> reperes() {
        Map<Long, LocalDate> points = new HashMap<>();
        Map<Long, List<DsmPassee>> dsms = new HashMap<>();
        jdbcClient.sql("select p.operateur_id, p.debut as point, d.id, d.debut, d.fin, d.etat"
                        + " from (select distinct on (operateur_id) operateur_id, debut from " + SCHEMA
                        + ".point_de_depart order by operateur_id, debut desc) p"
                        + " left join lateral ("
                        + " (select id, debut, fin, etat from " + SCHEMA + ".dsm"
                        + " where operateur_id = p.operateur_id and etat <> 'VALIDEE')"
                        + " union all"
                        + " (select id, debut, fin, etat from " + SCHEMA + ".dsm"
                        + " where operateur_id = p.operateur_id and etat = 'VALIDEE' and debut >= p.debut"
                        + " order by debut desc limit 1)) d on true")
                .query(rs -> {
                    long operateur = rs.getLong("operateur_id");
                    points.put(operateur, rs.getObject("point", LocalDate.class));
                    List<DsmPassee> liste = dsms.computeIfAbsent(operateur, o -> new ArrayList<>());
                    long id = rs.getLong("id");
                    if (!rs.wasNull()) {
                        liste.add(new DsmPassee(id, new PeriodeDeclaration(rs.getObject("debut", LocalDate.class),
                                rs.getObject("fin", LocalDate.class)), EtatDsm.valueOf(rs.getString("etat"))));
                    }
                });
        Map<Long, Reperes> reperes = new HashMap<>();
        points.forEach((operateur, point) -> reperes.put(operateur, new Reperes(point, dsms.get(operateur))));
        return reperes;
    }
}
