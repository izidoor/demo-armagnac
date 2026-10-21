package fr.bnia.facturation.adapter.persistence;

import static fr.bnia.facturation.adapter.persistence.Lignes.SCHEMA;

import fr.bnia.facturation.application.NotesInternes;
import fr.bnia.facturation.domain.Auteur;
import fr.bnia.facturation.domain.NoteInterne;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/** Notes internes des pièces en SQL direct (RG-FAC-21), à part de l'agrégat : elles ne changent pas sa version. */
@Repository
class JdbcNotesPieces implements NotesInternes {

    private static final String COLONNES = "id, facture_id, auteur_id, nom_auteur, ecrite_le, modifiee_le, texte";

    private final JdbcClient jdbcClient;

    JdbcNotesPieces(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public NoteInterne inserer(NoteInterne n) {
        long id = jdbcClient.sql("insert into " + SCHEMA + ".note_interne (facture_id, auteur_id, nom_auteur, ecrite_le,"
                        + " modifiee_le, texte) values (:piece, :auteur, :nomAuteur, :ecriteLe, :modifieeLe, :texte)"
                        + " returning id")
                .param("piece", n.piece())
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
        jdbcClient.sql("update " + SCHEMA + ".note_interne set modifiee_le = :modifieeLe, texte = :texte where id = :id")
                .param("id", n.id())
                .param("modifieeLe", n.modifieeLe() == null ? null : Timestamp.from(n.modifieeLe()), Types.TIMESTAMP)
                .param("texte", n.texte())
                .update();
    }

    @Override
    public Optional<NoteInterne> parId(long id) {
        return jdbcClient.sql("select " + COLONNES + " from " + SCHEMA + ".note_interne where id = :id")
                .param("id", id)
                .query(JdbcNotesPieces::note)
                .optional();
    }

    @Override
    public List<NoteInterne> deLaPiece(long piece) {
        return jdbcClient.sql("select " + COLONNES + " from " + SCHEMA + ".note_interne where facture_id = :piece"
                        + " order by ecrite_le, id")
                .param("piece", piece)
                .query(JdbcNotesPieces::note)
                .list();
    }

    @Override
    public void supprimer(long id) {
        jdbcClient.sql("delete from " + SCHEMA + ".note_interne where id = :id").param("id", id).update();
    }

    @Override
    public void supprimerDeLaPiece(long piece) {
        jdbcClient.sql("delete from " + SCHEMA + ".note_interne where facture_id = :piece").param("piece", piece)
                .update();
    }

    private static NoteInterne note(ResultSet rs, int n) throws SQLException {
        Timestamp modifieeLe = rs.getTimestamp("modifiee_le");
        return NoteInterne.reconstituer(rs.getLong("id"), rs.getLong("facture_id"),
                new Auteur(rs.getLong("auteur_id"), rs.getString("nom_auteur")), rs.getTimestamp("ecrite_le").toInstant(),
                modifieeLe == null ? null : modifieeLe.toInstant(), rs.getString("texte"));
    }
}
