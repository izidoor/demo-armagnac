package fr.bnia.referentiels.adapter.persistence;

import fr.bnia.commun.domain.ElementUtilise;
import fr.bnia.referentiels.application.ValeursRepository;
import fr.bnia.referentiels.domain.Code;
import fr.bnia.referentiels.domain.ValeurSimple;
import java.sql.Types;
import java.util.Optional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.jdbc.core.simple.JdbcClient;

/**
 * Écriture d'une liste de référentiel (ADR-004) : Spring Data JDBC pour l'agrégat, SQL direct pour
 * le contrôle d'unicité. L'identifiant est attribué par la base à l'insertion (ADR-011).
 *
 * @param <T> classe du domaine
 * @param <R> ligne persistée
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
    public Optional<T> parId(long id) {
        return Optional.ofNullable(template.findById(id, typeLigne)).map(this::versDomaine);
    }

    @Override
    public boolean existeCode(Code code, Long sauf) {
        return jdbcClient.sql("select exists (select 1 from " + table
                        + " where lower(code) = lower(:code) and id is distinct from :sauf)")
                .param("code", code.valeur())
                .param("sauf", sauf, Types.BIGINT)
                .query(Boolean.class)
                .single();
    }

    @Override
    public T inserer(T valeur) {
        return versDomaine(template.insert(versLigne(valeur)));
    }

    @Override
    public void mettreAJour(T valeur) {
        template.update(versLigne(valeur));
    }

    @Override
    public void supprimer(long id) {
        try {
            template.deleteById(id, typeLigne);
        } catch (DataIntegrityViolationException utilisee) {
            throw valeurUtilisee();
        }
    }

    /** RG-REF-04 : une valeur référencée ailleurs (clé étrangère) se désactive, elle ne se supprime pas. */
    static ElementUtilise valeurUtilisee() {
        return new ElementUtilise("RG-REF-04",
                "Cette valeur est utilisée par d'autres données : désactivez-la plutôt que de la supprimer.");
    }
}
