package fr.bnia.declarations.adapter.persistence;

import static fr.bnia.declarations.adapter.persistence.Lignes.SCHEMA;

import fr.bnia.declarations.application.NotesInternes;
import fr.bnia.declarations.domain.Auteur;
import fr.bnia.declarations.domain.NoteInterne;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/** Notes internes des DSM en SQL direct (spec 15 §3.7), à part de l'agrégat : elles ne changent pas sa version. */
@Repository
class JdbcNotesInternes implements NotesInternes {

    private static final String COLONNES = "id, dsm_id, ligne_id, ligne_supprimee, auteur_id, nom_auteur, ecrite_le,"
            + " modifiee_le, texte";

    private final JdbcClient jdbcClient;

    JdbcNotesInternes(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public NoteInterne inserer(NoteInterne n) {
        long id = jdbcClient.sql("insert into " + SCHEMA + ".note_interne (dsm_id, ligne_id, ligne_supprimee, auteur_id,"
                        + " nom_auteur, ecrite_le, modifiee_le, texte) values (:dsm, :ligne, :ligneSupprimee, :auteur,"
                        + " :nomAuteur, :ecriteLe, :modifieeLe, :texte) returning id")
                .param("dsm", n.dsm())
                .param("ligne", n.ligne(), Types.BIGINT)
                .param("ligneSupprimee", n.ligneSupprimee(), Types.VARCHAR)
                .param("auteur", n.auteur().utilisateur())
                .param("nomAuteur", n.auteur().nom())
                .param("ecriteLe", Timestamp.from(n.ecriteLe()))
                .param("modifieeLe", n.modifieeLe() == null ? null : Timestamp.from(n.modifieeLe()), Types.TIMESTAMP)
                .param("texte", n.texte())
                .query(Long.class)
                .single();
        return parId(id).orElseThrow();
    }

    @Override
    public void mettreAJour(NoteInterne n) {
        jdbcClient.sql("update " + SCHEMA + ".note_interne set ligne_id = :ligne, ligne_supprimee = :ligneSupprimee,"
                        + " modifiee_le = :modifieeLe, texte = :texte where id = :id")
                .param("id", n.id())
                .param("ligne", n.ligne(), Types.BIGINT)
                .param("ligneSupprimee", n.ligneSupprimee(), Types.VARCHAR)
                .param("modifieeLe", n.modifieeLe() == null ? null : Timestamp.from(n.modifieeLe()), Types.TIMESTAMP)
                .param("texte", n.texte())
                .update();
    }

    @Override
    public Optional<NoteInterne> parId(long id) {
        return jdbcClient.sql("select " + COLONNES + " from " + SCHEMA + ".note_interne where id = :id")
                .param("id", id)
                .query(JdbcNotesInternes::note)
                .optional();
    }

    @Override
    public List<NoteInterne> deLaDsm(long dsm) {
        return jdbcClient.sql("select " + COLONNES + " from " + SCHEMA + ".note_interne where dsm_id = :dsm"
                        + " order by ecrite_le, id")
                .param("dsm", dsm)
                .query(JdbcNotesInternes::note)
                .list();
    }

    @Override
    public void supprimer(long id) {
        jdbcClient.sql("delete from " + SCHEMA + ".note_interne where id = :id").param("id", id).update();
    }

    @Override
    public void supprimerDeLaDsm(long dsm) {
        jdbcClient.sql("delete from " + SCHEMA + ".note_interne where dsm_id = :dsm").param("dsm", dsm).update();
    }

    private static NoteInterne note(ResultSet rs, int n) throws SQLException {
        Timestamp modifieeLe = rs.getTimestamp("modifiee_le");
        return NoteInterne.reconstituer(rs.getLong("id"), rs.getLong("dsm_id"), rs.getObject("ligne_id", Long.class),
                rs.getString("ligne_supprimee"), new Auteur(rs.getLong("auteur_id"), rs.getString("nom_auteur")),
                rs.getTimestamp("ecrite_le").toInstant(), modifieeLe == null ? null : modifieeLe.toInstant(),
                rs.getString("texte"));
    }
}
