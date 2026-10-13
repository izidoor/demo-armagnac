package fr.bnia.commun.adapter.persistence;

import fr.bnia.commun.application.StockageDocuments;
import fr.bnia.commun.domain.DocumentDepose;
import fr.bnia.commun.domain.DocumentRefuse;
import fr.bnia.commun.domain.ElementIntrouvable;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.Clock;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/**
 * Stockage des documents en base (ADR-018) : table {@code documents.document}, contenu en {@code bytea}, dans la
 * transaction de l'appelant. Garde-fous : 10 Mo au plus, PDF vérifié par sa signature.
 */
@Repository
class JdbcStockageDocuments implements StockageDocuments {

    private static final String PDF = "application/pdf";
    private static final byte[] SIGNATURE_PDF = "%PDF".getBytes(StandardCharsets.US_ASCII);

    private final JdbcClient jdbcClient;
    private final Clock horloge;

    JdbcStockageDocuments(JdbcClient jdbcClient, Clock horloge) {
        this.jdbcClient = jdbcClient;
        this.horloge = horloge;
    }

    @Override
    public DocumentDepose deposerPdf(String nom, byte[] contenu) {
        if (contenu.length == 0) {
            throw new DocumentRefuse("Le fichier « " + nom + " » est vide.");
        }
        if (contenu.length > TAILLE_MAXIMALE) {
            throw new DocumentRefuse("Le fichier « " + nom + " » dépasse 10 Mo.");
        }
        if (contenu.length < SIGNATURE_PDF.length
                || !Arrays.equals(contenu, 0, SIGNATURE_PDF.length, SIGNATURE_PDF, 0, SIGNATURE_PDF.length)) {
            throw new DocumentRefuse("Le fichier « " + nom + " » n'est pas un PDF.");
        }
        long cle = jdbcClient.sql("insert into documents.document (nom, type, taille, contenu, depose_le)"
                        + " values (:nom, :type, :taille, :contenu, :deposeLe) returning id")
                .param("nom", nom)
                .param("type", PDF)
                .param("taille", contenu.length)
                .param("contenu", contenu)
                .param("deposeLe", Timestamp.from(horloge.instant()))
                .query(Long.class)
                .single();
        return new DocumentDepose(cle, nom, PDF, contenu.length);
    }

    @Override
    public DocumentLu lire(long cle) {
        return jdbcClient.sql("select id, nom, type, taille, contenu from documents.document where id = :id")
                .param("id", cle)
                .query((rs, n) -> new DocumentLu(new DocumentDepose(rs.getLong("id"), rs.getString("nom"),
                        rs.getString("type"), rs.getLong("taille")), rs.getBytes("contenu")))
                .optional()
                .orElseThrow(() -> new ElementIntrouvable("Document introuvable : " + cle + "."));
    }

    @Override
    public void supprimer(long cle) {
        jdbcClient.sql("delete from documents.document where id = :id").param("id", cle).update();
    }

    @Override
    public void supprimer(Collection<Long> cles) {
        if (!cles.isEmpty()) {
            jdbcClient.sql("delete from documents.document where id in (:ids)").param("ids", List.copyOf(cles))
                    .update();
        }
    }
}
