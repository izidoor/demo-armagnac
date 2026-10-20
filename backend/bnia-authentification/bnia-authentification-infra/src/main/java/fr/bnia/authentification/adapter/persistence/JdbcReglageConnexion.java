package fr.bnia.authentification.adapter.persistence;

import fr.bnia.authentification.application.ReglageConnexion;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/** Réglage de la connexion en SQL direct : la ligne unique créée par la migration (RG-AUT-16). */
@Repository
class JdbcReglageConnexion implements ReglageConnexion {

    private static final String TABLE = CompteJdbc.SCHEMA + ".reglage";

    private final JdbcClient jdbcClient;

    JdbcReglageConnexion(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public boolean doubleAuthentification() {
        return jdbcClient.sql("select double_authentification from " + TABLE).query(Boolean.class).single();
    }

    @Override
    public void definirDoubleAuthentification(boolean active) {
        jdbcClient.sql("update " + TABLE + " set double_authentification = :active").param("active", active).update();
    }
}
