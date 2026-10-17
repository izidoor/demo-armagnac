package fr.bnia.facturation.adapter.persistence;

import static fr.bnia.facturation.adapter.persistence.Lignes.SCHEMA;

import fr.bnia.facturation.api.ArticleRef;
import fr.bnia.facturation.api.CategorieTva;
import fr.bnia.facturation.api.ClientRef;
import fr.bnia.facturation.api.LigneVendue;
import fr.bnia.facturation.api.NatureArticle;
import fr.bnia.facturation.api.Origine;
import fr.bnia.facturation.application.VentesLecture;
import java.sql.Types;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/**
 * Les ventes (RG-FAC-30, 31) : les lignes article des pièces émises d'une période, à leur date d'émission ; celles
 * d'un avoir changent de signe (quantité et HT).
 */
@Repository
class JdbcVentesLecture implements VentesLecture {

    private final JdbcClient jdbcClient;

    JdbcVentesLecture(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public List<LigneVendue> ventes(LocalDate du, LocalDate au, Optional<String> nature) {
        return jdbcClient.sql("select f.id, f.sens, f.numero, f.date_emission, f.acheteur_nom,"
                        + " f.acheteur_client_identifiant, f.acheteur_client_code, f.origine_source, f.origine_nature,"
                        + " f.origine_libelle_nature, f.origine_identifiant, f.origine_reference,"
                        + " l.article_source, l.article_code, l.texte, l.unite, l.nature, l.code_comptable,"
                        + " l.tva_code, l.tva_taux, l.tva_motif, l.prix_unitaire,"
                        + " l.quantite * s.signe as quantite, l.montant_ht * s.signe as montant_ht"
                        + " from " + SCHEMA + ".facture f"
                        + " join " + SCHEMA + ".ligne_facture l on l.facture_id = f.id"
                        + " cross join lateral (select case f.sens when 'AVOIR' then -1 else 1 end as signe) s"
                        + " where f.etat = 'EMISE' and l.sorte = 'ARTICLE'"
                        + " and f.date_emission between :du and :au"
                        + " and (cast(:nature as text) is null or f.origine_nature = :nature)"
                        + " order by f.date_emission, f.numero, l.position")
                .param("du", du, Types.DATE)
                .param("au", au, Types.DATE)
                .param("nature", nature.orElse(null), Types.VARCHAR)
                .query((rs, n) -> new LigneVendue(rs.getLong("id"), rs.getString("numero"),
                        "AVOIR".equals(rs.getString("sens")), rs.getObject("date_emission", LocalDate.class),
                        rs.getString("acheteur_nom"),
                        rs.getString("acheteur_client_identifiant") == null ? null
                                : new ClientRef(rs.getString("acheteur_client_identifiant"),
                                        rs.getString("acheteur_client_code")),
                        rs.getString("origine_source") == null ? null
                                : new Origine(rs.getString("origine_source"), rs.getString("origine_nature"),
                                        rs.getString("origine_libelle_nature"), rs.getString("origine_identifiant"),
                                        rs.getString("origine_reference")),
                        new ArticleRef(rs.getString("article_source"), rs.getString("article_code")),
                        rs.getString("texte"), rs.getString("unite"), NatureArticle.valueOf(rs.getString("nature")),
                        rs.getString("code_comptable"),
                        new CategorieTva(CategorieTva.Code.valueOf(rs.getString("tva_code")),
                                rs.getBigDecimal("tva_taux"), rs.getString("tva_motif")),
                        rs.getBigDecimal("quantite"), rs.getBigDecimal("prix_unitaire"), rs.getBigDecimal("montant_ht")))
                .list();
    }
}
