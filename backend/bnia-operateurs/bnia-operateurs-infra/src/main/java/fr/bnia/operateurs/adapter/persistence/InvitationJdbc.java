package fr.bnia.operateurs.adapter.persistence;

import static fr.bnia.operateurs.adapter.persistence.OperateurJdbc.SCHEMA;

import fr.bnia.operateurs.application.InvitationRepository;
import fr.bnia.operateurs.domain.Email;
import fr.bnia.operateurs.domain.IdentitePersonne;
import fr.bnia.operateurs.domain.Invitation;
import fr.bnia.operateurs.domain.StatutInvitation;
import fr.bnia.operateurs.domain.partage.Role;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.data.annotation.Id;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/** Agrégat {@code Invitation} en Spring Data JDBC (A-OPE-6) ; une en attente par e-mail et opérateur (index partiel). */
@Repository
class InvitationJdbc implements InvitationRepository {

    private final JdbcAggregateTemplate template;
    private final JdbcClient jdbcClient;

    InvitationJdbc(JdbcAggregateTemplate template, JdbcClient jdbcClient) {
        this.template = template;
        this.jdbcClient = jdbcClient;
    }

    @Override
    public Optional<Invitation> parId(long id) {
        return Optional.ofNullable(template.findById(id, Ligne.class)).map(InvitationJdbc::versDomaine);
    }

    @Override
    public Optional<Invitation> parEmpreinte(String empreinte) {
        return ids("empreinte = :empreinte", Map.of("empreinte", empreinte)).stream().findFirst();
    }

    @Override
    public Optional<Invitation> enAttentePourEmail(long operateur, Email email) {
        return ids("operateur_id = :operateur and lower(email) = lower(:email) and statut = 'EN_ATTENTE'",
                Map.of("operateur", operateur, "email", email.valeur())).stream().findFirst();
    }

    @Override
    public Optional<Invitation> enAttentePourFiche(long fiche) {
        return ids("fiche_id = :fiche and statut = 'EN_ATTENTE'", Map.of("fiche", fiche)).stream().findFirst();
    }

    @Override
    public List<Invitation> enAttente(long operateur) {
        return ids("operateur_id = :operateur and statut = 'EN_ATTENTE'", Map.of("operateur", operateur));
    }

    @Override
    public List<Invitation> echues(Instant maintenant) {
        return ids("statut = 'EN_ATTENTE' and expire_le <= :maintenant",
                Map.of("maintenant", Timestamp.from(maintenant)));
    }

    @Override
    public Invitation inserer(Invitation invitation) {
        return versDomaine(template.insert(versLigne(invitation)));
    }

    @Override
    public void mettreAJour(Invitation invitation) {
        template.update(versLigne(invitation));
    }

    /** @param condition constante, jamais une saisie utilisateur */
    private List<Invitation> ids(String condition, Map<String, ?> parametres) {
        return jdbcClient.sql("select id from " + SCHEMA + ".invitation where " + condition + " order by id")
                .params(parametres)
                .query(Long.class).list().stream()
                .map(this::parId).flatMap(Optional::stream).toList();
    }

    private static Ligne versLigne(Invitation i) {
        IdentitePersonne p = i.personne().orElse(null);
        return new Ligne(i.id(), i.operateur(), i.email().valeur(), i.role(), i.statut(), i.expireLe(), i.empreinte(),
                i.fiche().orElse(null), p == null ? null : p.titre(), p == null ? null : p.nom(),
                p == null ? null : p.prenom(), i.fonction(), i.telephoneProfessionnel(), i.envoyeePar(), i.envoyeeLe(),
                i.accepteeLe().orElse(null), i.utilisateur().orElse(null), i.compteVise().orElse(null));
    }

    private static Invitation versDomaine(Ligne l) {
        IdentitePersonne personne = l.nom() == null ? null : new IdentitePersonne(l.titreId(), l.nom(), l.prenom());
        return Invitation.reconstituer(l.id(), l.operateurId(), new Email(l.email()), l.role(), l.statut(),
                l.expireLe(), l.empreinte(), l.ficheId(), personne, l.fonction(), l.telephoneProfessionnel(),
                l.envoyeePar(), l.envoyeeLe(), l.accepteeLe(), l.utilisateurId(), l.compteViseId());
    }

    @Table(schema = SCHEMA, name = "invitation")
    record Ligne(
            @Id Long id,
            long operateurId,
            String email,
            Role role,
            StatutInvitation statut,
            Instant expireLe,
            String empreinte,
            Long ficheId,
            Long titreId,
            String nom,
            String prenom,
            String fonction,
            String telephoneProfessionnel,
            String envoyeePar,
            Instant envoyeeLe,
            Instant accepteeLe,
            Long utilisateurId,
            Long compteViseId) {
    }
}
