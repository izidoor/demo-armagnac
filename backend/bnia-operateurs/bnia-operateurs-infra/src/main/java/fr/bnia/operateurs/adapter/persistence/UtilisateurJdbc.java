package fr.bnia.operateurs.adapter.persistence;

import static fr.bnia.operateurs.adapter.persistence.OperateurJdbc.SCHEMA;

import fr.bnia.operateurs.application.UtilisateurRepository;
import fr.bnia.operateurs.domain.ChangementEmail;
import fr.bnia.operateurs.domain.Email;
import fr.bnia.operateurs.domain.IdentitePersonne;
import fr.bnia.operateurs.domain.StatutUtilisateur;
import fr.bnia.operateurs.domain.Utilisateur;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/**
 * Agrégat {@code Utilisateur} en Spring Data JDBC (A-OPE-6), avec sa demande de changement d'e-mail,
 * versionné (RG-OPE-51).
 */
@Repository
class UtilisateurJdbc implements UtilisateurRepository {

    private final JdbcAggregateTemplate template;
    private final JdbcClient jdbcClient;

    UtilisateurJdbc(JdbcAggregateTemplate template, JdbcClient jdbcClient) {
        this.template = template;
        this.jdbcClient = jdbcClient;
    }

    @Override
    public Optional<Utilisateur> parId(long id) {
        return Optional.ofNullable(template.findById(id, Ligne.class)).map(UtilisateurJdbc::versDomaine);
    }

    @Override
    public Optional<Utilisateur> parEmail(Email email) {
        return jdbcClient.sql("select id from " + SCHEMA + ".utilisateur where lower(email) = lower(:email)")
                .param("email", email.valeur()).query(Long.class).optional().flatMap(this::parId);
    }

    @Override
    public Optional<Utilisateur> parEmpreinteChangementEmail(String empreinte) {
        return jdbcClient.sql("select utilisateur_id from " + SCHEMA + ".changement_email where empreinte = :empreinte")
                .param("empreinte", empreinte).query(Long.class).optional().flatMap(this::parId);
    }

    @Override
    public Utilisateur inserer(Utilisateur utilisateur) {
        return versDomaine(template.insert(versLigne(utilisateur)));
    }

    @Override
    public void mettreAJour(Utilisateur utilisateur) {
        Versionnage.mettreAJour(template, versLigne(utilisateur), "Le profil de " + utilisateur.identite().nomComplet());
    }

    @Override
    public void supprimer(long id) {
        template.deleteById(id, Ligne.class);
    }

    private static Ligne versLigne(Utilisateur u) {
        return new Ligne(u.id(), u.identite().titre(), u.identite().nom(), u.identite().prenom(), u.email().valeur(),
                u.statut(), u.operateurActif().orElse(null),
                u.changementEmail().map(c -> new ChangementLigne(c.nouvelEmail().valeur(), c.empreinte(), c.expireLe()))
                        .orElse(null),
                u.id() == null ? null : u.version());
    }

    private static Utilisateur versDomaine(Ligne l) {
        ChangementLigne c = l.changementEmail();
        return Utilisateur.reconstituer(l.id(), IdentitePersonne.utilisateur(l.titreId(), l.nom(), l.prenom()),
                new Email(l.email()), l.statut(), l.operateurActifId(),
                c == null ? null : new ChangementEmail(new Email(c.nouvelEmail()), c.empreinte(), c.expireLe()),
                l.version());
    }

    @Table(schema = SCHEMA, name = "utilisateur")
    record Ligne(
            @Id Long id,
            Long titreId,
            String nom,
            String prenom,
            String email,
            StatutUtilisateur statut,
            Long operateurActifId,
            @MappedCollection(idColumn = "utilisateur_id") ChangementLigne changementEmail,
            @Version Long version) {
    }

    @Table(schema = SCHEMA, name = "changement_email")
    record ChangementLigne(String nouvelEmail, String empreinte, Instant expireLe) {
    }
}
