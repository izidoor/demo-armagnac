package fr.bnia.declarations.domain;

import fr.bnia.commun.domain.DocumentDepose;
import java.time.Instant;
import java.util.Objects;

/**
 * Justificatif d'une DSM (entité, spec 05 §3.2.2) : un PDF déposé, rattaché à la DSM ou à une de ses lignes,
 * avec un type de mouvement et un type de pièce compatibles (RG-DEC-26). Enregistré à part de la DSM, comme
 * les notes internes : un dépôt ne change pas sa version.
 */
public final class Justificatif {

    private final Long id;
    private final long dsm;
    private Long ligne;
    private String ligneSupprimee;
    private final TypeMouvement typeMouvement;
    private final TypePiece typePiece;
    private final DocumentDepose document;
    private final String reference;
    private final Auteur deposePar;
    private final Instant deposeLe;

    private Justificatif(Long id, long dsm, Long ligne, String ligneSupprimee, TypeMouvement typeMouvement,
            TypePiece typePiece, DocumentDepose document, String reference, Auteur deposePar, Instant deposeLe) {
        this.id = id;
        this.dsm = dsm;
        this.ligne = ligne;
        this.ligneSupprimee = ligneSupprimee;
        this.typeMouvement = Objects.requireNonNull(typeMouvement, "typeMouvement");
        this.typePiece = Objects.requireNonNull(typePiece, "typePiece");
        this.document = Objects.requireNonNull(document, "document");
        this.reference = reference;
        this.deposePar = Objects.requireNonNull(deposePar, "deposePar");
        this.deposeLe = Objects.requireNonNull(deposeLe, "deposeLe");
    }

    /** RG-DEC-26 : sur la DSM ({@code ligne} absente) ou sur une de ses lignes ; référence DAE/DAI facultative. */
    public static Justificatif deposer(long dsm, Long ligne, TypeMouvement typeMouvement, TypePiece typePiece,
            String reference, DocumentDepose document, Auteur par, Instant le) {
        typeMouvement.exigerPiece(typePiece);
        String ref = reference == null || reference.isBlank() ? null : reference.strip();
        return new Justificatif(null, dsm, ligne, null, typeMouvement, typePiece, document, ref, par, le);
    }

    public static Justificatif reconstituer(long id, long dsm, Long ligne, String ligneSupprimee,
            TypeMouvement typeMouvement, TypePiece typePiece, DocumentDepose document, String reference,
            Auteur deposePar, Instant deposeLe) {
        return new Justificatif(id, dsm, ligne, ligneSupprimee, typeMouvement, typePiece, document, reference,
                deposePar, deposeLe);
    }

    /** Comme une note interne : quand sa ligne est supprimée, la pièce passe sur la DSM et garde le libellé. */
    public void detacher(String libelleLigne) {
        this.ligne = null;
        this.ligneSupprimee = libelleLigne;
    }

    public Long id() {
        return id;
    }

    public long dsm() {
        return dsm;
    }

    public Long ligne() {
        return ligne;
    }

    public String ligneSupprimee() {
        return ligneSupprimee;
    }

    public TypeMouvement typeMouvement() {
        return typeMouvement;
    }

    public TypePiece typePiece() {
        return typePiece;
    }

    public DocumentDepose document() {
        return document;
    }

    public String reference() {
        return reference;
    }

    public Auteur deposePar() {
        return deposePar;
    }

    public Instant deposeLe() {
        return deposeLe;
    }
}
