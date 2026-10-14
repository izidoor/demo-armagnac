package fr.bnia.declarations.adapter.persistence;

import static fr.bnia.declarations.adapter.persistence.Lignes.SCHEMA;

import fr.bnia.declarations.application.GrilleCotisationRepository;
import fr.bnia.declarations.domain.GrilleCotisation;
import fr.bnia.declarations.domain.TauxCotisation;
import java.time.LocalDate;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/** La grille CVO, une ligne par taux (spec 05 §3.5) : réécrite d'un bloc, dans la transaction du cas d'usage. */
@Repository
class JdbcGrilleCotisation implements GrilleCotisationRepository {

    private final JdbcClient jdbcClient;

    JdbcGrilleCotisation(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public GrilleCotisation charger() {
        return GrilleCotisation.de(jdbcClient.sql("select debut, montant, pourcentage_exonere from " + SCHEMA
                        + ".taux_cvo")
                .query((rs, n) -> new TauxCotisation(rs.getBigDecimal("montant"), rs.getBigDecimal("pourcentage_exonere"),
                        rs.getObject("debut", LocalDate.class)))
                .list());
    }

    @Override
    public void enregistrer(GrilleCotisation grille) {
        jdbcClient.sql("delete from " + SCHEMA + ".taux_cvo").update();
        for (TauxCotisation taux : grille.taux()) {
            jdbcClient.sql("insert into " + SCHEMA + ".taux_cvo (debut, montant, pourcentage_exonere)"
                            + " values (:debut, :montant, :pourcentage)")
                    .param("debut", taux.debut())
                    .param("montant", taux.montant())
                    .param("pourcentage", taux.pourcentageExonere())
                    .update();
        }
    }
}
