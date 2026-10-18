package fr.bnia.commun.adapter.persistence;

import fr.bnia.commun.application.FormatDocument;
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
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/**
 * Stockage des documents en base (ADR-018) : table {@code documents.document}, contenu en {@code bytea}, dans la
 * transaction de l'appelant. Garde-fous : 10 Mo au plus, format vérifié par sa signature.
 */
@Repository
class JdbcStockageDocuments implements StockageDocuments {

    /** Signature de chaque format (ADR-018 amendé) : un {@code .xlsx} est une archive ZIP. */
    private static final Map<FormatDocument, byte[]> SIGNATURES = Map.of(
            FormatDocument.PDF, "%PDF".getBytes(StandardCharsets.US_ASCII),
            FormatDocument.PNG, new byte[] {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A},
            FormatDocument.JPEG, new byte[] {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF},
            FormatDocument.XLSX, new byte[] {'P', 'K', 0x03, 0x04});

    private final JdbcClient jdbcClient;
    private final Clock horloge;

    JdbcStockageDocuments(JdbcClient jdbcClient, Clock horloge) {
        this.jdbcClient = jdbcClient;
        this.horloge = horloge;
    }

    @Override
    public DocumentDepose deposer(String nom, byte[] contenu, Set<FormatDocument> admis) {
        if (contenu.length == 0) {
            throw new DocumentRefuse("Le fichier « " + nom + " » est vide.");
        }
        if (contenu.length > TAILLE_MAXIMALE) {
            throw new DocumentRefuse("Le fichier « " + nom + " » dépasse 10 Mo.");
        }
        FormatDocument format = admis.stream().sorted().filter(f -> commencePar(contenu, SIGNATURES.get(f)))
                .findFirst()
                .orElseThrow(() -> new DocumentRefuse("Le fichier « " + nom + " » n'est pas " + admis.stream().sorted()
                        .map(FormatDocument::designation).collect(Collectors.joining(", ni ")) + "."));
        long cle = jdbcClient.sql("insert into documents.document (nom, type, taille, contenu, depose_le)"
                        + " values (:nom, :type, :taille, :contenu, :deposeLe) returning id")
                .param("nom", nom)
                .param("type", format.type())
                .param("taille", contenu.length)
                .param("contenu", contenu)
                .param("deposeLe", Timestamp.from(horloge.instant()))
                .query(Long.class)
                .single();
        return new DocumentDepose(cle, nom, format.type(), contenu.length);
    }

    private static boolean commencePar(byte[] contenu, byte[] signature) {
        return contenu.length >= signature.length
                && Arrays.equals(contenu, 0, signature.length, signature, 0, signature.length);
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
