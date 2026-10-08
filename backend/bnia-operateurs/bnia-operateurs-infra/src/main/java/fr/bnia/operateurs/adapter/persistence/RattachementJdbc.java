package fr.bnia.operateurs.adapter.persistence;

import static fr.bnia.operateurs.adapter.persistence.OperateurJdbc.SCHEMA;

import fr.bnia.operateurs.application.RattachementRepository;
import fr.bnia.operateurs.domain.Rattachement;
import fr.bnia.operateurs.domain.StatutRattachement;
import fr.bnia.operateurs.domain.partage.Role;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.annotation.Id;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/** Agrégat {@code Rattachement} en Spring Data JDBC (A-OPE-6). */
@Repository
class RattachementJdbc implements RattachementRepository {

    private final JdbcAggregateTemplate template;
    private final JdbcClient jdbcClient;

    RattachementJdbc(JdbcAggregateTemplate template, JdbcClient jdbcClient) {
        this.template = template;
        this.jdbcClient = jdbcClient;
    }

    @Override
    public Optional<Rattachement> parId(long id) {
        return Optional.ofNullable(template.findById(id, Ligne.class)).map(RattachementJdbc::versDomaine);
    }

    @Override
    public Optional<Rattachement> parFiche(long fiche) {
        return liste("fiche_id = :valeur", fiche).stream().findFirst();
    }

    @Override
    public Optional<Rattachement> de(long operateur, long utilisateur) {
        return jdbcClient.sql("select id from " + SCHEMA + ".rattachement"
                        + " where operateur_id = :operateur and utilisateur_id = :utilisateur")
                .param("operateur", operateur)
                .param("utilisateur", utilisateur)
                .query(Long.class).optional().flatMap(this::parId);
    }

    @Override
    public List<Rattachement> deLUtilisateur(long utilisateur) {
        return liste("utilisateur_id = :valeur", utilisateur);
    }

    @Override
    public List<Rattachement> deLOperateur(long operateur) {
        return liste("operateur_id = :valeur", operateur);
    }

    @Override
    public Rattachement inserer(Rattachement rattachement) {
        return versDomaine(template.insert(versLigne(rattachement)));
    }

    @Override
    public void mettreAJour(Rattachement rattachement) {
        template.update(versLigne(rattachement));
    }

    @Override
    public void supprimer(long id) {
        template.deleteById(id, Ligne.class);
    }

    /** @param condition constante, jamais une saisie utilisateur */
    private List<Rattachement> liste(String condition, long valeur) {
        return jdbcClient.sql("select id from " + SCHEMA + ".rattachement where " + condition + " order by id")
                .param("valeur", valeur)
                .query(Long.class).list().stream().map(this::parId).flatMap(Optional::stream).toList();
    }

    private static Ligne versLigne(Rattachement r) {
        return new Ligne(r.id(), r.operateur(), r.utilisateur(), r.role(), r.statut(), r.fiche(), r.creeLe());
    }

    private static Rattachement versDomaine(Ligne l) {
        return Rattachement.reconstituer(l.id(), l.operateurId(), l.utilisateurId(), l.role(), l.statut(), l.ficheId(),
                l.creeLe());
    }

    @Table(schema = SCHEMA, name = "rattachement")
    record Ligne(@Id Long id, long operateurId, long utilisateurId, Role role, StatutRattachement statut, long ficheId,
            Instant creeLe) {
    }
}
