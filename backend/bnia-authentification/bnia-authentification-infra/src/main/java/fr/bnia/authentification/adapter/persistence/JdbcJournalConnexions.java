package fr.bnia.authentification.adapter.persistence;

import fr.bnia.authentification.application.FiltreConnexions;
import fr.bnia.authentification.application.JournalConnexions;
import fr.bnia.authentification.application.LigneConnexion;
import fr.bnia.authentification.domain.EvenementConnexion;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.List;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/** Journal des connexions en SQL direct, en ajout seul (RG-AUT-12). */
@Repository
class JdbcJournalConnexions implements JournalConnexions {

    /** Au-delà, l'écran filtre davantage. */
    static final int LIGNES_MAXIMUM = 500;

    private static final String TABLE = CompteJdbc.SCHEMA + ".journal_connexion";

    private final JdbcClient jdbcClient;

    JdbcJournalConnexions(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public void ajouter(LigneConnexion ligne) {
        jdbcClient.sql("insert into " + TABLE + " (date, identifiant_saisi, utilisateur_id, evenement, detail, adresse_ip,"
                        + " navigateur) values (:date, :identifiant, :utilisateur, :evenement, :detail, :ip, :navigateur)")
                .param("date", Timestamp.from(ligne.date()))
                .param("identifiant", ligne.identifiantSaisi())
                .param("utilisateur", ligne.utilisateur(), Types.BIGINT)
                .param("evenement", ligne.evenement().name())
                .param("detail", ligne.detail(), Types.VARCHAR)
                .param("ip", ligne.adresseIp(), Types.VARCHAR)
                .param("navigateur", ligne.navigateur(), Types.VARCHAR)
                .update();
    }

    @Override
    public List<LigneConnexion> lister(FiltreConnexions filtre) {
        return jdbcClient.sql("select date, identifiant_saisi, utilisateur_id, evenement, detail, adresse_ip, navigateur"
                        + " from " + TABLE
                        + " where (cast(:utilisateur as bigint) is null or utilisateur_id = :utilisateur)"
                        + " and (cast(:evenement as text) is null or evenement = :evenement)"
                        + " and (cast(:du as timestamptz) is null or date >= :du)"
                        + " and (cast(:au as timestamptz) is null or date < :au)"
                        + " order by date desc, id desc limit " + LIGNES_MAXIMUM)
                .param("utilisateur", filtre.utilisateur(), Types.BIGINT)
                .param("evenement", filtre.evenement() == null ? null : filtre.evenement().name(), Types.VARCHAR)
                .param("du", filtre.du() == null ? null : Timestamp.from(filtre.du()), Types.TIMESTAMP_WITH_TIMEZONE)
                .param("au", filtre.au() == null ? null : Timestamp.from(filtre.au()), Types.TIMESTAMP_WITH_TIMEZONE)
                .query((rs, i) -> {
                    long utilisateur = rs.getLong("utilisateur_id");
                    return new LigneConnexion(rs.getTimestamp("date").toInstant(), rs.getString("identifiant_saisi"),
                            rs.wasNull() ? null : utilisateur, EvenementConnexion.valueOf(rs.getString("evenement")),
                            rs.getString("detail"), rs.getString("adresse_ip"), rs.getString("navigateur"));
                })
                .list();
    }
}
