package fr.bnia.operateurs.adapter.persistence;

import fr.bnia.operateurs.application.AnnuaireLecture;
import fr.bnia.operateurs.application.PersonneAnnuaire;
import fr.bnia.operateurs.domain.StatutRattachement;
import fr.bnia.operateurs.domain.partage.Role;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/**
 * Annuaire d'un opérateur en une requête, sans copie de données (RG-OPE-52, vision §2.2) : le nom d'un
 * membre est lu dans l'utilisateur, celui d'un contact dans le contact ; une invitation en attente d'une
 * personne nouvelle y figure d'après ses propres informations.
 */
@Repository
class JdbcAnnuaire implements AnnuaireLecture {

    private static final String S = OperateurJdbc.SCHEMA + ".";

    private static final String REQUETE = """
            select 'CONTACT' as genre, c.id, c.fiche_id, c.titre_id, c.nom, c.prenom, c.email, f.fonction,
                   f.telephone_professionnel, null as role, null as statut, false as bloque, i.id as invitation
              from %1$scontact c
              join %1$sfiche_professionnelle f on f.id = c.fiche_id
              left join %1$sinvitation i on i.fiche_id = c.fiche_id and i.statut = 'EN_ATTENTE'
             where c.operateur_id = :operateur
            union all
            select 'MEMBRE', r.id, r.fiche_id, u.titre_id, u.nom, u.prenom, u.email, f.fonction,
                   f.telephone_professionnel, r.role, r.statut, u.statut = 'BLOQUE', null
              from %1$srattachement r
              join %1$sutilisateur u on u.id = r.utilisateur_id
              join %1$sfiche_professionnelle f on f.id = r.fiche_id
             where r.operateur_id = :operateur
            union all
            select 'INVITATION', i.id, null, i.titre_id, i.nom, i.prenom, i.email, i.fonction,
                   i.telephone_professionnel, i.role, null, false, i.id
              from %1$sinvitation i
             where i.operateur_id = :operateur and i.statut = 'EN_ATTENTE' and i.fiche_id is null
             order by nom, prenom nulls first, genre
            """.formatted(S);

    private final JdbcClient jdbcClient;

    JdbcAnnuaire(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public List<PersonneAnnuaire> annuaire(long operateur) {
        return jdbcClient.sql(REQUETE).param("operateur", operateur).query((rs, i) -> personne(rs)).list();
    }

    private static PersonneAnnuaire personne(ResultSet rs) throws SQLException {
        String role = rs.getString("role");
        String statut = rs.getString("statut");
        long fiche = rs.getLong("fiche_id");
        Long idFiche = rs.wasNull() ? null : fiche;
        long titre = rs.getLong("titre_id");
        Long idTitre = rs.wasNull() ? null : titre;
        long invitation = rs.getLong("invitation");
        Long idInvitation = rs.wasNull() ? null : invitation;
        return new PersonneAnnuaire(PersonneAnnuaire.Genre.valueOf(rs.getString("genre")), rs.getLong("id"), idFiche,
                idTitre, rs.getString("nom"), rs.getString("prenom"), rs.getString("email"), rs.getString("fonction"),
                rs.getString("telephone_professionnel"), role == null ? null : Role.valueOf(role),
                statut == null ? null : StatutRattachement.valueOf(statut), rs.getBoolean("bloque"), idInvitation);
    }
}
