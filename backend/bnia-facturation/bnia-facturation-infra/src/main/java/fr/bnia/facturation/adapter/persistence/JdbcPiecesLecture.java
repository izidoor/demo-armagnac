package fr.bnia.facturation.adapter.persistence;

import static fr.bnia.facturation.adapter.persistence.Lignes.SCHEMA;

import fr.bnia.facturation.api.ClientRef;
import fr.bnia.facturation.api.Origine;
import fr.bnia.facturation.application.FiltrePieces;
import fr.bnia.facturation.application.PieceLigne;
import fr.bnia.facturation.application.PiecesLecture;
import fr.bnia.facturation.domain.EtatPiece;
import fr.bnia.facturation.domain.EtatZeendoc;
import fr.bnia.facturation.domain.SensPiece;
import fr.bnia.facturation.domain.SuiviZeendoc;
import java.sql.Types;
import java.time.LocalDate;
import java.util.List;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/** Historique des pièces (RG-FAC-27) : une requête, filtres facultatifs, recherches sans distinction de casse. */
@Repository
class JdbcPiecesLecture implements PiecesLecture {

    private final JdbcClient jdbcClient;

    JdbcPiecesLecture(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public List<PieceLigne> rechercher(FiltrePieces filtre) {
        return jdbcClient.sql("select id, sens, etat, numero, date_emission, acheteur_nom, acheteur_client_identifiant,"
                        + " acheteur_client_code, origine_source, origine_nature, origine_libelle_nature,"
                        + " origine_identifiant, origine_reference, libelle_periode, total_ht, total_tva, total_ttc,"
                        + " creee_le, zeendoc, zeendoc_transmise_le, zeendoc_payee_le from " + SCHEMA + ".facture"
                        + " where (cast(:numero as text) is null or numero like :numero || '%')"
                        + " and (cast(:sens as text) is null or sens = :sens)"
                        + " and (cast(:etat as text) is null or etat = :etat)"
                        + " and (cast(:nature as text) is null or origine_nature = :nature)"
                        + " and (cast(:client as text) is null or acheteur_client_identifiant = :client)"
                        + " and (cast(:acheteur as text) is null or acheteur_nom ilike :acheteur"
                        + " or acheteur_client_code ilike :acheteur)"
                        + " and (cast(:periode as text) is null or libelle_periode ilike :periode)"
                        + " and (cast(:du as date) is null or date_emission >= :du)"
                        + " and (cast(:au as date) is null or date_emission <= :au)"
                        + " order by date_emission desc nulls first, numero desc, creee_le desc, id desc")
                .param("numero", filtre.numero().map(String::strip).orElse(null), Types.VARCHAR)
                .param("sens", filtre.sens().map(Enum::name).orElse(null), Types.VARCHAR)
                .param("etat", filtre.etat().map(Enum::name).orElse(null), Types.VARCHAR)
                .param("nature", filtre.nature().orElse(null), Types.VARCHAR)
                .param("client", filtre.client().orElse(null), Types.VARCHAR)
                .param("acheteur", filtre.acheteur().map(JdbcPiecesLecture::contient).orElse(null), Types.VARCHAR)
                .param("periode", filtre.periode().map(JdbcPiecesLecture::contient).orElse(null), Types.VARCHAR)
                .param("du", filtre.du().orElse(null), Types.DATE)
                .param("au", filtre.au().orElse(null), Types.DATE)
                .query((rs, n) -> new PieceLigne(rs.getLong("id"), SensPiece.valueOf(rs.getString("sens")),
                        EtatPiece.valueOf(rs.getString("etat")), rs.getString("numero"),
                        rs.getObject("date_emission", LocalDate.class), rs.getString("acheteur_nom"),
                        rs.getString("acheteur_client_identifiant") == null ? null
                                : new ClientRef(rs.getString("acheteur_client_identifiant"),
                                        rs.getString("acheteur_client_code")),
                        rs.getString("origine_source") == null ? null
                                : new Origine(rs.getString("origine_source"), rs.getString("origine_nature"),
                                        rs.getString("origine_libelle_nature"), rs.getString("origine_identifiant"),
                                        rs.getString("origine_reference")),
                        rs.getString("libelle_periode"), rs.getBigDecimal("total_ht"), rs.getBigDecimal("total_tva"),
                        rs.getBigDecimal("total_ttc"), rs.getTimestamp("creee_le").toInstant(),
                        rs.getString("zeendoc") == null ? null
                                : new SuiviZeendoc(EtatZeendoc.valueOf(rs.getString("zeendoc")),
                                        rs.getTimestamp("zeendoc_transmise_le").toInstant(),
                                        rs.getObject("zeendoc_payee_le", LocalDate.class))))
                .list();
    }

    /** Recherche « contient », jokers de la saisie échappés. */
    private static String contient(String texte) {
        return "%" + texte.strip().replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_") + "%";
    }
}
