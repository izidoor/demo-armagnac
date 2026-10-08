package fr.bnia.operateurs.adapter.persistence;

import static fr.bnia.operateurs.adapter.persistence.OperateurJdbc.SCHEMA;

import fr.bnia.operateurs.application.ContactRepository;
import fr.bnia.operateurs.domain.Contact;
import fr.bnia.operateurs.domain.Email;
import fr.bnia.operateurs.domain.IdentitePersonne;
import java.sql.Types;
import java.util.Optional;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/** Agrégat {@code Contact} en Spring Data JDBC (A-OPE-6), versionné (RG-OPE-51). */
@Repository
class ContactJdbc implements ContactRepository {

    private final JdbcAggregateTemplate template;
    private final JdbcClient jdbcClient;

    ContactJdbc(JdbcAggregateTemplate template, JdbcClient jdbcClient) {
        this.template = template;
        this.jdbcClient = jdbcClient;
    }

    @Override
    public Optional<Contact> parId(long id) {
        return Optional.ofNullable(template.findById(id, Ligne.class)).map(ContactJdbc::versDomaine);
    }

    @Override
    public Optional<Contact> parFiche(long fiche) {
        return jdbcClient.sql("select id from " + SCHEMA + ".contact where fiche_id = :fiche").param("fiche", fiche)
                .query(Long.class).optional().flatMap(this::parId);
    }

    @Override
    public Contact inserer(Contact contact) {
        return versDomaine(template.insert(versLigne(contact)));
    }

    @Override
    public void mettreAJour(Contact contact) {
        Versionnage.mettreAJour(template, versLigne(contact), "La fiche de " + contact.identite().nomComplet());
    }

    @Override
    public void supprimer(long id) {
        template.deleteById(id, Ligne.class);
    }

    @Override
    public boolean existeEmail(long operateur, Email email, Long sauf) {
        return jdbcClient.sql("select exists (select 1 from " + SCHEMA + ".contact where operateur_id = :operateur"
                        + " and lower(email) = lower(:email) and id is distinct from :sauf)")
                .param("operateur", operateur)
                .param("email", email.valeur())
                .param("sauf", sauf, Types.BIGINT)
                .query(Boolean.class)
                .single();
    }

    private static Ligne versLigne(Contact c) {
        return new Ligne(c.id(), c.operateur(), c.identite().titre(), c.identite().nom(), c.identite().prenom(),
                c.email() == null ? null : c.email().valeur(), c.fiche(), c.id() == null ? null : c.version());
    }

    private static Contact versDomaine(Ligne l) {
        return Contact.reconstituer(l.id(), l.operateurId(), new IdentitePersonne(l.titreId(), l.nom(), l.prenom()),
                l.email() == null ? null : new Email(l.email()), l.ficheId(), l.version());
    }

    @Table(schema = SCHEMA, name = "contact")
    record Ligne(@Id Long id, long operateurId, Long titreId, String nom, String prenom, String email, long ficheId,
            @Version Long version) {
    }
}
