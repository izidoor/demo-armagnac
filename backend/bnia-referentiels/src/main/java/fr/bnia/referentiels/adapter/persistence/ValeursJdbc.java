package fr.bnia.referentiels.adapter.persistence;

import fr.bnia.referentiels.application.ValeursRepository;
import fr.bnia.referentiels.domain.Code;
import fr.bnia.referentiels.domain.ValeurSimple;
import java.util.Optional;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.jdbc.core.simple.JdbcClient;

/**
 * Écriture d'une liste de référentiel (ADR-004) : Spring Data JDBC pour l'agrégat, SQL direct pour
 * le contrôle d'unicité.
 *
 * <p>Le code est l'identifiant de la ligne : Spring Data ne peut pas deviner seul s'il s'agit d'une
 * création ou d'une mise à jour, d'où {@code insert} et {@code update} explicites.
 *
 * @param <T> classe du domaine
 * @param <R> ligne persistée, dont l'identifiant est le code
 */
abstract class ValeursJdbc<T extends ValeurSimple, R> implements ValeursRepository<T> {

    protected static final String SCHEMA = "referentiels";

    protected final JdbcAggregateTemplate template;
    protected final JdbcClient jdbcClient;
    private final Class<R> typeLigne;
    /** Table qualifiée par son schéma ({@code referentiels.titre}), jamais une saisie utilisateur. */
    protected final String table;

    protected ValeursJdbc(JdbcAggregateTemplate template, JdbcClient jdbcClient, Class<R> typeLigne,
            String table) {
        this.template = template;
        this.jdbcClient = jdbcClient;
        this.typeLigne = typeLigne;
        this.table = SCHEMA + "." + table;
    }

    protected abstract R versLigne(T valeur);

    protected abstract T versDomaine(R ligne);

    @Override
    public Optional<T> parCode(Code code) {
        return Optional.ofNullable(template.findById(code.valeur(), typeLigne)).map(this::versDomaine);
    }

    @Override
    public boolean existeCode(Code code) {
        return jdbcClient.sql("select exists (select 1 from " + table + " where lower(code) = lower(:code))")
                .param("code", code.valeur())
                .query(Boolean.class)
                .single();
    }

    @Override
    public void inserer(T valeur) {
        template.insert(versLigne(valeur));
    }

    @Override
    public void mettreAJour(T valeur) {
        template.update(versLigne(valeur));
    }
}
