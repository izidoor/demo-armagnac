package fr.bnia.declarations.adapter.persistence;

import static fr.bnia.declarations.adapter.persistence.Lignes.SCHEMA;

import fr.bnia.commun.domain.DocumentDepose;
import fr.bnia.declarations.application.Pieces;
import fr.bnia.declarations.domain.Auteur;
import fr.bnia.declarations.domain.Drm;
import fr.bnia.declarations.domain.Justificatif;
import fr.bnia.declarations.domain.TypeMouvement;
import fr.bnia.declarations.domain.TypePiece;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/**
 * Justificatifs et DRM des DSM en SQL direct (spec 05 §3.2.2, §3.2.3), à part de l'agrégat : ils ne changent
 * pas sa version. La référence du document ({@code DocumentDepose}) est copiée dans leurs tables (ADR-018) :
 * une requête par liste, sans jointure vers le stockage.
 */
@Repository
class JdbcPieces implements Pieces {

    private static final String DOCUMENT = "document_id, document_nom, document_type, document_taille";
    private static final String JUSTIFICATIFS = "select id, dsm_id, ligne_id, ligne_supprimee, type_mouvement,"
            + " type_piece, reference, depose_par_id, nom_depose_par, depose_le, " + DOCUMENT + " from " + SCHEMA
            + ".justificatif";
    private static final String DRMS = "select id, dsm_id, mois, depose_par_id, nom_depose_par, depose_le, "
            + DOCUMENT + " from " + SCHEMA + ".drm";

    private final JdbcClient jdbcClient;

    JdbcPieces(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public Justificatif inserer(Justificatif j) {
        long id = jdbcClient.sql("insert into " + SCHEMA + ".justificatif (dsm_id, ligne_id, ligne_supprimee,"
                        + " type_mouvement, type_piece, " + DOCUMENT + ", reference, depose_par_id, nom_depose_par,"
                        + " depose_le) values (:dsm, :ligne, :ligneSupprimee, :typeMouvement, :typePiece, :document,"
                        + " :documentNom, :documentType, :documentTaille, :reference, :deposePar, :nomDeposePar,"
                        + " :deposeLe) returning id")
                .param("dsm", j.dsm())
                .param("ligne", j.ligne(), Types.BIGINT)
                .param("ligneSupprimee", j.ligneSupprimee(), Types.VARCHAR)
                .param("typeMouvement", j.typeMouvement().name())
                .param("typePiece", j.typePiece().name())
                .param("document", j.document().cle())
                .param("documentNom", j.document().nom())
                .param("documentType", j.document().type())
                .param("documentTaille", j.document().taille())
                .param("reference", j.reference(), Types.VARCHAR)
                .param("deposePar", j.deposePar().utilisateur())
                .param("nomDeposePar", j.deposePar().nom())
                .param("deposeLe", Timestamp.from(j.deposeLe()))
                .query(Long.class)
                .single();
        return justificatif(id).orElseThrow();
    }

    @Override
    public void mettreAJour(Justificatif j) {
        jdbcClient.sql("update " + SCHEMA + ".justificatif set ligne_id = :ligne, ligne_supprimee = :ligneSupprimee"
                        + " where id = :id")
                .param("id", j.id())
                .param("ligne", j.ligne(), Types.BIGINT)
                .param("ligneSupprimee", j.ligneSupprimee(), Types.VARCHAR)
                .update();
    }

    @Override
    public Optional<Justificatif> justificatif(long id) {
        return jdbcClient.sql(JUSTIFICATIFS + " where id = :id").param("id", id)
                .query(JdbcPieces::justificatif).optional();
    }

    @Override
    public List<Justificatif> justificatifsDe(long dsm) {
        return jdbcClient.sql(JUSTIFICATIFS + " where dsm_id = :dsm order by depose_le, id").param("dsm", dsm)
                .query(JdbcPieces::justificatif).list();
    }

    @Override
    public void supprimerJustificatif(long id) {
        jdbcClient.sql("delete from " + SCHEMA + ".justificatif where id = :id").param("id", id).update();
    }

    @Override
    public Drm inserer(Drm drm) {
        long id = jdbcClient.sql("insert into " + SCHEMA + ".drm (dsm_id, mois, " + DOCUMENT + ", depose_par_id,"
                        + " nom_depose_par, depose_le) values (:dsm, :mois, :document, :documentNom, :documentType,"
                        + " :documentTaille, :deposePar, :nomDeposePar, :deposeLe) returning id")
                .param("dsm", drm.dsm())
                .param("mois", Date.valueOf(drm.mois().atDay(1)))
                .param("document", drm.document().cle())
                .param("documentNom", drm.document().nom())
                .param("documentType", drm.document().type())
                .param("documentTaille", drm.document().taille())
                .param("deposePar", drm.deposePar().utilisateur())
                .param("nomDeposePar", drm.deposePar().nom())
                .param("deposeLe", Timestamp.from(drm.deposeLe()))
                .query(Long.class)
                .single();
        return new Drm(id, drm.dsm(), drm.mois(), drm.document(), drm.deposePar(), drm.deposeLe());
    }

    @Override
    public Optional<Drm> drm(long dsm, YearMonth mois) {
        return jdbcClient.sql(DRMS + " where dsm_id = :dsm and mois = :mois")
                .param("dsm", dsm)
                .param("mois", Date.valueOf(mois.atDay(1)))
                .query(JdbcPieces::drm)
                .optional();
    }

    @Override
    public List<Drm> drmsDe(long dsm) {
        return jdbcClient.sql(DRMS + " where dsm_id = :dsm order by mois").param("dsm", dsm)
                .query(JdbcPieces::drm).list();
    }

    @Override
    public void supprimerDrm(long id) {
        jdbcClient.sql("delete from " + SCHEMA + ".drm where id = :id").param("id", id).update();
    }

    private static Justificatif justificatif(ResultSet rs, int n) throws SQLException {
        return Justificatif.reconstituer(rs.getLong("id"), rs.getLong("dsm_id"), rs.getObject("ligne_id", Long.class),
                rs.getString("ligne_supprimee"), TypeMouvement.valueOf(rs.getString("type_mouvement")),
                TypePiece.valueOf(rs.getString("type_piece")), document(rs), rs.getString("reference"),
                deposePar(rs), rs.getTimestamp("depose_le").toInstant());
    }

    private static Drm drm(ResultSet rs, int n) throws SQLException {
        return new Drm(rs.getLong("id"), rs.getLong("dsm_id"), YearMonth.from(rs.getDate("mois").toLocalDate()),
                document(rs), deposePar(rs), rs.getTimestamp("depose_le").toInstant());
    }

    private static DocumentDepose document(ResultSet rs) throws SQLException {
        return new DocumentDepose(rs.getLong("document_id"), rs.getString("document_nom"),
                rs.getString("document_type"), rs.getLong("document_taille"));
    }

    private static Auteur deposePar(ResultSet rs) throws SQLException {
        return new Auteur(rs.getLong("depose_par_id"), rs.getString("nom_depose_par"));
    }
}
