package fr.bnia.authentification.adapter.persistence;

import fr.bnia.authentification.application.CompteRepository;
import fr.bnia.authentification.domain.CompteConnexion;
import fr.bnia.authentification.domain.Echeance;
import fr.bnia.authentification.domain.NavigateurConfiance;
import fr.bnia.commun.domain.ConflitUnicite;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.annotation.Id;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/**
 * Agrégat {@code CompteConnexion} en Spring Data JDBC (spec 03 §3.1 à 3.4) : code, réinitialisation et
 * navigateurs de confiance réécrits à chaque enregistrement. L'utilisateur est référencé sans clé
 * étrangère (ADR-015).
 */
@Repository
class CompteJdbc implements CompteRepository {

    static final String SCHEMA = "authentification";

    private final JdbcAggregateTemplate template;
    private final JdbcClient jdbcClient;

    CompteJdbc(JdbcAggregateTemplate template, JdbcClient jdbcClient) {
        this.template = template;
        this.jdbcClient = jdbcClient;
    }

    @Override
    public Optional<CompteConnexion> parUtilisateur(long utilisateur) {
        return id("utilisateur_id = :valeur", Map.of("valeur", utilisateur));
    }

    @Override
    public Optional<CompteConnexion> parIdentifiant(String identifiant) {
        return id("lower(identifiant) = lower(:valeur)", Map.of("valeur", identifiant));
    }

    @Override
    public Optional<CompteConnexion> parEmpreinteReinitialisation(String empreinte) {
        return jdbcClient.sql("select compte_id from " + SCHEMA + ".reinitialisation where empreinte = :empreinte")
                .param("empreinte", empreinte).query(Long.class).optional()
                .map(id -> template.findById(id, Ligne.class)).map(CompteJdbc::versDomaine);
    }

    @Override
    public CompteConnexion inserer(CompteConnexion compte) {
        try {
            return versDomaine(template.insert(versLigne(compte)));
        } catch (DuplicateKeyException pris) {
            throw identifiantPris(compte);
        }
    }

    @Override
    public void mettreAJour(CompteConnexion compte) {
        try {
            template.update(versLigne(compte));
        } catch (DuplicateKeyException pris) {
            throw identifiantPris(compte);
        }
    }

    @Override
    public void supprimer(long id) {
        template.deleteById(id, Ligne.class);
    }

    /** @param condition constante, jamais une saisie utilisateur */
    private Optional<CompteConnexion> id(String condition, Map<String, ?> parametres) {
        return jdbcClient.sql("select id from " + SCHEMA + ".compte where " + condition).params(parametres)
                .query(Long.class).optional()
                .map(id -> template.findById(id, Ligne.class)).map(CompteJdbc::versDomaine);
    }

    private static ConflitUnicite identifiantPris(CompteConnexion compte) {
        return new ConflitUnicite("RG-AUT-10", "« " + compte.identifiant() + " » est déjà l'identifiant d'un autre compte"
                + " de connexion.");
    }

    private static Ligne versLigne(CompteConnexion c) {
        return new Ligne(c.id(), c.utilisateur(), c.identifiant(), c.motDePasse().orElse(null), c.echecs(),
                c.bloqueJusqua().orElse(null), c.derniereConnexion().orElse(null), c.motDePasseChangeLe().orElse(null),
                c.code().map(e -> new CodeLigne(e.empreinte(), e.expireLe())).orElse(null),
                c.reinitialisation().map(e -> new ReinitialisationLigne(e.empreinte(), e.expireLe())).orElse(null),
                c.navigateurs().stream().map(n -> new NavigateurLigne(n.empreinte(), n.creeLe(), n.expireLe()))
                        .collect(Collectors.toSet()));
    }

    private static CompteConnexion versDomaine(Ligne l) {
        return CompteConnexion.reconstituer(l.id(), l.utilisateurId(), l.identifiant(), l.motDePasse(), l.echecs(),
                l.bloqueJusqua(), l.derniereConnexion(), l.motDePasseChangeLe(),
                l.code() == null ? null : new Echeance(l.code().empreinte(), l.code().expireLe()),
                l.reinitialisation() == null ? null
                        : new Echeance(l.reinitialisation().empreinte(), l.reinitialisation().expireLe()),
                l.navigateurs().stream().map(n -> new NavigateurConfiance(n.empreinte(), n.creeLe(), n.expireLe()))
                        .toList());
    }

    @Table(schema = SCHEMA, name = "compte")
    record Ligne(
            @Id Long id,
            long utilisateurId,
            String identifiant,
            String motDePasse,
            int echecs,
            Instant bloqueJusqua,
            Instant derniereConnexion,
            Instant motDePasseChangeLe,
            @MappedCollection(idColumn = "compte_id") CodeLigne code,
            @MappedCollection(idColumn = "compte_id") ReinitialisationLigne reinitialisation,
            @MappedCollection(idColumn = "compte_id") Set<NavigateurLigne> navigateurs) {
    }

    @Table(schema = SCHEMA, name = "code_verification")
    record CodeLigne(String empreinte, Instant expireLe) {
    }

    @Table(schema = SCHEMA, name = "reinitialisation")
    record ReinitialisationLigne(String empreinte, Instant expireLe) {
    }

    @Table(schema = SCHEMA, name = "navigateur_confiance")
    record NavigateurLigne(String empreinte, Instant creeLe, Instant expireLe) {
    }
}
