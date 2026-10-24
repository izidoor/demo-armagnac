package fr.bnia.notifications.adapter.persistence;

import fr.bnia.notifications.application.AbonnementRepository;
import fr.bnia.notifications.domain.Abonnement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/** Abonnements push en SQL direct (spec 11 §3.1), uniques par adresse (RG-NOT-02). */
@Repository
class JdbcAbonnements implements AbonnementRepository {

    private static final String COLONNES = "utilisateur_id, adresse, cle_p256dh, secret_auth, cree_le";

    private final JdbcClient jdbcClient;

    JdbcAbonnements(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public void enregistrer(Abonnement a) {
        jdbcClient.sql("insert into notifications.abonnement (" + COLONNES + ") values (:utilisateur, :adresse, :cle,"
                        + " :secret, :creeLe) on conflict (adresse) do update set utilisateur_id = excluded.utilisateur_id,"
                        + " cle_p256dh = excluded.cle_p256dh, secret_auth = excluded.secret_auth, cree_le = excluded.cree_le")
                .param("utilisateur", a.utilisateur())
                .param("adresse", a.adresse())
                .param("cle", a.cleP256dh())
                .param("secret", a.secretAuth())
                .param("creeLe", Timestamp.from(a.creeLe()))
                .update();
    }

    @Override
    public Optional<Abonnement> parAdresse(String adresse) {
        return jdbcClient.sql("select " + COLONNES + " from notifications.abonnement where adresse = :adresse")
                .param("adresse", adresse)
                .query(JdbcAbonnements::abonnement)
                .optional();
    }

    @Override
    public List<Abonnement> de(Collection<Long> utilisateurs) {
        if (utilisateurs.isEmpty()) {
            return List.of();
        }
        return jdbcClient.sql("select " + COLONNES + " from notifications.abonnement"
                        + " where utilisateur_id in (:utilisateurs) order by id")
                .param("utilisateurs", List.copyOf(utilisateurs))
                .query(JdbcAbonnements::abonnement)
                .list();
    }

    @Override
    public void supprimer(String adresse) {
        jdbcClient.sql("delete from notifications.abonnement where adresse = :adresse").param("adresse", adresse).update();
    }

    private static Abonnement abonnement(ResultSet rs, int n) throws SQLException {
        return new Abonnement(rs.getLong("utilisateur_id"), rs.getString("adresse"), rs.getString("cle_p256dh"),
                rs.getString("secret_auth"), rs.getTimestamp("cree_le").toInstant());
    }
}
